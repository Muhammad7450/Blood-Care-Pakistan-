package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class NotificationRecord(
    @DocumentId val id: String = "",
    val recipientUserId: String = "",
    val requestId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "NEW_REQUEST", // NEW_REQUEST, DONOR_RESPONDED, STATUS_UPDATE
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "recipientUserId" to recipientUserId,
        "requestId" to requestId,
        "title" to title,
        "body" to body,
        "type" to type,
        "isRead" to isRead
    )

    companion object {
        fun fromDocument(doc: DocumentSnapshot): NotificationRecord? {
            if (!doc.exists()) return null
            return try {
                doc.toObject(NotificationRecord::class.java)?.copy(id = doc.id)
            } catch (e: Exception) {
                val data = doc.data ?: return null
                NotificationRecord(
                    id = doc.id,
                    recipientUserId = data["recipientUserId"] as? String ?: "",
                    requestId = data["requestId"] as? String ?: "",
                    title = data["title"] as? String ?: "Emergency Blood Alert",
                    body = data["body"] as? String ?: "",
                    type = data["type"] as? String ?: "NEW_REQUEST",
                    isRead = data["isRead"] as? Boolean ?: false,
                    createdAt = data["createdAt"] as? Timestamp
                )
            }
        }
    }
}
