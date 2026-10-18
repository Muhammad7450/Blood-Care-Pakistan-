package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.BloodCareRepository
import com.example.theme.BloodCareTheme
import com.example.theme.CrimsonRed
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CreateBloodRequestScreen
import com.example.ui.screens.DonorDashboardScreen
import com.example.ui.screens.NotificationListScreen
import com.example.ui.screens.PatientDashboardScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RequestDetailsScreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.BloodCareViewModel

enum class Screen {
    DASHBOARD,
    CREATE_REQUEST,
    REQUEST_DETAILS,
    NOTIFICATIONS,
    ADMIN,
    PROFILE
}

class MainActivity : ComponentActivity() {

    private val authRepository by lazy { AuthRepository(applicationContext) }
    private val bloodCareRepository by lazy { BloodCareRepository(applicationContext) }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.Factory(authRepository)
    }

    private val bloodCareViewModel: BloodCareViewModel by viewModels {
        BloodCareViewModel.Factory(bloodCareRepository)
    }

    private val notificationRequestId = mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("EXTRA_REQUEST_ID")?.let { reqId ->
            if (reqId.isNotBlank()) {
                notificationRequestId.value = reqId
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Ensure High-Priority Notification Channels exist immediately
        com.example.util.NotificationHelper.createNotificationChannel(applicationContext)

        // 2. Schedule persistent background sync job & trigger immediate check
        com.example.data.service.BloodCareBackgroundSyncService.scheduleJob(applicationContext)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            com.example.data.service.BloodCareBackgroundSyncService.triggerImmediateSync(applicationContext)
        }

        val initialRequestId = intent?.getStringExtra("EXTRA_REQUEST_ID")
        if (!initialRequestId.isNullOrBlank()) {
            notificationRequestId.value = initialRequestId
        }

        setContent {
            BloodCareTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(
                        authViewModel = authViewModel,
                        bloodCareViewModel = bloodCareViewModel,
                        initialRequestId = notificationRequestId.value
                    )
                }
            }
        }
    }
}

@Composable
fun AppRoot(
    authViewModel: AuthViewModel,
    bloodCareViewModel: BloodCareViewModel,
    initialRequestId: String? = null
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val isProfileLoading by authViewModel.isProfileLoading.collectAsState()

    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var activeRequestId by remember { mutableStateOf(initialRequestId ?: "") }

    // Request notification permission on Android 13+
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(initialRequestId) {
        if (!initialRequestId.isNullOrBlank()) {
            activeRequestId = initialRequestId
            currentScreen = Screen.REQUEST_DETAILS
        }
    }

    // 1. Auth Gating: If not signed in or profile is being configured, show AuthScreen
    if (currentUser == null || userProfile == null) {
        AuthScreen(
            authViewModel = authViewModel,
            onAuthComplete = {
                currentScreen = Screen.DASHBOARD
            }
        )
        return
    }

    if (isProfileLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CrimsonRed)
        }
        return
    }

    val profile = userProfile!!

    // BackHandler for custom screen stack navigation
    if (currentScreen != Screen.DASHBOARD) {
        BackHandler {
            currentScreen = Screen.DASHBOARD
        }
    }

    // Navigation routing
    when (currentScreen) {
        Screen.DASHBOARD -> {
            // AUTOMATIC ROLE DETERMINATION - NO MODE SWITCHING BUTTONS!
            val userRole = UserRole.fromString(profile.role)
            when (userRole) {
                UserRole.PATIENT -> {
                    PatientDashboardScreen(
                        userProfile = profile,
                        bloodCareViewModel = bloodCareViewModel,
                        onRequestClick = { req ->
                            activeRequestId = req.id
                            bloodCareViewModel.selectRequest(req)
                            currentScreen = Screen.REQUEST_DETAILS
                        },
                        onCreateRequestClick = { currentScreen = Screen.CREATE_REQUEST },
                        onNotificationsClick = { currentScreen = Screen.NOTIFICATIONS },
                        onProfileClick = { currentScreen = Screen.PROFILE },
                        onAdminClick = { currentScreen = Screen.ADMIN }
                    )
                }
                else -> {
                    // DONOR, BOTH, or ADMIN primary feed
                    DonorDashboardScreen(
                        userProfile = profile,
                        authViewModel = authViewModel,
                        bloodCareViewModel = bloodCareViewModel,
                        onRequestClick = { req ->
                            activeRequestId = req.id
                            bloodCareViewModel.selectRequest(req)
                            currentScreen = Screen.REQUEST_DETAILS
                        },
                        onCreateRequestClick = { currentScreen = Screen.CREATE_REQUEST },
                        onNotificationsClick = { currentScreen = Screen.NOTIFICATIONS },
                        onProfileClick = { currentScreen = Screen.PROFILE },
                        onAdminClick = { currentScreen = Screen.ADMIN }
                    )
                }
            }
        }
        Screen.CREATE_REQUEST -> {
            CreateBloodRequestScreen(
                userProfile = profile,
                bloodCareViewModel = bloodCareViewModel,
                onBackClick = { currentScreen = Screen.DASHBOARD },
                onRequestCreated = { createdId ->
                    activeRequestId = createdId
                    currentScreen = Screen.REQUEST_DETAILS
                }
            )
        }
        Screen.REQUEST_DETAILS -> {
            RequestDetailsScreen(
                requestId = activeRequestId,
                currentUserProfile = profile,
                bloodCareViewModel = bloodCareViewModel,
                onBackClick = { currentScreen = Screen.DASHBOARD }
            )
        }
        Screen.NOTIFICATIONS -> {
            NotificationListScreen(
                bloodCareViewModel = bloodCareViewModel,
                onBackClick = { currentScreen = Screen.DASHBOARD },
                onNotificationClick = { reqId ->
                    activeRequestId = reqId
                    currentScreen = Screen.REQUEST_DETAILS
                }
            )
        }
        Screen.ADMIN -> {
            AdminDashboardScreen(
                bloodCareViewModel = bloodCareViewModel,
                onBackClick = { currentScreen = Screen.DASHBOARD },
                onRequestClick = { req ->
                    activeRequestId = req.id
                    bloodCareViewModel.selectRequest(req)
                    currentScreen = Screen.REQUEST_DETAILS
                }
            )
        }
        Screen.PROFILE -> {
            ProfileScreen(
                userProfile = profile,
                authViewModel = authViewModel,
                onBackClick = { currentScreen = Screen.DASHBOARD },
                onSignedOut = {
                    currentScreen = Screen.DASHBOARD
                }
            )
        }
    }
}
