package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BloodRequest
import com.example.data.model.CityUtils
import com.example.data.model.DonorResponse
import com.example.data.model.NotificationRecord
import com.example.data.model.RequestStatus
import com.example.data.model.RequestUrgency
import com.example.data.repository.BloodCareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class BloodCareViewModel(
    private val repository: BloodCareRepository
) : ViewModel() {

    // Donor city filter and requests
    private val _donorCity = MutableStateFlow<String>("")
    val donorCity: StateFlow<String> = _donorCity.asStateFlow()

    private val _bloodGroupFilter = MutableStateFlow<String>("ALL")
    val bloodGroupFilter: StateFlow<String> = _bloodGroupFilter.asStateFlow()

    private val _urgencyFilter = MutableStateFlow<String>("ALL")
    val urgencyFilter: StateFlow<String> = _urgencyFilter.asStateFlow()

    private val _matchingRequestsState = MutableStateFlow<UiState<List<BloodRequest>>>(UiState.Loading)
    val matchingRequestsState: StateFlow<UiState<List<BloodRequest>>> = _matchingRequestsState.asStateFlow()

    // Patient requests
    private val _patientRequestsState = MutableStateFlow<UiState<List<BloodRequest>>>(UiState.Loading)
    val patientRequestsState: StateFlow<UiState<List<BloodRequest>>> = _patientRequestsState.asStateFlow()

    // Active Selected Request Details
    private val _selectedRequest = MutableStateFlow<BloodRequest?>(null)
    val selectedRequest: StateFlow<BloodRequest?> = _selectedRequest.asStateFlow()

    private val _requestResponses = MutableStateFlow<List<DonorResponse>>(emptyList())
    val requestResponses: StateFlow<List<DonorResponse>> = _requestResponses.asStateFlow()

    // Notifications
    private val _notificationsState = MutableStateFlow<List<NotificationRecord>>(emptyList())
    val notificationsState: StateFlow<List<NotificationRecord>> = _notificationsState.asStateFlow()

    // Admin Requests
    private val _adminRequestsState = MutableStateFlow<UiState<List<BloodRequest>>>(UiState.Loading)
    val adminRequestsState: StateFlow<UiState<List<BloodRequest>>> = _adminRequestsState.asStateFlow()

    // Transient UI feedback
    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isActionLoading = MutableStateFlow<Boolean>(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun setBloodGroupFilter(group: String) {
        _bloodGroupFilter.value = group
    }

    fun setUrgencyFilter(urgency: String) {
        _urgencyFilter.value = urgency
    }

    /**
     * Start real-time observation for matching requests in a donor's city.
     * New requests arrive in real time without refreshing or mode switching!
     */
    /**
     * Start real-time observation for matching requests in a donor's city.
     * New requests arrive in real time without refreshing or mode switching!
     * When a new urgent request arrives, pops up a heads-up alert immediately on screen.
     */
    fun startObservingMatchingRequests(context: android.content.Context? = null, city: String, donorBloodGroup: String = "") {
        _donorCity.value = city
        val normalized = CityUtils.normalizeCity(city)
        viewModelScope.launch {
            val prefs = context?.getSharedPreferences("bloodcare_alert_prefs", android.content.Context.MODE_PRIVATE)
            val shownAlertIds = prefs?.getStringSet("shown_alerts", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

            repository.observeCityBloodRequests(normalized)
                .map<List<BloodRequest>, UiState<List<BloodRequest>>> { list ->
                    // Trigger pop-up alert for newly posted matching requests
                    if (context != null) {
                        for (req in list) {
                            if (req.status == RequestStatus.OPEN.name) {
                                if (donorBloodGroup.isBlank() || CityUtils.isBloodCompatible(donorBloodGroup, req.bloodGroup)) {
                                    val alertKey = "req_${req.id}"
                                    if (!shownAlertIds.contains(alertKey)) {
                                        val notifId = req.id.hashCode()
                                        com.example.util.NotificationHelper.showBloodNotification(
                                            context = context,
                                            notificationId = notifId,
                                            title = "🚨 BLOODCARE PAKISTAN - ${req.urgency.uppercase()} REQUEST",
                                            body = "${req.bloodGroup} needed at ${req.hospitalName} (${req.city}). ${req.unitsRequired} unit(s).",
                                            requestId = req.id
                                        )
                                        shownAlertIds.add(alertKey)
                                    }
                                }
                            }
                        }
                        prefs?.edit()?.putStringSet("shown_alerts", shownAlertIds)?.apply()
                    }
                    UiState.Success(list)
                }
                .catch { emit(UiState.Error(it.localizedMessage ?: "Failed to load requests")) }
                .collect { state ->
                    _matchingRequestsState.value = state
                }
        }
    }

    /**
     * Start real-time observation for patient's requests.
     */
    fun startObservingPatientRequests(patientId: String) {
        viewModelScope.launch {
            repository.observePatientRequests(patientId)
                .map<List<BloodRequest>, UiState<List<BloodRequest>>> { list ->
                    UiState.Success(list)
                }
                .catch { emit(UiState.Error(it.localizedMessage ?: "Failed to load my requests")) }
                .collect { state ->
                    _patientRequestsState.value = state
                }
        }
    }

    /**
     * Select a request for the details screen and listen to its updates and responses.
     */
    fun selectRequest(request: BloodRequest) {
        _selectedRequest.value = request
        observeSelectedRequestDetails(request.id)
    }

    fun loadRequestById(requestId: String) {
        if (requestId.isNotBlank()) {
            observeSelectedRequestDetails(requestId)
        }
    }

    private fun observeSelectedRequestDetails(requestId: String) {
        if (requestId.isBlank()) return
        viewModelScope.launch {
            repository.observeRequestById(requestId)
                .catch { e -> android.util.Log.e("BloodCareVM", "Error loading request details", e) }
                .collect { req ->
                    if (req != null) {
                        _selectedRequest.value = req
                    }
                }
        }
        viewModelScope.launch {
            repository.observeDonorResponses(requestId)
                .catch { e -> android.util.Log.e("BloodCareVM", "Error loading responses", e) }
                .collect { responses ->
                    _requestResponses.value = responses
                }
        }
    }

    /**
     * Start real-time observation for notifications and trigger heads-up popup alerts on screen.
     */
    fun startObservingNotifications(context: android.content.Context, userId: String) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("bloodcare_alert_prefs", android.content.Context.MODE_PRIVATE)
            val shownAlertIds = prefs.getStringSet("shown_alerts", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

            repository.observeUserNotifications(userId).collect { list ->
                _notificationsState.value = list

                for (notif in list) {
                    val alertKey = "notif_${notif.id}"
                    if (!notif.isRead && !shownAlertIds.contains(alertKey)) {
                        val notifId = (notif.requestId.hashCode().takeIf { it != 0 } ?: notif.id.hashCode())
                        com.example.util.NotificationHelper.showBloodNotification(
                            context = context,
                            notificationId = notifId,
                            title = notif.title,
                            body = notif.body,
                            requestId = notif.requestId
                        )
                        shownAlertIds.add(alertKey)
                    }
                }
                prefs.edit().putStringSet("shown_alerts", shownAlertIds).apply()
            }
        }
    }

    fun startObservingNotifications(userId: String) {
        viewModelScope.launch {
            repository.observeUserNotifications(userId).collect { list ->
                _notificationsState.value = list
            }
        }
    }

    /**
     * Start real-time observation for admin requests.
     */
    fun startObservingAdminRequests() {
        viewModelScope.launch {
            repository.observeAllRequestsAdmin()
                .map<List<BloodRequest>, UiState<List<BloodRequest>>> { UiState.Success(it) }
                .catch { emit(UiState.Error(it.localizedMessage ?: "Admin load error")) }
                .collect { _adminRequestsState.value = it }
        }
    }

    /**
     * Submit an emergency blood request.
     */
    fun submitBloodRequest(
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
        longitude: Double = 0.0,
        onSuccess: (String) -> Unit
    ) {
        _isActionLoading.value = true
        _actionMessage.value = null
        viewModelScope.launch {
            val result = repository.createBloodRequest(
                patientName = patientName,
                patientPhone = patientPhone,
                bloodGroup = bloodGroup,
                city = city,
                hospitalName = hospitalName,
                hospitalAddress = hospitalAddress,
                unitsRequired = unitsRequired,
                urgency = urgency,
                requiredDate = requiredDate,
                additionalNotes = additionalNotes,
                latitude = latitude,
                longitude = longitude
            )
            _isActionLoading.value = false
            if (result.isSuccess) {
                _actionMessage.value = "Blood request published successfully! Matching donors in $city have been notified."
                onSuccess(result.getOrThrow())
            } else {
                _actionMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to post request."
            }
        }
    }

    /**
     * Submit donor response ("I Can Donate").
     */
    fun donateForRequest(
        request: BloodRequest,
        donorName: String,
        donorPhone: String,
        donorBloodGroup: String,
        onSuccess: () -> Unit
    ) {
        _isActionLoading.value = true
        _actionMessage.value = null
        viewModelScope.launch {
            val result = repository.respondToRequest(
                requestId = request.id,
                patientId = request.patientId,
                donorName = donorName,
                donorPhone = donorPhone,
                donorBloodGroup = donorBloodGroup
            )
            _isActionLoading.value = false
            if (result.isSuccess) {
                _actionMessage.value = "Thank you! Your donation response was sent to the patient."
                onSuccess()
            } else {
                _actionMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to respond."
            }
        }
    }

    /**
     * Update request status (e.g. FULFILLED or CANCELLED).
     */
    fun updateStatus(requestId: String, newStatus: RequestStatus) {
        _isActionLoading.value = true
        viewModelScope.launch {
            val result = repository.updateRequestStatus(requestId, newStatus)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _actionMessage.value = "Request marked as ${newStatus.name}."
            } else {
                _actionMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Status update failed."
            }
        }
    }

    /**
     * Soft delete/archive request.
     */
    fun deleteRequest(requestId: String, onSuccess: () -> Unit) {
        _isActionLoading.value = true
        viewModelScope.launch {
            val result = repository.deleteRequest(requestId)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _actionMessage.value = "Request archived."
                onSuccess()
            } else {
                _actionMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Delete failed."
            }
        }
    }

    fun markNotificationRead(notifId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notifId)
        }
    }

    class Factory(private val repository: BloodCareRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BloodCareViewModel(repository) as T
        }
    }
}
