package com.ijreddy.loanapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ijreddy.loanapp.ui.auth.LoginScreen
import com.ijreddy.loanapp.ui.auth.AuthViewModel
import com.ijreddy.loanapp.ui.dashboard.DashboardScreen
import com.ijreddy.loanapp.ui.loans.LoanListScreen
import com.ijreddy.loanapp.ui.loans.LoanDetailScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Customers : Screen("customers")
    object CustomerDetail : Screen("customers/{customerId}") {
        fun createRoute(customerId: String) = "customers/$customerId"
    }
    object Loans : Screen("loans")
    object LoanDetail : Screen("loans/{loanId}") {
        fun createRoute(loanId: String) = "loans/$loanId"
    }
    object Subscriptions : Screen("subscriptions")
    object LoanSeniority : Screen("loan-seniority")
    object Summary : Screen("summary")
    object Data : Screen("data")
    object Trash : Screen("trash")
    object AddRecord : Screen("add-record")
}

@Composable
fun LoanAppNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val isScopedCustomer by authViewModel.isScopedCustomer.collectAsState()
    
    val startDestination = if (isAuthenticated) {
        Screen.Dashboard.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard (switches based on user role)
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                isScopedCustomer = isScopedCustomer,
                onNavigateToLoans = { navController.navigate(Screen.Loans.route) },
                onNavigateToSubscriptions = { navController.navigate(Screen.Subscriptions.route) },
                onNavigateToSeniority = { navController.navigate(Screen.LoanSeniority.route) },
                onNavigateToSummary = { navController.navigate(Screen.Summary.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Loans
        composable(Screen.Loans.route) {
            LoanListScreen(
                onLoanClick = { loanId ->
                    navController.navigate(Screen.LoanDetail.createRoute(loanId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LoanDetail.route,
            arguments = listOf(navArgument("loanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments?.getString("loanId") ?: return@composable
            LoanDetailScreen(
                loanId = loanId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // TODO: Add remaining screens:
        // - CustomerListScreen (admin only)
        // - CustomerDetailScreen
        // - SubscriptionListScreen
        // - LoanSeniorityScreen  
        // - SummaryScreen
        // - DataScreen
        // - TrashScreen (admin only)
        // - AddRecordScreen (admin only)
    }
}
