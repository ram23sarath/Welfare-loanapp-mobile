package com.ijreddy.loanapp.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ijreddy.loanapp.ui.navigation.BottomNavBar
import com.ijreddy.loanapp.ui.navigation.Screen
import com.ijreddy.loanapp.ui.navigation.Sidebar

// Breakpoint for tablet/desktop sidebar (600dp matches Material Design compact/medium breakpoint)
private val TABLET_BREAKPOINT = 600.dp

/**
 * Main dashboard screen acting as a role-based router.
 * - Admins on large screens get a Sidebar.
 * - Admins on phones get Bottom Navigation.
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
    onNavigateToData: () -> Unit = {},
    onLogout: () -> Unit,
    userName: String = "",
    userInitials: String = ""
) {
    // Sidebar collapsed state (persisted across recompositions)
    var isSidebarCollapsed by rememberSaveable { mutableStateOf(false) }

    // Use BoxWithConstraints to detect screen size
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useSidebar = maxWidth >= TABLET_BREAKPOINT && !isScopedCustomer

        if (isScopedCustomer) {
            // Customer View: Simple layout, no navigation chrome
            CustomerDashboardContent(
                onNavigateToLoans = onNavigateToLoans,
                onNavigateToSubscriptions = onNavigateToSubscriptions,
                onNavigateToSeniority = onNavigateToSeniority
            )
        } else if (useSidebar) {
            // Tablet/Desktop: Sidebar + Content
            Row(modifier = Modifier.fillMaxSize()) {
                Sidebar(
                    currentRoute = Screen.Dashboard.route,
                    isScopedCustomer = false,
                    isCollapsed = isSidebarCollapsed,
                    userName = userName,
                    userInitials = userInitials,
                    onNavigate = { route ->
                        handleNavigation(
                            route = route,
                            onNavigateToLoans = onNavigateToLoans,
                            onNavigateToCustomers = onNavigateToCustomers,
                            onNavigateToSummary = onNavigateToSummary,
                            onNavigateToAddCustomer = onNavigateToAddCustomer,
                            onNavigateToAddRecord = onNavigateToAddRecord,
                            onNavigateToSubscriptions = onNavigateToSubscriptions,
                            onNavigateToSeniority = onNavigateToSeniority,
                            onNavigateToData = onNavigateToData
                        )
                    },
                    onToggleCollapse = { isSidebarCollapsed = !isSidebarCollapsed },
                    onLogout = onLogout
                )

                Box(modifier = Modifier.weight(1f)) {
                    AdminDashboardContent(
                        onNavigateToCustomers = onNavigateToCustomers,
                        onNavigateToLoans = onNavigateToLoans,
                        onNavigateToSummary = onNavigateToSummary,
                        onNavigateToAddCustomer = onNavigateToAddCustomer,
                        onNavigateToAddRecord = onNavigateToAddRecord
                    )
                }
            }
        } else {
            // Phone: Bottom Navigation
            Scaffold(
                bottomBar = {
                    BottomNavBar(
                        currentRoute = Screen.Dashboard.route,
                        onNavigate = { route ->
                            when (route) {
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
}

/**
 * Handle navigation from sidebar to appropriate destinations.
 */
private fun handleNavigation(
    route: String,
    onNavigateToLoans: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToAddRecord: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToSeniority: () -> Unit,
    onNavigateToData: () -> Unit
) {
    when (route) {
        Screen.Dashboard.route -> { /* Already on dashboard */ }
        Screen.Loans.route -> onNavigateToLoans()
        Screen.Customers.route -> onNavigateToCustomers()
        Screen.Summary.route -> onNavigateToSummary()
        Screen.AddCustomer.route -> onNavigateToAddCustomer()
        Screen.AddRecord.route -> onNavigateToAddRecord()
        Screen.Subscriptions.route -> onNavigateToSubscriptions()
        Screen.LoanSeniority.route -> onNavigateToSeniority()
        Screen.Data.route -> onNavigateToData()
    }
}

