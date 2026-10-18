package com.example.ui.viewmodel

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser.value)

    val userProfile: StateFlow<UserProfile?> = authRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.userProfile.value)

    val isProfileLoading: StateFlow<Boolean> = authRepository.isProfileLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSubmitting = MutableStateFlow<Boolean>(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun signInWithGoogle(
        activity: Activity,
        credentialManager: CredentialManager,
        onSuccess: () -> Unit
    ) {
        _isSubmitting.value = true
        _errorMessage.value = null
        authRepository.onGoogleSignInClicked(
            activity = activity,
            credentialManager = credentialManager,
            onAuthSuccess = {
                _isSubmitting.value = false
                onSuccess()
            },
            onAuthError = { error ->
                _isSubmitting.value = false
                _errorMessage.value = error
            },
            scope = viewModelScope,
            onAuthCancelled = {
                _isSubmitting.value = false
            }
        )
    }

    fun completeProfile(
        role: UserRole,
        bloodGroup: String,
        city: String,
        phoneNumber: String,
        onSuccess: () -> Unit
    ) {
        _isSubmitting.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            val result = authRepository.createUserProfile(
                role = role,
                bloodGroup = bloodGroup,
                city = city,
                phoneNumber = phoneNumber,
                isDonor = (role == UserRole.DONOR || role == UserRole.BOTH)
            )
            _isSubmitting.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to save profile"
            }
        }
    }

    fun toggleAvailability(isAvailable: Boolean) {
        viewModelScope.launch {
            authRepository.updateAvailability(isAvailable)
        }
    }

    fun updateProfile(city: String, phone: String, bloodGroup: String, onSuccess: () -> Unit) {
        _isSubmitting.value = true
        viewModelScope.launch {
            val result = authRepository.updateProfileDetails(city, phone, bloodGroup)
            _isSubmitting.value = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Update failed"
            }
        }
    }

    fun signOut(credentialManager: CredentialManager, onComplete: () -> Unit) {
        authRepository.signOut(credentialManager, onComplete, viewModelScope)
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
