package com.example.data.remote

import android.util.Log
import com.example.data.model.BloodRequestEntity
import com.example.data.model.DonorEntity
import com.example.ui.util.PakistanCities
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

object FirestoreService {
    private const val TAG = "FirestoreService"

    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_DONORS = "donors"
    private const val COLLECTION_REQUESTS = "blood_requests"

    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FirebaseFirestore: ${e.message}")
            null
        }
    }

    // Save or update user profile & donor location in Firestore
    fun saveUserProfile(
        phone: String,
        name: String,
        role: String,
        bloodGroup: String,
        city: String,
        locationName: String = "",
        isAvailable: Boolean = true
    ) {
        val firestore = db ?: return
        if (phone.isBlank()) return

        val coords = PakistanCities.getCityCoords(city)

        val userMap = hashMapOf<String, Any>(
            "phone" to phone,
            "fullName" to name,
            "role" to role,
            "bloodGroup" to bloodGroup,
            "city" to city,
            "locationName" to locationName,
            "latitude" to coords.first,
            "longitude" to coords.second,
            "isAvailable" to isAvailable,
            "updatedAt" to System.currentTimeMillis()
        )

        try {
            firestore.collection(COLLECTION_USERS)
                .document(phone)
                .set(userMap, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "User profile saved to Firestore for $phone")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error saving user profile to Firestore", e)
                }

            if (role.equals("Donor", ignoreCase = true)) {
                firestore.collection(COLLECTION_DONORS)
                    .document(phone)
                    .set(userMap, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "Donor location saved to Firestore for $phone")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error saving donor to Firestore", e)
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore exception saving user profile: ${e.message}")
        }
    }

    // Save or update donor entity in Firestore
    fun saveDonorEntity(donor: DonorEntity) {
        val firestore = db ?: return
        val docId = if (donor.phone.isNotBlank()) donor.phone else "donor_${donor.id}"

        val donorMap = hashMapOf<String, Any>(
            "id" to donor.id,
            "fullName" to donor.fullName,
            "phone" to donor.phone,
            "bloodGroup" to donor.bloodGroup,
            "age" to donor.age,
            "gender" to donor.gender,
            "city" to donor.city,
            "locationName" to donor.locationName,
            "latitude" to donor.latitude,
            "longitude" to donor.longitude,
            "isAvailable" to donor.isAvailable,
            "lastDonationDate" to donor.lastDonationDate,
            "totalDonations" to donor.totalDonations,
            "isVerified" to donor.isVerified,
            "badges" to donor.badges,
            "updatedAt" to System.currentTimeMillis()
        )

        try {
            firestore.collection(COLLECTION_DONORS)
                .document(docId)
                .set(donorMap, SetOptions.merge())
        } catch (e: Exception) {
            Log.e(TAG, "Firestore exception saving donor entity: ${e.message}")
        }
    }

    // Save or update Blood Request in Firestore
    fun saveBloodRequest(request: BloodRequestEntity, onComplete: ((String?) -> Unit)? = null) {
        val firestore = db ?: run {
            onComplete?.invoke(null)
            return
        }

        val docId = if (request.id != 0) request.id.toString() else "req_${System.currentTimeMillis()}"

        val requestMap = hashMapOf<String, Any>(
            "id" to request.id,
            "patientName" to request.patientName,
            "bloodGroup" to request.bloodGroup,
            "units" to request.units,
            "hospitalName" to request.hospitalName,
            "city" to request.city,
            "locationName" to request.locationName,
            "emergencyLevel" to request.emergencyLevel,
            "requiredDateTime" to request.requiredDateTime,
            "contactPhone" to request.contactPhone,
            "notes" to request.notes,
            "status" to request.status,
            "isThalassemia" to request.isThalassemia,
            "requesterRole" to request.requesterRole,
            "latitude" to request.latitude,
            "longitude" to request.longitude,
            "createdAt" to request.createdAt,
            "acceptedDonorId" to (request.acceptedDonorId ?: 0),
            "acceptedDonorName" to (request.acceptedDonorName ?: ""),
            "acceptedDonorPhone" to (request.acceptedDonorPhone ?: ""),
            "acceptedDonorLocation" to (request.acceptedDonorLocation ?: ""),
            "acceptedDonorDistanceKm" to (request.acceptedDonorDistanceKm ?: 0.0),
            "acceptedAt" to (request.acceptedAt ?: "")
        )

        try {
            firestore.collection(COLLECTION_REQUESTS)
                .document(docId)
                .set(requestMap, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Blood request saved to Firestore: $docId")
                    onComplete?.invoke(docId)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error saving blood request to Firestore", e)
                    onComplete?.invoke(null)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore exception saving blood request: ${e.message}")
            onComplete?.invoke(null)
        }
    }

    // Update request status (e.g., when accepted or fulfilled)
    fun updateRequestStatus(
        docId: String,
        status: String,
        acceptedDonorName: String = "",
        acceptedDonorPhone: String = "",
        acceptedDonorLocation: String = "",
        acceptedDonorDistanceKm: Double = 0.0,
        acceptedAt: String = ""
    ) {
        val firestore = db ?: return
        val updates = hashMapOf<String, Any>(
            "status" to status
        )
        if (acceptedDonorName.isNotBlank()) {
            updates["acceptedDonorName"] = acceptedDonorName
            updates["acceptedDonorPhone"] = acceptedDonorPhone
            updates["acceptedDonorLocation"] = acceptedDonorLocation
            updates["acceptedDonorDistanceKm"] = acceptedDonorDistanceKm
            updates["acceptedAt"] = acceptedAt
        }

        try {
            firestore.collection(COLLECTION_REQUESTS)
                .document(docId)
                .update(updates)
        } catch (e: Exception) {
            Log.e(TAG, "Firestore exception updating request status: ${e.message}")
        }
    }

    // Listen in real time to all blood requests
    fun listenToBloodRequests(
        onRequestsUpdated: (List<BloodRequestEntity>) -> Unit
    ): ListenerRegistration? {
        val firestore = db ?: return null
        return try {
            firestore.collection(COLLECTION_REQUESTS)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed for blood_requests", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val requests = mutableListOf<BloodRequestEntity>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.getLong("id") ?: 0L).toInt()
                                val patientName = doc.getString("patientName") ?: ""
                                val bloodGroup = doc.getString("bloodGroup") ?: ""
                                val units = (doc.getLong("units") ?: 1L).toInt()
                                val hospitalName = doc.getString("hospitalName") ?: ""
                                val city = doc.getString("city") ?: ""
                                val locationName = doc.getString("locationName") ?: ""
                                val emergencyLevel = doc.getString("emergencyLevel") ?: "Urgent"
                                val requiredDateTime = doc.getString("requiredDateTime") ?: ""
                                val contactPhone = doc.getString("contactPhone") ?: ""
                                val notes = doc.getString("notes") ?: ""
                                val status = doc.getString("status") ?: "Pending"
                                val isThalassemia = doc.getBoolean("isThalassemia") ?: false
                                val requesterRole = doc.getString("requesterRole") ?: "Patient"
                                val latitude = doc.getDouble("latitude") ?: 0.0
                                val longitude = doc.getDouble("longitude") ?: 0.0
                                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                val acceptedDonorId = (doc.getLong("acceptedDonorId") ?: 0L).toInt()
                                val acceptedDonorName = doc.getString("acceptedDonorName") ?: ""
                                val acceptedDonorPhone = doc.getString("acceptedDonorPhone") ?: ""
                                val acceptedDonorLocation = doc.getString("acceptedDonorLocation") ?: ""
                                val acceptedDonorDistanceKm = doc.getDouble("acceptedDonorDistanceKm") ?: 0.0
                                val acceptedAt = doc.getString("acceptedAt") ?: ""

                                if (patientName.isNotBlank() && bloodGroup.isNotBlank()) {
                                    requests.add(
                                        BloodRequestEntity(
                                            id = id,
                                            patientName = patientName,
                                            bloodGroup = bloodGroup,
                                            units = units,
                                            hospitalName = hospitalName,
                                            city = city,
                                            locationName = locationName,
                                            emergencyLevel = emergencyLevel,
                                            requiredDateTime = requiredDateTime,
                                            contactPhone = contactPhone,
                                            notes = notes,
                                            status = status,
                                            isThalassemia = isThalassemia,
                                            requesterRole = requesterRole,
                                            latitude = latitude,
                                            longitude = longitude,
                                            createdAt = createdAt,
                                            acceptedDonorId = acceptedDonorId,
                                            acceptedDonorName = acceptedDonorName,
                                            acceptedDonorPhone = acceptedDonorPhone,
                                            acceptedDonorLocation = acceptedDonorLocation,
                                            acceptedDonorDistanceKm = acceptedDonorDistanceKm,
                                            acceptedAt = acceptedAt
                                        )
                                    )
                                }
                            } catch (err: Exception) {
                                Log.e(TAG, "Error parsing blood request doc ${doc.id}", err)
                            }
                        }
                        onRequestsUpdated(requests)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating blood requests listener: ${e.message}")
            null
        }
    }

    // Listen in real time to all donors & locations
    fun listenToDonors(
        onDonorsUpdated: (List<DonorEntity>) -> Unit
    ): ListenerRegistration? {
        val firestore = db ?: return null
        return try {
            firestore.collection(COLLECTION_DONORS)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed for donors", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val donors = mutableListOf<DonorEntity>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = (doc.getLong("id") ?: 0L).toInt()
                                val fullName = doc.getString("fullName") ?: ""
                                val phone = doc.getString("phone") ?: doc.id
                                val bloodGroup = doc.getString("bloodGroup") ?: ""
                                val age = (doc.getLong("age") ?: 25L).toInt()
                                val gender = doc.getString("gender") ?: "Male"
                                val city = doc.getString("city") ?: ""
                                val locationName = doc.getString("locationName") ?: ""
                                val latitude = doc.getDouble("latitude") ?: 0.0
                                val longitude = doc.getDouble("longitude") ?: 0.0
                                val isAvailable = doc.getBoolean("isAvailable") ?: true
                                val lastDonationDate = doc.getString("lastDonationDate") ?: "Never"
                                val totalDonations = (doc.getLong("totalDonations") ?: 0L).toInt()
                                val isVerified = doc.getBoolean("isVerified") ?: true
                                val badges = doc.getString("badges") ?: "Hero Donor"

                                if (fullName.isNotBlank() && bloodGroup.isNotBlank()) {
                                    donors.add(
                                        DonorEntity(
                                            id = id,
                                            fullName = fullName,
                                            phone = phone,
                                            bloodGroup = bloodGroup,
                                            age = age,
                                            gender = gender,
                                            city = city,
                                            locationName = locationName,
                                            latitude = latitude,
                                            longitude = longitude,
                                            isAvailable = isAvailable,
                                            lastDonationDate = lastDonationDate,
                                            totalDonations = totalDonations,
                                            isVerified = isVerified,
                                            badges = badges
                                        )
                                    )
                                }
                            } catch (err: Exception) {
                                Log.e(TAG, "Error parsing donor doc ${doc.id}", err)
                            }
                        }
                        onDonorsUpdated(donors)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating donors listener: ${e.message}")
            null
        }
    }
}
