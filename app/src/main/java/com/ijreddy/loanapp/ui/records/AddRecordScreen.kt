package com.ijreddy.loanapp.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToDataEntries: () -> Unit
) {
    val actions = listOf(
        RecordAction(
            title = "New Loan",
            subtitle = "Create a loan and set terms",
            icon = Icons.Default.AttachMoney,
            onClick = onNavigateToLoans
        ),
        RecordAction(
            title = "Record Installment",
            subtitle = "Open loans to add an installment",
            icon = Icons.Default.AddCard,
            onClick = onNavigateToLoans
        ),
        RecordAction(
            title = "New Subscription",
            subtitle = "Add a subscription payment",
            icon = Icons.Default.Payments,
            onClick = onNavigateToSubscriptions
        ),
        RecordAction(
            title = "New Data Entry",
            subtitle = "Log credit, debit, or expense",
            icon = Icons.Default.ReceiptLong,
            onClick = onNavigateToDataEntries
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Record") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(actions) { action ->
                RecordActionCard(action = action)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordActionCard(action: RecordAction) {
    Card(
        onClick = action.onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(action.title, style = MaterialTheme.typography.titleMedium)
            Text(action.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class RecordAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
