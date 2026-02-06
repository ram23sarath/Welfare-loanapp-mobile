package com.ijreddy.loanapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ijreddy.loanapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    isScopedCustomer: Boolean,
    onNavigateToLoans: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToSeniority: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard)) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = stringResource(R.string.logout))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Loans Card
            item {
                DashboardCard(
                    title = stringResource(R.string.loans),
                    icon = Icons.Default.AttachMoney,
                    onClick = onNavigateToLoans
                )
            }

            // Subscriptions Card
            item {
                DashboardCard(
                    title = stringResource(R.string.subscriptions),
                    icon = Icons.Default.Subscriptions,
                    onClick = onNavigateToSubscriptions
                )
            }

            // Loan Seniority Card
            item {
                DashboardCard(
                    title = stringResource(R.string.loan_seniority),
                    icon = Icons.Default.Queue,
                    onClick = onNavigateToSeniority
                )
            }

            // Summary Card (admin only typically, but show for all)
            if (!isScopedCustomer) {
                item {
                    DashboardCard(
                        title = stringResource(R.string.summary),
                        icon = Icons.Default.Analytics,
                        onClick = onNavigateToSummary
                    )
                }
            }

            // TODO: Add more cards based on role
            // - Customers (admin)
            // - Data Entries
            // - Documents download (customer)
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
