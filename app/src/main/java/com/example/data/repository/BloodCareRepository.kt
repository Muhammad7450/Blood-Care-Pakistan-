package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.BloodRequest
import com.example.data.model.CityUtils
import com.example.data.model.DonorResponse
import com.example.data.model.NotificationRecord
import com.example.data.model.RequestStatus
import com.example.data.model.RequestUrgency
import com.example.data.model.ResponseStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BloodCareRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google")
    }

    /**
     * Real-time query observing open blood requests in donor's normalized city.
     * Donors receive new requests instantaneously without switching modes or reloading.
     */
    fun observeCityBloodRequests(normalizedCity: String): Flow<List<BloodRequest>> = callbackFlow {
        val path = "bloodRequests"
        val query = if (normalizedCity.isNotBlank()) {
            db.collection(path)
                .whereEqualTo("normalizedCity", normalizedCity.lowercase())
                .whereEqualTo("status", RequestStatus.OPEN.name)
        } else {
            db.collection(path)
                .whereEqualTo("status", RequestStatus.OPEN.name)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val requests = try {
                    snapshot.documents.mapNotNull { BloodRequest.fromDocument(it) }
                } catch (e: Exception) {
                    emptyList()
                }
                trySend(requests)
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Real-time observation of a patient's own submitted requests.
     */
    fun observePatientRequests(patientId: String): Flow<List<BloodRequest>> = callbackFlow {
        val path = "bloodRequests"
        val query = db.collection(path)
            .whereEqualTo("patientId", patientId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val requests = try {
                    snapshot.documents.mapNotNull { BloodRequest.fromDocument(it) }
                        .filter { it.deletedAt == null }
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                } catch (e: Exception) {
                    emptyList()
                }
                trySend(requests)
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Real-time observation of a specific blood request by ID.
     */
    fun observeRequestById(requestId: String): Flow<BloodRequest?> = callbackFlow {
        val docRef = db.collection("bloodRequests").document(requestId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                trySend(null)
                return@addSnapshotListener
            }
            val request = try {
                if (snapshot != null && snapshot.exists()) BloodRequest.fromDocument(snapshot) else null
            } catch (e: Exception) {
                android.util.Log.e("BloodCareRepo", "Error parsing request in observeRequestById", e)
                null
            }
            trySend(request)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Real-time observation of donor responses for a request.
     */
    fun observeDonorResponses(requestId: String): Flow<List<DonorResponse>> = callbackFlow {
        val path = "donorResponses"
        val query = db.collection(path).whereEqualTo("requestId", requestId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val responses = try {
                    snapshot.documents.mapNotNull { DonorResponse.fromDocument(it) }
                } catch (e: Exception) {
                    emptyList()
                }
                trySend(responses)
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Real-time observation of notifications for the signed-in user.
     */
    fun observeUserNotifications(userId: String): Flow<List<NotificationRecord>> = callbackFlow {
        val path = "notifications"
        val query = db.collection(path)
            .whereEqualTo("recipientUserId", userId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = try {
                    snapshot.documents.mapNotNull { NotificationRecord.fromDocument(it) }
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                } catch (e: Exception) {
                    emptyList()
                }
                trySend(list)
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Real-time observation of all requests for Admin users.
     */
    fun observeAllRequestsAdmin(): Flow<List<BloodRequest>> = callbackFlow {
        val path = "bloodRequests"
        val query = db.collection(path)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = try {
                    snapshot.documents.mapNotNull { BloodRequest.fromDocument(it) }
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                } catch (e: Exception) {
                    emptyList()
                }
                trySend(list)
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Creates a new blood request and alerts matching donors in the normalized city.
     */
    suspend fun createBloodRequest(
        patientName: String,
        patientPhone: String,
        bloodGroup: String,
        city: String,
        hospitalName: String,
        hospitalAddress: String,
        unitsRequired: Long,
        urgency: RequestUrgency,
        requiredDate: String,
        additionalNotes: String,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ): Result<String> = withContext(Dispatchers.IO) {
        val patientId = requireUserId()
        val normalizedCity = CityUtils.normalizeCity(city)
        val (finalLat, finalLng) = if (latitude != 0.0 && longitude != 0.0) {
            Pair(latitude, longitude)
        } else {
            CityUtils.getCityCoordinates(city)
        }
        val docRef = db.collection("bloodRequests").document()
        val requestId = docRef.id

        val requestData = mapOf(
            "patientId" to patientId,
            "patientName" to patientName,
            "patientPhone" to patientPhone,
            "bloodGroup" to bloodGroup,
            "city" to city,
            "normalizedCity" to normalizedCity,
            "hospitalName" to hospitalName,
            "hospitalAddress" to hospitalAddress,
            "unitsRequired" to unitsRequired,
            "urgency" to urgency.name,
            "requiredDate" to requiredDate,
            "additionalNotes" to additionalNotes,
            "status" to RequestStatus.OPEN.name,
            "latitude" to finalLat,
            "longitude" to finalLng,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(requestData).await()

            // Find matching available donors in the same normalized city
            notifyEligibleDonorsInCity(
                requestId = requestId,
                bloodGroup = bloodGroup,
                city = city,
                normalizedCity = normalizedCity,
                hospitalName = hospitalName,
                unitsRequired = unitsRequired,
                urgency = urgency
            )

            Result.success(requestId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    private suspend fun notifyEligibleDonorsInCity(
        requestId: String,
        bloodGroup: String,
        city: String,
        normalizedCity: String,
        hospitalName: String,
        unitsRequired: Long,
        urgency: RequestUrgency
    ) {
        try {
            val donorsQuery = db.collection("users")
                .whereEqualTo("normalizedCity", normalizedCity)
                .whereEqualTo("isDonor", true)
                .whereEqualTo("isAvailable", true)
                .get()
                .await()

            val currentUid = auth.currentUser?.uid

            for (donorDoc in donorsQuery.documents) {
                val donorUid = donorDoc.getString("uid") ?: continue
                if (donorUid == currentUid) continue // Do not notify patient as donor

                val donorBloodGroup = donorDoc.getString("bloodGroup") ?: "UNKNOWN"
                if (!CityUtils.isBloodCompatible(donorBloodGroup, bloodGroup)) continue

                val notifRef = db.collection("notifications").document()
                val notificationData = mapOf(
                    "recipientUserId" to donorUid,
                    "requestId" to requestId,
                    "title" to "BLOODCARE PAKISTAN - ${urgency.name} BLOOD REQUEST",
                    "body" to "Blood Group: $bloodGroup | City: $city | Hospital: $hospitalName | Units: $unitsRequired",
                    "type" to "NEW_REQUEST",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                notifRef.set(notificationData).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "notifications")
        }
    }

    /**
     * Submits a donor response ("I Can Donate").
     * Validates that request is still OPEN, prevents duplicate responses,
     * updates request status, and notifies the patient.
     */
    suspend fun respondToRequest(
        requestId: String,
        patientId: String,
        donorName: String,
        donorPhone: String,
        donorBloodGroup: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val donorId = requireUserId()
        if (donorId == patientId) {
            return@withContext Result.failure(IllegalArgumentException("You cannot donate for your own request."))
        }

        // Check for existing response from this donor
        val existingResp = db.collection("donorResponses")
            .whereEqualTo("requestId", requestId)
            .whereEqualTo("donorId", donorId)
            .get()
            .await()

        if (!existingResp.isEmpty) {
            return@withContext Result.failure(IllegalStateException("You have already submitted a response for this request."))
        }

        val requestDoc = db.collection("bloodRequests").document(requestId).get().await()
        val currentStatus = requestDoc.getString("status")
        if (currentStatus != RequestStatus.OPEN.name) {
            return@withContext Result.failure(IllegalStateException("This request is no longer open for donation offers."))
        }

        val respRef = db.collection("donorResponses").document()
        val responseData = mapOf(
            "requestId" to requestId,
            "donorId" to donorId,
            "patientId" to patientId,
            "donorName" to donorName,
            "donorPhone" to donorPhone,
            "donorBloodGroup" to donorBloodGroup,
            "responseStatus" to ResponseStatus.ACCEPTED.name,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            respRef.set(responseData).await()

            // Update request status to DONOR_RESPONDED
            db.collection("bloodRequests").document(requestId).update(
                mapOf(
                    "status" to RequestStatus.DONOR_RESPONDED.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Notify patient that a donor has responded
            val patientNotifRef = db.collection("notifications").document()
            val patientNotifData = mapOf(
                "recipientUserId" to patientId,
                "requestId" to requestId,
                "title" to "DONOR FOUND - BloodCare Pakistan",
                "body" to "$donorName ($donorBloodGroup) has offered to donate! Phone: $donorPhone",
                "type" to "DONOR_RESPONDED",
                "isRead" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            patientNotifRef.set(patientNotifData).await()

            Result.success(respRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, respRef.path)
            Result.failure(e)
        }
    }

    /**
     * Updates request lifecycle status (FULFILLED, CANCELLED).
     */
    suspend fun updateRequestStatus(
        requestId: String,
        newStatus: RequestStatus
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = db.collection("bloodRequests").document(requestId)

        val updates = mutableMapOf<String, Any>(
            "status" to newStatus.name,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (newStatus == RequestStatus.FULFILLED) {
            updates["fulfilledAt"] = FieldValue.serverTimestamp()
        } else if (newStatus == RequestStatus.CANCELLED) {
            updates["cancelledAt"] = FieldValue.serverTimestamp()
        }

        try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Soft-deletes a request so it is removed from active views but preserved for audit.
     */
    suspend fun deleteRequest(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val docRef = db.collection("bloodRequests").document(requestId)
        try {
            docRef.update(
                mapOf(
                    "status" to RequestStatus.CANCELLED.name,
                    "deletedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val docRef = db.collection("notifications").document(notificationId)
        try {
            docRef.update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }
}
