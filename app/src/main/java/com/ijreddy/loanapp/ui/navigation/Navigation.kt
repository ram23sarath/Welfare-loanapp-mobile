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
import com.ijreddy.loanapp.ui.customers.CustomerDetailScreen
import com.ijreddy.loanapp.ui.customers.CustomerListScreen
import com.ijreddy.loanapp.ui.dashboard.DashboardScreen
import com.ijreddy.loanapp.ui.data.DataEntriesScreen
import com.ijreddy.loanapp.ui.loans.LoanListScreen
import com.ijreddy.loanapp.ui.loans.LoanDetailScreen
import com.ijreddy.loanapp.ui.subscriptions.SubscriptionListScreen
import com.ijreddy.loanapp.ui.summary.SummaryScreen

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
                onNavigateToCustomers = { navController.navigate(Screen.Customers.route) },
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

        // Customer List
        composable(Screen.Customers.route) {
            CustomerListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { customerId ->
                    navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                }
            )
        }
        
        // Trash (Placeholder for now)
        composable(Screen.Trash.route) {
            Box(androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { 
                androidx.compose.material3.Text("Trash Screen (Coming Soon)") 
            }
        }
        
        // Add Record (Placeholder for now)
        composable(Screen.AddRecord.route) {
             Box(androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { 
                androidx.compose.material3.Text("Add Record Screen (Coming Soon)") 
            }
        }
        
        // Customer Detail
        composable(
            route = Screen.CustomerDetail.route,
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
            CustomerDetailScreen(
                customerId = customerId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLoan = { loanId ->
                    navController.navigate(Screen.LoanDetail.createRoute(loanId))
                }
            )
        }
        
        // Subscriptions
        composable(Screen.Subscriptions.route) {
            SubscriptionListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Summary
        composable(Screen.Summary.route) {
            SummaryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Data Entries
        composable(Screen.Data.route) {
            DataEntriesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
