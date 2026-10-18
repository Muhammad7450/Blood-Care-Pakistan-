const functions = require("firebase-functions");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Configurable default notification radius in kilometers.
 * Can be overridden per donor preference (e.g., 5, 10, 15, 25 km).
 */
const DEFAULT_NOTIFICATION_RADIUS_KM = 10.0;

/**
 * Centralized, configurable blood compatibility rules.
 * Maps the recipient's requested blood group to a list of compatible donor groups.
 *
 * Medical compatibility for red blood cell donations:
 * - A+  <- A+, A-, O+, O-
 * - A-  <- A-, O-
 * - B+  <- B+, B-, O+, O-
 * - B-  <- B-, O-
 * - AB+ <- Universal Recipient (all 8 blood groups)
 * - AB- <- A-, B-, AB-, O-
 * - O+  <- O+, O-
 * - O-  <- O- (Universal Donor)
 */
const BLOOD_COMPATIBILITY_RULES = {
  "A+": ["A+", "A-", "O+", "O-"],
  "A-": ["A-", "O-"],
  "B+": ["B+", "B-", "O+", "O-"],
  "B-": ["B-", "O-"],
  "AB+": ["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"],
  "AB-": ["A-", "B-", "AB-", "O-"],
  "O+": ["O+", "O-"],
  "O-": ["O-"]
};

function getCompatibleDonorBloodGroups(requestedBloodGroup) {
  const normalized = (requestedBloodGroup || "").trim().toUpperCase();
  return BLOOD_COMPATIBILITY_RULES[normalized] || [normalized];
}

/**
 * Haversine formula to compute great-circle distance between two GPS coordinates in kilometers.
 */
function calculateHaversineDistanceKm(lat1, lon1, lat2, lon2) {
  if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
    return 999999.0;
  }
  const R = 6371.0; // Earth's mean radius in km
  const dLat = ((lat2 - lat1) * Math.PI) / 180.0;
  const dLon = ((lon2 - lon1) * Math.PI) / 180.0;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180.0) *
      Math.cos((lat2 * Math.PI) / 180.0) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

/**
 * Cloud Function: onBloodRequestCreated
 * Triggers on document creation in `bloodRequests/{requestId}`
 */
