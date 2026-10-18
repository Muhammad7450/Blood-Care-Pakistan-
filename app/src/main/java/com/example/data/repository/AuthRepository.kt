package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.CityUtils
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        context = context,
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth
    )

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isProfileLoading = MutableStateFlow<Boolean>(false)
    val isProfileLoading: StateFlow<Boolean> = _isProfileLoading.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            if (user != null) {
                loadUserProfile(user.uid)
            } else {
                _userProfile.value = null
            }
        }
    }

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google")
    }

    fun loadUserProfile(uid: String) {
        val path = "users/$uid"
        _isProfileLoading.value = true
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                _isProfileLoading.value = false
                if (doc.exists()) {
                    val profile = doc.toObject(UserProfile::class.java)
                    _userProfile.value = profile
                } else {
                    _userProfile.value = null
                }
            }
            .addOnFailureListener { error ->
                _isProfileLoading.value = false
                handleFirestoreError(error, OperationType.GET, path)
            }
    }

    suspend fun createUserProfile(
        role: UserRole,
        bloodGroup: String,
        city: String,
        phoneNumber: String,
        isDonor: Boolean = true
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val displayName = auth.currentUser?.displayName ?: "User"
        val normalizedCity = CityUtils.normalizeCity(city)
        val path = "users/$uid"

        val capabilities = when (role) {
            UserRole.DONOR -> listOf("CAN_DONATE")
            UserRole.PATIENT -> listOf("CAN_REQUEST")
            UserRole.BOTH -> listOf("CAN_DONATE", "CAN_REQUEST")
            UserRole.ADMIN -> listOf("CAN_DONATE", "CAN_REQUEST", "ADMIN_ACCESS")
        }

        val profileData = mapOf(
            "uid" to uid,
            "fullName" to displayName,
            "role" to role.name,
            "supportedCapabilities" to capabilities,
            "bloodGroup" to bloodGroup,
            "city" to city,
            "normalizedCity" to normalizedCity,
            "phoneNumber" to phoneNumber,
            "isDonor" to isDonor,
            "isAvailable" to true,
            "isProfileVerified" to false,
            "fcmToken" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            db.collection("users").document(uid).set(profileData).await()
            val createdProfile = UserProfile(
                uid = uid,
                fullName = displayName,
                role = role.name,
                supportedCapabilities = capabilities,
                bloodGroup = bloodGroup,
                city = city,
                normalizedCity = normalizedCity,
                phoneNumber = phoneNumber,
                isDonor = isDonor,
                isAvailable = true,
                isProfileVerified = false
            )
            _userProfile.value = createdProfile
            Result.success(createdProfile)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateAvailability(isAvailable: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val path = "users/$uid"
        try {
            db.collection("users").document(uid).update(
                mapOf(
                    "isAvailable" to isAvailable,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            _userProfile.value = _userProfile.value?.copy(isAvailable = isAvailable)
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProfileDetails(
        city: String,
        phoneNumber: String,
        bloodGroup: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val path = "users/$uid"
        try {
            db.collection("users").document(uid).update(
                mapOf(
                    "city" to city,
                    "normalizedCity" to CityUtils.normalizeCity(city),
                    "phoneNumber" to phoneNumber,
                    "bloodGroup" to bloodGroup,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            _userProfile.value = _userProfile.value?.copy(
                city = city,
                normalizedCity = CityUtils.normalizeCity(city),
                phoneNumber = phoneNumber,
                bloodGroup = bloodGroup
            )
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(token: String) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val path = "users/$uid"
        try {
            db.collection("users").document(uid).update(
                mapOf(
                    "fcmToken" to token,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    fun attemptAutoSignIn(
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        if (auth.currentUser != null) {
            onAuthSuccess()
            return
        }
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    onAuthSuccess()
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                onUnauthenticated()
            }
        }
    }

    fun onGoogleSignInClicked(
        activity: Activity,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit,
        scope: CoroutineScope,
        onAuthCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    onAuthSuccess()
                } else {
                    onAuthError("Unexpected credential type")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                onAuthCancelled()
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed", e)
                onAuthError(e.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun signOut(
        credentialManager: CredentialManager,
        onSignOutComplete: () -> Unit,
        scope: CoroutineScope
    ) {
        auth.signOut()
        _currentUser.value = null
        _userProfile.value = null
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Failed to clear credential state", e)
            } finally {
                onSignOutComplete()
            }
        }
    }
}
