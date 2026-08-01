package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object LoginRegister : Screen("login_register")
    object RoleSelection : Screen("role_selection")
    object DonorDashboard : Screen("donor_dashboard")
    object PatientDashboard : Screen("patient_dashboard")
    object CreateBloodRequest : Screen("create_blood_request")
    object NearbyDonorsMap : Screen("nearby_donors_map")
    object EmergencyRequestDetails : Screen("request_details/{requestId}") {
        fun createRoute(requestId: Int) = "request_details/$requestId"
    }
    object DonationHistory : Screen("donation_history")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object AdminDashboard : Screen("admin_dashboard")
    object AiChatbot : Screen("ai_chatbot")
    object ThalassemiaSupport : Screen("thalassemia_support")
    object BloodCamps : Screen("blood_camps")
}
