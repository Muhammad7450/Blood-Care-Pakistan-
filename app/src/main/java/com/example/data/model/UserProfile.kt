package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class UserRole {
    DONOR,
    PATIENT,
    ADMIN,
    BOTH;

    companion object {
        fun fromString(value: String?): UserRole {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DONOR
        }
    }
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val uid: String = "",
    val fullName: String = "",
    val role: String = UserRole.DONOR.name,
    val supportedCapabilities: List<String> = emptyList(),
    val bloodGroup: String = "A+",
    val city: String = "Nawabshah",
    val normalizedCity: String = "nawabshah",
    val phoneNumber: String = "",
    val isDonor: Boolean = true,
    val isAvailable: Boolean = true,
    val isProfileVerified: Boolean = false,
    val fcmToken: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "fullName" to fullName,
        "role" to role,
        "supportedCapabilities" to supportedCapabilities,
        "bloodGroup" to bloodGroup,
        "city" to city,
        "normalizedCity" to normalizedCity,
        "phoneNumber" to phoneNumber,
        "isDonor" to isDonor,
        "isAvailable" to isAvailable,
        "isProfileVerified" to isProfileVerified,
        "fcmToken" to fcmToken
    )
}
