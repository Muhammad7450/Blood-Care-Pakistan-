package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BloodCampEntity
import com.example.data.model.BloodRequestEntity
import com.example.data.model.DonationRecordEntity
import com.example.data.model.DonorEntity
import com.example.data.model.HospitalEntity
import com.example.data.repository.BloodCareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserSession(
    val isLoggedIn: Boolean = false,
    val role: String = "Donor", // Donor, Patient, Hospital, Admin
    val phoneNumber: String = "",
    val userName: String = "Guest User",
    val userBloodGroup: String = "O+",
    val userCity: String = "Lahore",
    val currentDonorId: Int = 1
)

class BloodCareViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = BloodCareRepository(
        donorDao = db.donorDao(),
        requestDao = db.bloodRequestDao(),
        hospitalDao = db.hospitalDao(),
        donationRecordDao = db.donationRecordDao(),
        campDao = db.bloodCampDao()
    )

    private val _userSession = MutableStateFlow(
        UserSession(
            isLoggedIn = true,
            role = "Donor",
            phoneNumber = "+92 300 1234567",
            userName = "Muhammad Ali Khan",
            userBloodGroup = "O+",
            userCity = "Lahore",
            currentDonorId = 1
        )
    )
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    val allDonors: StateFlow<List<DonorEntity>> = repository.allDonors.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allRequests: StateFlow<List<BloodRequestEntity>> = repository.allRequests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val emergencyRequests: StateFlow<List<BloodRequestEntity>> = repository.emergencyRequests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val thalassemiaRequests: StateFlow<List<BloodRequestEntity>> = repository.thalassemiaRequests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allHospitals: StateFlow<List<HospitalEntity>> = repository.allHospitals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allRecords: StateFlow<List<DonationRecordEntity>> = repository.allRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCamps: StateFlow<List<BloodCampEntity>> = repository.allCamps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filter states
    val selectedBloodGroupFilter = MutableStateFlow("All")
    val selectedCityFilter = MutableStateFlow("All")

    // Methods
    fun loginUser(phone: String, name: String, bloodGroup: String, city: String, role: String) {
        _userSession.value = UserSession(
            isLoggedIn = true,
            role = role,
            phoneNumber = phone,
            userName = name,
            userBloodGroup = bloodGroup,
            userCity = city,
            currentDonorId = 1
        )
    }

    fun selectRole(role: String) {
        _userSession.value = _userSession.value.copy(role = role)
    }

    fun toggleDonorAvailability(donorId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.updateDonorAvailability(donorId, !currentStatus)
        }
    }

    fun createBloodRequest(
        patientName: String,
        bloodGroup: String,
        units: Int,
        hospitalName: String,
        city: String,
        locationName: String,
        emergencyLevel: String,
        requiredDateTime: String,
        notes: String,
        isThalassemia: Boolean = false
    ) {
        viewModelScope.launch {
            val request = BloodRequestEntity(
                patientName = patientName,
                bloodGroup = bloodGroup,
                units = units,
                hospitalName = hospitalName,
                city = city,
                locationName = locationName,
                emergencyLevel = emergencyLevel,
                requiredDateTime = requiredDateTime,
                contactPhone = _userSession.value.phoneNumber,
                notes = notes,
                status = "Pending",
                isThalassemia = isThalassemia,
                requesterRole = _userSession.value.role
            )
            repository.addBloodRequest(request)
        }
    }

    fun registerDonor(
        name: String,
        phone: String,
        bloodGroup: String,
        age: Int,
        gender: String,
        city: String,
        locationName: String
    ) {
        viewModelScope.launch {
            val donor = DonorEntity(
                fullName = name,
                phone = phone,
                bloodGroup = bloodGroup,
                age = age,
                gender = gender,
                city = city,
                locationName = locationName,
                latitude = 31.5204,
                longitude = 74.3587,
                isAvailable = true,
                lastDonationDate = "Never",
                totalDonations = 0,
                isVerified = true,
                badges = "New Hero,Life Saver"
            )
            repository.addDonor(donor)
        }
    }

    fun updateRequestStatus(requestId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateRequestStatus(requestId, newStatus)
        }
    }

    fun recordDonation(donorId: Int, donorName: String, patientName: String, hospitalName: String, bloodGroup: String) {
        viewModelScope.launch {
            val code = "BCP-${System.currentTimeMillis().toString().takeLast(4)}-${(1000..9999).random()}"
            val record = DonationRecordEntity(
                donorId = donorId,
                donorName = donorName,
                patientName = patientName,
                hospitalName = hospitalName,
                bloodGroup = bloodGroup,
                unitsDonated = 1,
                donationDate = "Today",
                certificateCode = code
            )
            repository.addDonationRecord(record)

            val donor = repository.getDonorById(donorId)
            if (donor != null) {
                val updated = donor.copy(
                    totalDonations = donor.totalDonations + 1,
                    lastDonationDate = "Today"
                )
                repository.updateDonor(updated)
            }
        }
    }

    fun registerForCamp(campId: Int) {
        viewModelScope.launch {
            repository.registerForCamp(campId)
        }
    }

    fun toggleHospitalVerification(hospitalId: Int, currentVerified: Boolean) {
        viewModelScope.launch {
            repository.updateHospitalVerification(hospitalId, !currentVerified)
        }
    }
}