exports.onBloodRequestCreated = functions.firestore
  .document("bloodRequests/{requestId}")
  .onCreate(async (snap, context) => {
    const requestId = context.params.requestId;
    const request = snap.data();

    if (!request) {
      console.log(`[onBloodRequestCreated] Empty request data for ${requestId}`);
      return null;
    }

    // Check request status
    const rawStatus = (request.status || "open").toLowerCase();
    if (rawStatus !== "open" && rawStatus !== "active" && rawStatus !== "pending") {
      console.log(`[onBloodRequestCreated] Request ${requestId} is not open/active (status: ${rawStatus})`);
      return null;
    }

    const bloodGroup = request.bloodGroup || "O+";
    const unitsRequired = request.unitsRequired || request.units || 1;
    const hospitalName = request.hospitalName || "Emergency Hospital";
    const urgency = request.urgency || request.emergencyLevel || "Urgent";
    const reqLat = request.latitude;
    const reqLng = request.longitude;
    const patientId = request.patientId || "";
    const city = request.city || "Pakistan";
    const normalizedCity = (request.normalizedCity || city || "").trim().toLowerCase();

    const compatibleGroups = getCompatibleDonorBloodGroups(bloodGroup);
    console.log(`[onBloodRequestCreated] Request ${requestId}: Needed ${bloodGroup}. Compatible groups:`, compatibleGroups);

    try {
      // Query candidate donors from both `users` and `donors` collections
      let donorDocs = [];

      try {
        const usersSnapshot = await db
          .collection("users")
          .where("isDonor", "==", true)
          .where("isAvailable", "==", true)
          .get();
        donorDocs.push(...usersSnapshot.docs);
      } catch (err) {
        console.warn("[onBloodRequestCreated] Could not query users:", err.message);
      }

      try {
        const donorsSnapshot = await db
          .collection("donors")
          .where("bloodGroup", "in", compatibleGroups)
          .get();
        donorDocs.push(...donorsSnapshot.docs);
      } catch (err) {
        console.warn("[onBloodRequestCreated] Could not query donors:", err.message);
      }

      if (donorDocs.length === 0) {
        console.log(`[onBloodRequestCreated] No candidate donors found for ${bloodGroup}`);
        return null;
      }

      const matchingTokens = [];
      const notificationPromises = [];

      for (const donorDoc of donorsSnapshot.docs) {
        const donor = donorDoc.data();
        const donorId = donorDoc.id;

        // 1. Exclude the patient who created the request
        if (patientId && (donor.userId === patientId || donorDoc.id === patientId)) {
          continue;
        }

        // 2. Check donor availability
        const isAvailable = donor.available !== false && donor.isAvailable !== false;
        if (!isAvailable) {
          continue;
        }

        // 3. Check location sharing permission
        if (donor.locationSharingEnabled === false) {
          continue;
        }

        // 4. Check notification preferences
        if (donor.bloodRequestNotificationsEnabled === false) {
          continue;
        }
        if (urgency.toLowerCase() === "emergency" && donor.emergencyNotificationsEnabled === false) {
          continue;
        }

        // 5. Calculate geospatial distance
        const donorLat = donor.latitude;
        const donorLng = donor.longitude;
        const distKm = calculateHaversineDistanceKm(reqLat, reqLng, donorLat, donorLng);
        const allowedRadius = donor.notificationRadiusKm || DEFAULT_NOTIFICATION_RADIUS_KM;

        if (distKm > allowedRadius) {
          continue;
        }

        // 6. Check for valid FCM token
        const fcmToken = donor.fcmToken;
        if (!fcmToken || typeof fcmToken !== "string" || fcmToken.length < 10) {
          continue;
        }

        // 7. Anti-Spam protection: Check if donor was already notified for this request
        const existingAlertQuery = await db
          .collection("notifications")
          .where("donorId", "==", donorId)
          .where("requestId", "==", requestId)
          .limit(1)
          .get();

        if (!existingAlertQuery.empty) {
          console.log(`[onBloodRequestCreated] Donor ${donorId} was already notified for ${requestId}. Skipping.`);
          continue;
        }

        // Add to batch notification list
        matchingTokens.push(fcmToken);

        // Record notification in Firestore for history and anti-spam
        const notificationRecord = {
          donorId: donorId,
          requestId: requestId,
          type: "blood_request",
          title: urgency.toLowerCase() === "emergency" ? "🚨 Emergency Blood Request" : "🩸 Blood Needed Near You",
          body: `${bloodGroup} blood is urgently needed near ${hospitalName}.\n${unitsRequired} unit(s) required. (~${distKm.toFixed(1)} km away)`,
          urgency: urgency,
          bloodGroup: bloodGroup,
          hospitalName: hospitalName,
          distanceKm: parseFloat(distKm.toFixed(1)),
          sentAt: Date.now(),
          read: false
        };

        notificationPromises.push(
          db.collection("notifications").add(notificationRecord)
        );
      }

      await Promise.all(notificationPromises);

      if (matchingTokens.length === 0) {
        console.log(`[onBloodRequestCreated] No eligible donors within radius for request ${requestId}`);
        return null;
      }

      console.log(`[onBloodRequestCreated] Dispatching FCM push to ${matchingTokens.length} matching donors`);

      // Dispatch FCM Push Notification
      const isEmergency = urgency.toLowerCase() === "emergency";
      const messageTitle = isEmergency ? "🚨 Emergency Blood Request Near You" : "🩸 Blood Needed Near You";
      const messageBody = `${bloodGroup} blood is urgently needed near ${hospitalName}.\n${unitsRequired} unit(s) required.`;

      // Use multicast to deliver securely
      const multicastMessage = {
        tokens: matchingTokens,
        notification: {
          title: messageTitle,
          body: messageBody
        },
        data: {
          type: "blood_request",
          requestId: String(requestId),
          bloodGroup: String(bloodGroup),
          urgency: String(urgency),
          hospitalName: String(hospitalName)
        },
        android: {
          priority: "high",
          notification: {
            channelId: "blood_requests_channel_v3",
            priority: "max",
            defaultSound: true,
            defaultVibrateTimings: true
          }
        }
      };

      const response = await messaging.sendEachForMulticast(multicastMessage);
      console.log(`[onBloodRequestCreated] Sent ${response.successCount} FCM messages successfully; ${response.failureCount} failed.`);

      return { successCount: response.successCount, failureCount: response.failureCount };
    } catch (err) {
      console.error(`[onBloodRequestCreated] Error processing request ${requestId}:`, err);
      return null;
    }
  });

