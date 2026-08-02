package com.example.data.repository

import com.example.data.local.BloodCampDao
import com.example.data.local.BloodRequestDao
import com.example.data.local.DonationRecordDao
import com.example.data.local.DonorDao
import com.example.data.local.HospitalDao
import com.example.data.model.BloodCampEntity
import com.example.data.model.BloodRequestEntity
import com.example.data.model.DonationRecordEntity
import com.example.data.model.DonorEntity
import com.example.data.model.HospitalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class BloodCareRepository(
    private val donorDao: DonorDao,
    private val requestDao: BloodRequestDao,
    private val hospitalDao: HospitalDao,
    private val donationRecordDao: DonationRecordDao,
    private val campDao: BloodCampDao
) {
    val allDonors: Flow<List<DonorEntity>> = donorDao.getAllDonors()
    val allRequests: Flow<List<BloodRequestEntity>> = requestDao.getAllRequests()
    val emergencyRequests: Flow<List<BloodRequestEntity>> = requestDao.getEmergencyRequests()
    val thalassemiaRequests: Flow<List<BloodRequestEntity>> = requestDao.getThalassemiaRequests()
    val allHospitals: Flow<List<HospitalEntity>> = hospitalDao.getAllHospitals()
    val allRecords: Flow<List<DonationRecordEntity>> = donationRecordDao.getAllRecords()
    val allCamps: Flow<List<BloodCampEntity>> = campDao.getAllCamps()

    suspend fun getDonorById(id: Int) = donorDao.getDonorById(id)
    suspend fun addDonor(donor: DonorEntity) = donorDao.insertDonor(donor)
    suspend fun updateDonor(donor: DonorEntity) = donorDao.updateDonor(donor)
    suspend fun updateDonorAvailability(id: Int, isAvailable: Boolean) = donorDao.updateAvailability(id, isAvailable)
    suspend fun deleteDonor(id: Int) = donorDao.deleteDonor(id)

    suspend fun getRequestById(id: Int) = requestDao.getRequestById(id)
    suspend fun addBloodRequest(request: BloodRequestEntity) = requestDao.insertRequest(request)
    suspend fun updateRequest(request: BloodRequestEntity) = requestDao.updateRequest(request)
    suspend fun updateRequestStatus(id: Int, status: String) = requestDao.updateRequestStatus(id, status)
    suspend fun deleteRequest(id: Int) = requestDao.deleteRequest(id)

    suspend fun addHospital(hospital: HospitalEntity) = hospitalDao.insertHospital(hospital)
    suspend fun updateHospitalVerification(id: Int, isVerified: Boolean) = hospitalDao.updateHospitalVerification(id, isVerified)

    suspend fun addDonationRecord(record: DonationRecordEntity) = donationRecordDao.insertRecord(record)
    fun getRecordsForDonor(donorId: Int) = donationRecordDao.getRecordsForDonor(donorId)

    suspend fun registerForCamp(campId: Int) = campDao.registerForCamp(campId)

    // Smart Blood Group Compatibility Checker
    fun isCompatible(donorGroup: String, patientGroup: String): Boolean {
        val compatibleDonorsForPatient = when (patientGroup.trim().uppercase()) {
            "O-" -> listOf("O-")
            "O+" -> listOf("O+", "O-")
            "A-" -> listOf("A-", "O-")
            "A+" -> listOf("A+", "A-", "O+", "O-")
            "B-" -> listOf("B-", "O-")
            "B+" -> listOf("B+", "B-", "O+", "O-")
            "AB-" -> listOf("AB-", "A-", "B-", "O-")
            "AB+" -> listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
            else -> listOf(donorGroup)
        }
        return compatibleDonorsForPatient.contains(donorGroup.trim().uppercase())
    }

    // Get Matched Donors for a request
    fun getMatchedDonors(patientBloodGroup: String, city: String): Flow<List<DonorEntity>> {
        return donorDao.getAllDonors().map { donors ->
            donors.filter { donor ->
                donor.isAvailable &&
                        isCompatible(donor.bloodGroup, patientBloodGroup) &&
                        (city.isBlank() || donor.city.equals(city, ignoreCase = true))
            }.sortedByDescending { it.totalDonations }
        }
    }

    // Calculate approximate distance between two GPS coordinates (Haversine Formula) in KM
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
