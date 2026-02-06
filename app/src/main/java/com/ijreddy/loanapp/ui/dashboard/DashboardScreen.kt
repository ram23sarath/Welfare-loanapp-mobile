package com.ijreddy.loanapp.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ijreddy.loanapp.ui.navigation.BottomNavBar
import com.ijreddy.loanapp.ui.navigation.Screen

/**
 * Main dashboard screen acting as a role-based router.
 * - Admins get a Scaffold with Bottom Navigation.
 * - Customers get a dedicated, simpler dashboard layout.
 */
@Composable
fun DashboardScreen(
    isScopedCustomer: Boolean,
    onNavigateToLoans: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToSeniority: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateToCustomers: () -> Unit = {}, // Only for admin
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToAddRecord: () -> Unit,
    onLogout: () -> Unit
) {
    if (isScopedCustomer) {
        // Customer View: Simple layout, no bottom nav
        CustomerDashboardContent(
            onNavigateToLoans = onNavigateToLoans,
            onNavigateToSubscriptions = onNavigateToSubscriptions,
            onNavigateToSeniority = onNavigateToSeniority
        )
    } else {
        // Admin View: Power user layout with Bottom Navigation
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    currentRoute = Screen.Dashboard.route,
                    onNavigate = { route ->
                        when(route) {
                            Screen.Dashboard.route -> { /* Already here */ }
                            Screen.Loans.route -> onNavigateToLoans()
                            Screen.Customers.route -> onNavigateToCustomers()
                            Screen.Summary.route -> onNavigateToSummary()
                        }
                    }
                )
            }
        ) { paddingAndInsets ->
            Box(modifier = Modifier.padding(paddingAndInsets)) {
                AdminDashboardContent(
                    onNavigateToCustomers = onNavigateToCustomers,
                    onNavigateToLoans = onNavigateToLoans,
                    onNavigateToSummary = onNavigateToSummary,
                    onNavigateToAddCustomer = onNavigateToAddCustomer,
                    onNavigateToAddRecord = onNavigateToAddRecord
                )
            }
        }
    }
}