/**
 * Cloud Function: onBloodRequestResponseCreated
 * Triggers when a donor clicks "I Can Donate" and creates a record in `bloodRequestResponses/{responseId}`
 * Notifies patient and hospital that a donor has accepted.
 */
exports.onBloodRequestResponseCreated = functions.firestore
  .document("bloodRequestResponses/{responseId}")
  .onCreate(async (snap, context) => {
    const responseId = context.params.responseId;
    const responseData = snap.data();

    if (!responseData) return null;

    const requestId = responseData.requestId;
    const donorId = responseData.donorId;
    const status = responseData.status || "accepted";

    if (status !== "accepted") {
      return null;
    }

    try {
      // 1. Fetch blood request to find patient and hospital
      const requestDoc = await db.collection("bloodRequests").doc(String(requestId)).get();
      if (!requestDoc.exists) {
        console.log(`[onBloodRequestResponseCreated] Request ${requestId} not found.`);
        return null;
      }

      const request = requestDoc.data();
      const patientId = request.patientId;
      const patientName = request.patientName || "Patient";
      const hospitalName = request.hospitalName || "Hospital";
      const bloodGroup = request.bloodGroup || "";

      // 2. Fetch donor details for approximate notification
      let donorName = "A verified donor";
      const donorDoc = await db.collection("donors").doc(String(donorId)).get();
      if (donorDoc.exists) {
        const donor = donorDoc.data();
        donorName = donor.fullName || donor.userName || "A verified donor";
      }

      // 3. Find patient FCM token
      if (patientId) {
        const userDoc = await db.collection("users").doc(String(patientId)).get();
        if (userDoc.exists) {
          const userData = userDoc.data();
          const patientToken = userData.fcmToken;

          if (patientToken) {
            await messaging.send({
              token: patientToken,
              notification: {
                title: "✅ Donor Responded to Blood Request!",
                body: `${donorName} has responded to your ${bloodGroup} request for ${hospitalName}. Tap to view contact details.`
              },
              data: {
                type: "donor_accepted",
                requestId: String(requestId),
                donorId: String(donorId),
                bloodGroup: String(bloodGroup)
              },
              android: {
                priority: "high",
                notification: {
                  channelId: "blood_requests_channel_v3",
                  priority: "max",
                  defaultSound: true
                }
              }
            });
            console.log(`[onBloodRequestResponseCreated] Sent acceptance notification to patient ${patientId}`);
          }
        }
      }

      // 4. Save notification record for patient
      await db.collection("notifications").add({
        recipientId: patientId,
        requestId: requestId,
        type: "donor_accepted",
        title: "✅ Hero Donor Accepted Request",
        body: `${donorName} accepted emergency request for ${bloodGroup} at ${hospitalName}.`,
        sentAt: Date.now(),
        read: false
      });

      return { responseId, notified: true };
    } catch (err) {
      console.error(`[onBloodRequestResponseCreated] Error handling response ${responseId}:`, err);
      return null;
    }
  });

/**
 * Cloud Function: onBloodRequestStatusUpdated
 * Notifies participants when request status changes (e.g. "fulfilled" or "closed")
 */
exports.onBloodRequestStatusUpdated = functions.firestore
  .document("bloodRequests/{requestId}")
  .onUpdate(async (change, context) => {
    const requestId = context.params.requestId;
    const before = change.before.data();
    const after = change.after.data();

    if (!before || !after) return null;

    if (before.status !== after.status && (after.status === "fulfilled" || after.status === "closed")) {
      console.log(`[onBloodRequestStatusUpdated] Request ${requestId} closed with status ${after.status}`);
      // Record status change event
      await db.collection("notifications").add({
        requestId: requestId,
        type: "request_closed",
        title: "Request Closed",
        body: `Blood request for ${after.bloodGroup} at ${after.hospitalName} has been ${after.status}.`,
        sentAt: Date.now(),
        read: false
      });
    }
    return null;
  });
