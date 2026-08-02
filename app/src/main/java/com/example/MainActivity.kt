package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.BloodCareBottomNav
import com.example.ui.components.BloodCareTopBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AiChatbotScreen
import com.example.ui.screens.BloodCampsScreen
import com.example.ui.screens.CreateBloodRequestScreen
import com.example.ui.screens.DonationHistoryScreen
import com.example.ui.screens.DonorDashboardScreen
import com.example.ui.screens.EmergencyRequestDetailsScreen
import com.example.ui.screens.LoginRegisterScreen
import com.example.ui.screens.NearbyDonorsMapScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PatientDashboardScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.ThalassemiaSupportScreen
import com.example.ui.theme.BloodCareTheme
import com.example.ui.viewmodel.BloodCareViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BloodCareTheme {
                BloodCareApp()
            }
        }
    }
}

@Composable
fun BloodCareApp() {
    val navController = rememberNavController()
    val viewModel: BloodCareViewModel = viewModel()
    val userSession by viewModel.userSession.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route

    val hideTopBottomNav = currentRoute == Screen.Splash.route ||
            currentRoute == Screen.LoginRegister.route ||
            currentRoute == Screen.RoleSelection.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!hideTopBottomNav) {
                BloodCareTopBar(
                    title = "BloodCare Pakistan",
                    currentRole = userSession.role,
                    onRoleClick = { navController.navigate(Screen.RoleSelection.route) },
                    onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                    onAiClick = { navController.navigate(Screen.AiChatbot.route) }
                )
            }
        },
        bottomBar = {
            if (!hideTopBottomNav) {
                BloodCareBottomNav(
                    currentRoute = currentRoute,
                    currentRole = userSession.role,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        navController.navigate(Screen.LoginRegister.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.LoginRegister.route) {
                LoginRegisterScreen(
                    onLoginSuccess = { phone, name, bloodGroup, city, role ->
                        viewModel.loginUser(phone, name, bloodGroup, city, role)
                        val targetRoute = when(role) {
                            "Patient" -> Screen.PatientDashboard.route
                            "Hospital" -> Screen.PatientDashboard.route
                            "Admin" -> Screen.AdminDashboard.route
                            else -> Screen.DonorDashboard.route
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.LoginRegister.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.RoleSelection.route) {
                RoleSelectionScreen(
                    currentRole = userSession.role,
                    onRoleSelected = { selectedRole ->
                        viewModel.selectRole(selectedRole)
                        val targetRoute = when(selectedRole) {
                            "Patient" -> Screen.PatientDashboard.route
                            "Hospital" -> Screen.PatientDashboard.route
                            "Admin" -> Screen.AdminDashboard.route
                            else -> Screen.DonorDashboard.route
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.DonorDashboard.route) {
                DonorDashboardScreen(
                    viewModel = viewModel,
                    onCreateRequestClick = { navController.navigate(Screen.CreateBloodRequest.route) },
                    onRequestDetailsClick = { requestId ->
                        navController.navigate(Screen.EmergencyRequestDetails.createRoute(requestId))
                    }
                )
            }

            composable(Screen.PatientDashboard.route) {
                PatientDashboardScreen(
                    viewModel = viewModel,
                    onCreateRequestClick = { navController.navigate(Screen.CreateBloodRequest.route) },
                    onFindDonorsMapClick = { navController.navigate(Screen.NearbyDonorsMap.route) }
                )
            }

            composable(Screen.CreateBloodRequest.route) {
                CreateBloodRequestScreen(
                    viewModel = viewModel,
                    onRequestCreated = {
                        val target = if (userSession.role == "Donor") Screen.DonorDashboard.route else Screen.PatientDashboard.route
                        navController.navigate(target) {
                            popUpTo(Screen.CreateBloodRequest.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.NearbyDonorsMap.route) {
                NearbyDonorsMapScreen(viewModel = viewModel)
            }

            composable(
                route = Screen.EmergencyRequestDetails.route,
                arguments = listOf(navArgument("requestId") { type = NavType.IntType })
            ) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getInt("requestId") ?: 1
                EmergencyRequestDetailsScreen(
                    requestId = requestId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.DonationHistory.route) {
                DonationHistoryScreen(viewModel = viewModel)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onRoleSelectClick = { navController.navigate(Screen.RoleSelection.route) },
                    onLogoutClick = {
                        navController.navigate(Screen.LoginRegister.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    viewModel = viewModel,
                    onRequestClick = { reqId ->
                        navController.navigate(Screen.EmergencyRequestDetails.createRoute(reqId))
                    }
                )
            }

            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(viewModel = viewModel)
            }

            composable(Screen.AiChatbot.route) {
                AiChatbotScreen()
            }

            composable(Screen.ThalassemiaSupport.route) {
                ThalassemiaSupportScreen(
                    viewModel = viewModel,
                    onRequestDetailsClick = { reqId ->
                        navController.navigate(Screen.EmergencyRequestDetails.createRoute(reqId))
                    }
                )
            }

            composable(Screen.BloodCamps.route) {
                BloodCampsScreen(viewModel = viewModel)
            }
        }
    }
}
