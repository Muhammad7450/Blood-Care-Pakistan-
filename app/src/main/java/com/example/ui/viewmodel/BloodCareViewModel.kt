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
import com.example.data.remote.FirestoreService
import com.example.data.repository.BloodCareRepository
import com.example.ui.util.PakistanCities
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserSession(
    val isLoggedIn: Boolean = false,
    val role: String = "Donor", // Donor, Patient, Hospital, Admin
    val phoneNumber: String = "",
    val userName: String = "Guest User",
    val userBloodGroup: String = "O+",
    val userCity: String = "Lahore",
    val currentDonorId: Int = 1
)

data class AppNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val time: String = "Just Now",
    val isSmsBackup: Boolean = false,
    val requestId: Int? = null,
    val city: String = "Lahore",
    val distanceKm: Double? = null,
    val donorName: String? = null,
    val donorPhone: String? = null,
    val hospitalName: String? = null
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
            isLoggedIn = false,
            role = "Donor",
            phoneNumber = "",
            userName = "",
            userBloodGroup = "O+",
            userCity = "Lahore",
            currentDonorId = 0
        )
    )
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

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

    private var requestListener: ListenerRegistration? = null
    private var donorListener: ListenerRegistration? = null

    init {
        // Real-time Cloud Firestore sync for Blood Requests
        requestListener = FirestoreService.listenToBloodRequests { firestoreRequests ->
            viewModelScope.launch {
                firestoreRequests.forEach { req ->
                    repository.addBloodRequest(req)

                    // Real-time donor location matching notification
                    val currentUser = _userSession.value
                    if (currentUser.isLoggedIn && repository.isCompatible(currentUser.userBloodGroup, req.bloodGroup)) {
                        val dist = PakistanCities.calculateDistanceKm(currentUser.userCity, req.city)
                        val isExistingAlert = _notifications.value.any { it.requestId == req.id }
                        if (!isExistingAlert && req.status == "Pending") {
                            val alert = AppNotification(
                                title = "🔥 REALTIME CLOUD ALERT (${dist} km away)",
                                body = "Urgent ${req.bloodGroup} request for ${req.patientName} at ${req.hospitalName}, ${req.city}. Tap to respond!",
                                time = "Just Now",
                                isSmsBackup = req.emergencyLevel == "Critical",
                                requestId = req.id,
                                city = req.city,
                                distanceKm = dist,
                                hospitalName = req.hospitalName
                            )
                            _notifications.value = listOf(alert) + _notifications.value
                        }
                    }
                }
            }
        }

        // Real-time Cloud Firestore sync for Donors
        donorListener = FirestoreService.listenToDonors { firestoreDonors ->
            viewModelScope.launch {
                firestoreDonors.forEach { donor ->
                    repository.addDonor(donor)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        requestListener?.remove()
        donorListener?.remove()
    }

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
        // Sync profile to Cloud Firestore
        FirestoreService.saveUserProfile(
            phone = phone,
            name = name,
            role = role,
            bloodGroup = bloodGroup,
            city = city
        )
    }

    fun selectRole(role: String) {
        _userSession.value = _userSession.value.copy(role = role)
    }

    fun toggleDonorAvailability(donorId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.updateDonorAvailability(donorId, !currentStatus)
            val session = _userSession.value
            if (session.phoneNumber.isNotBlank()) {
                FirestoreService.saveUserProfile(
                    phone = session.phoneNumber,
                    name = session.userName,
                    role = session.role,
                    bloodGroup = session.userBloodGroup,
                    city = session.userCity,
                    isAvailable = !currentStatus
                )
            }
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
            val coords = PakistanCities.getCityCoords(city)
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
                requesterRole = _userSession.value.role,
                latitude = coords.first,
                longitude = coords.second
            )
            val newId = repository.addBloodRequest(request).toInt()
            val createdWithId = request.copy(id = newId)

            // Save to Cloud Firestore for real-time sync across devices
            FirestoreService.saveBloodRequest(createdWithId)

            val currentDonors = allDonors.value
            val nearbyMatchingDonors = currentDonors.filter { donor ->
                donor.isAvailable && repository.isCompatible(donor.bloodGroup, bloodGroup)
            }

            val newAlerts = nearbyMatchingDonors.map { donor ->
                val dist = PakistanCities.calculateDistanceKm(donor.city, city)
                AppNotification(
                    title = "🚨 NEARBY EMERGENCY ALERT (${dist} km away)",
                    body = "Need $units unit(s) of $bloodGroup for $patientName at $hospitalName, $city ($locationName). Tap to respond & donate!",
                    time = "Just Now",
                    isSmsBackup = emergencyLevel == "Critical",
                    requestId = newId,
                    city = city,
                    distanceKm = dist,
                    hospitalName = hospitalName
                )
            }

            if (newAlerts.isNotEmpty()) {
                _notifications.value = newAlerts + _notifications.value
            } else {
                _notifications.value = listOf(
                    AppNotification(
                        title = "🚨 $city EMERGENCY BROADCAST",
                        body = "New emergency request for $bloodGroup at $hospitalName, $city. Nearby donors alerted!",
                        time = "Just Now",
                        isSmsBackup = true,
                        requestId = newId,
                        city = city
                    )
                ) + _notifications.value
            }
        }
    }

    fun acceptBloodRequest(
        requestId: Int,
        donorName: String,
        donorPhone: String,
        donorCity: String,
        donorLocationName: String
    ) {
        viewModelScope.launch {
            val current = repository.getRequestById(requestId) ?: return@launch
            val dist = PakistanCities.calculateDistanceKm(donorCity, current.city)
            val nowTime = "Today, " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val updated = current.copy(
                status = "Accepted",
                acceptedDonorId = _userSession.value.currentDonorId,
                acceptedDonorName = donorName,
                acceptedDonorPhone = donorPhone,
                acceptedDonorLocation = "$donorLocationName, $donorCity",
                acceptedDonorDistanceKm = dist,
                acceptedAt = nowTime
            )
            repository.updateRequest(updated)

            // Update in Cloud Firestore
            FirestoreService.updateRequestStatus(
                docId = if (requestId != 0) requestId.toString() else "req_${current.createdAt}",
                status = "Accepted",
                acceptedDonorName = donorName,
                acceptedDonorPhone = donorPhone,
                acceptedDonorLocation = "$donorLocationName, $donorCity",
                acceptedDonorDistanceKm = dist,
                acceptedAt = nowTime
            )

            val mutualAlerts = listOf(
                AppNotification(
                    title = "✅ EMERGENCY REQUEST ACCEPTED!",
                    body = "Hero donor $donorName ($donorPhone) accepted emergency request for ${current.patientName} at ${current.hospitalName}! Distance: $dist km. Contact & Location details shared.",
                    time = "Just Now",
                    isSmsBackup = true,
                    requestId = requestId,
                    city = current.city,
                    distanceKm = dist,
                    donorName = donorName,
                    donorPhone = donorPhone,
                    hospitalName = current.hospitalName
                )
            )
            _notifications.value = mutualAlerts + _notifications.value
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
            val coords = PakistanCities.getCityCoords(city)
            val donor = DonorEntity(
                fullName = name,
                phone = phone,
                bloodGroup = bloodGroup,
                age = age,
                gender = gender,
                city = city,
                locationName = locationName,
                latitude = coords.first,
                longitude = coords.second,
                isAvailable = true,
                lastDonationDate = "Never",
                totalDonations = 0,
                isVerified = true,
                badges = "New Hero,Life Saver"
            )
            val donorId = repository.addDonor(donor).toInt()
            val donorWithId = donor.copy(id = donorId)

            // Save to Cloud Firestore
            FirestoreService.saveDonorEntity(donorWithId)
            FirestoreService.saveUserProfile(
                phone = phone,
                name = name,
                role = "Donor",
                bloodGroup = bloodGroup,
                city = city,
                locationName = locationName,
                isAvailable = true
            )
        }
    }

    fun updateRequestStatus(requestId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateRequestStatus(requestId, newStatus)
            FirestoreService.updateRequestStatus(
                docId = requestId.toString(),
                status = newStatus
            )
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
