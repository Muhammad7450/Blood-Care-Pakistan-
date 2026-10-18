package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class RequestUrgency {
    NORMAL,
    URGENT,
    CRITICAL;

    companion object {
        fun fromString(value: String?): RequestUrgency {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: URGENT
        }
    }
}

enum class RequestStatus {
    OPEN,
    DONOR_RESPONDED,
    FULFILLED,
    CANCELLED;

    companion object {
        fun fromString(value: String?): RequestStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OPEN
        }
    }
}

@IgnoreExtraProperties
data class BloodRequest(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val patientPhone: String = "",
    val bloodGroup: String = "A+",
    val city: String = "Nawabshah",
    val normalizedCity: String = "nawabshah",
    val hospitalName: String = "",
    val hospitalAddress: String = "",
    val unitsRequired: Long = 1L,
    val urgency: String = RequestUrgency.URGENT.name,
    val requiredDate: String = "",
    val additionalNotes: String = "",
    val status: String = RequestStatus.OPEN.name,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    @ServerTimestamp val fulfilledAt: Timestamp? = null,
    @ServerTimestamp val cancelledAt: Timestamp? = null,
    @ServerTimestamp val deletedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "patientId" to patientId,
        "patientName" to patientName,
        "patientPhone" to patientPhone,
        "bloodGroup" to bloodGroup,
        "city" to city,
        "normalizedCity" to normalizedCity,
        "hospitalName" to hospitalName,
        "hospitalAddress" to hospitalAddress,
        "unitsRequired" to unitsRequired,
        "urgency" to urgency,
        "requiredDate" to requiredDate,
        "additionalNotes" to additionalNotes,
        "status" to status,
        "latitude" to latitude,
        "longitude" to longitude
    )

    companion object {
        fun fromDocument(doc: DocumentSnapshot): BloodRequest? {
            if (!doc.exists()) return null
            return try {
                doc.toObject(BloodRequest::class.java)?.copy(id = doc.id)
            } catch (e: Exception) {
                // Defensive fallback for numeric type mismatches (e.g. Long vs Double)
                val data = doc.data ?: return null
                val lat = (data["latitude"] as? Number)?.toDouble()
                    ?: (data["latitude"] as? String)?.toDoubleOrNull()
                    ?: 0.0
                val lng = (data["longitude"] as? Number)?.toDouble()
                    ?: (data["longitude"] as? String)?.toDoubleOrNull()
                    ?: 0.0
                val units = (data["unitsRequired"] as? Number)?.toLong()
                    ?: (data["unitsRequired"] as? String)?.toLongOrNull()
                    ?: 1L

                BloodRequest(
                    id = doc.id,
                    patientId = data["patientId"] as? String ?: "",
                    patientName = data["patientName"] as? String ?: "",
                    patientPhone = data["patientPhone"] as? String ?: "",
                    bloodGroup = data["bloodGroup"] as? String ?: "A+",
                    city = data["city"] as? String ?: "Nawabshah",
                    normalizedCity = data["normalizedCity"] as? String ?: "nawabshah",
                    hospitalName = data["hospitalName"] as? String ?: "",
                    hospitalAddress = data["hospitalAddress"] as? String ?: "",
                    unitsRequired = units,
                    urgency = data["urgency"] as? String ?: RequestUrgency.URGENT.name,
                    requiredDate = data["requiredDate"] as? String ?: "",
                    additionalNotes = data["additionalNotes"] as? String ?: "",
                    status = data["status"] as? String ?: RequestStatus.OPEN.name,
                    latitude = lat,
                    longitude = lng,
                    createdAt = data["createdAt"] as? Timestamp,
                    updatedAt = data["updatedAt"] as? Timestamp,
                    fulfilledAt = data["fulfilledAt"] as? Timestamp,
                    cancelledAt = data["cancelledAt"] as? Timestamp,
                    deletedAt = data["deletedAt"] as? Timestamp
                )
            }
        }
    }
}
