package com.ijreddy.loanapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Dashboard content for Admin users.
 * Displays quick actions and summary cards.
 */
@Composable
fun AdminDashboardContent(
    onNavigateToCustomers: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToSummary: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AdminStatCard(
                title = "Active Customers",
                value = "Loading...", // TODO: Bind to stats
                modifier = Modifier.weight(1f),
                onClick = onNavigateToCustomers
            )
            AdminStatCard(
                title = "Total Loans",
                value = "Loading...",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToLoans
            )
        }
        
        // Actions
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Button(
            onClick = { /* TODO: Open Add Customer Modal */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Customer")
        }
        
        OutlinedButton(
            onClick = onNavigateToSummary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Financial Summary")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
