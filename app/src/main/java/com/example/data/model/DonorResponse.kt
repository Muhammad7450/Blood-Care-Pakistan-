package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ResponseStatus {
    ACCEPTED,
    CONFIRMED,
    COMPLETED,
    DECLINED;

    companion object {
        fun fromString(value: String?): ResponseStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ACCEPTED
        }
    }
}

@IgnoreExtraProperties
data class DonorResponse(
    @DocumentId val id: String = "",
    val requestId: String = "",
    val donorId: String = "",
    val patientId: String = "",
    val donorName: String = "",
    val donorPhone: String = "",
    val donorBloodGroup: String = "A+",
    val responseStatus: String = ResponseStatus.ACCEPTED.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "requestId" to requestId,
        "donorId" to donorId,
        "patientId" to patientId,
        "donorName" to donorName,
        "donorPhone" to donorPhone,
        "donorBloodGroup" to donorBloodGroup,
        "responseStatus" to responseStatus
    )

    companion object {
        fun fromDocument(doc: DocumentSnapshot): DonorResponse? {
            if (!doc.exists()) return null
            return try {
                doc.toObject(DonorResponse::class.java)?.copy(id = doc.id)
            } catch (e: Exception) {
                val data = doc.data ?: return null
                DonorResponse(
                    id = doc.id,
                    requestId = data["requestId"] as? String ?: "",
                    donorId = data["donorId"] as? String ?: "",
                    patientId = data["patientId"] as? String ?: "",
                    donorName = data["donorName"] as? String ?: "Donor",
                    donorPhone = data["donorPhone"] as? String ?: "",
                    donorBloodGroup = data["donorBloodGroup"] as? String ?: "A+",
                    responseStatus = data["responseStatus"] as? String ?: ResponseStatus.ACCEPTED.name,
                    createdAt = data["createdAt"] as? Timestamp,
                    updatedAt = data["updatedAt"] as? Timestamp
                )
            }
        }
    }
}
