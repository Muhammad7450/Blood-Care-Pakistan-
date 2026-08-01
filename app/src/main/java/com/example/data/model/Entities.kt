package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donors")
data class DonorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val phone: String,
    val bloodGroup: String, // A+, A-, B+, B-, AB+, AB-, O+, O-
    val age: Int,
    val gender: String,
    val city: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val isAvailable: Boolean = true,
    val lastDonationDate: String = "Never",
    val totalDonations: Int = 0,
    val isVerified: Boolean = true,
    val badges: String = "Hero Donor,Life Saver",
    val avatarUrl: String = ""
)

@Entity(tableName = "blood_requests")
data class BloodRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientName: String,
    val bloodGroup: String,
    val units: Int,
    val hospitalName: String,
    val city: String,
    val locationName: String,
    val emergencyLevel: String, // Normal, Urgent, Critical
    val requiredDateTime: String,
    val contactPhone: String,
    val notes: String,
    val status: String = "Pending", // Pending, Matched, Fulfilled, Cancelled
    val createdAt: Long = System.currentTimeMillis(),
    val isThalassemia: Boolean = false,
    val requesterRole: String = "Patient" // Patient, Hospital
)

@Entity(tableName = "hospitals")
data class HospitalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val city: String,
    val address: String,
    val phone: String,
    val isVerified: Boolean = true,
    val aPosUnits: Int = 12,
    val aNegUnits: Int = 4,
    val bPosUnits: Int = 18,
    val bNegUnits: Int = 3,
    val abPosUnits: Int = 8,
    val abNegUnits: Int = 2,
    val oPosUnits: Int = 25,
    val oNegUnits: Int = 6
)

@Entity(tableName = "donation_records")
data class DonationRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val donorId: Int,
    val donorName: String,
    val patientName: String,
    val hospitalName: String,
    val bloodGroup: String,
    val unitsDonated: Int,
    val donationDate: String,
    val certificateCode: String
)

@Entity(tableName = "blood_camps")
data class BloodCampEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val organizer: String,
    val city: String,
    val venue: String,
    val date: String,
    val time: String,
    val contact: String,
    val registeredCount: Int = 0
)
