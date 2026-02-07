package com.ijreddy.loanapp.ui.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ijreddy.loanapp.ui.common.DataEntryCard
import com.ijreddy.loanapp.ui.common.DataEntryType
import com.ijreddy.loanapp.ui.common.LoanCard
import com.ijreddy.loanapp.ui.common.SubscriptionCard
import java.text.NumberFormat
import java.util.Locale

/**
 * Full-screen customer detail view with tabs.
 * Replaces CustomerDetailModal from web.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: String,
    onNavigateBack: () -> Unit,
    onNavigateToLoan: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomerDetailViewModel = hiltViewModel()
) {
    val tabs = listOf("Overview", "Loans", "Subscriptions", "Entries")
    var selectedTab by remember { mutableStateOf(0) }

    val uiState by viewModel.uiState.collectAsState()
    val customer = uiState.customer

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "Customer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Edit customer */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        if (uiState.isLoading && customer == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Tab row
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                // Tab content
                when (selectedTab) {
                    0 -> CustomerOverviewTab(uiState)
                    1 -> CustomerLoansTab(
                        loans = uiState.loans,
                        onLoanClick = onNavigateToLoan
                    )
                    2 -> CustomerSubscriptionsTab(
                        subscriptions = uiState.subscriptions,
                        customerName = customer?.name ?: ""
                    )
                    3 -> CustomerEntriesTab(entries = uiState.entries)
                }
            }
        }
    }
}

@Composable
private fun CustomerOverviewTab(state: CustomerDetailUiState) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val customer = state.customer

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Contact Info Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Contact Information", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()

                    InfoRow(icon = Icons.Default.Phone, label = "Phone", value = customer?.phone ?: "—")
                    InfoRow(icon = Icons.Default.Email, label = "Email", value = state.customerEmail ?: "—")
                }
            }
        }
        
        // Summary Cards
        item {
            Text("Financial Summary", style = MaterialTheme.typography.titleMedium)
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Loans",
                    amount = currencyFormat.format(state.totalLoansAmount),
                    icon = Icons.Default.AccountBalance,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Subscriptions",
                    amount = currencyFormat.format(state.totalSubscriptionsAmount),
                    icon = Icons.Default.Repeat,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            SummaryCard(
                title = "Data Entries",
                amount = currencyFormat.format(state.totalDataEntriesAmount),
                icon = Icons.Default.Receipt,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CustomerLoansTab(
    loans: List<com.ijreddy.loanapp.ui.model.LoanUiModel>,
    onLoanClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(loans, key = { it.id }) { loan ->
            LoanCard(
                customerName = loan.customerName,
                originalAmount = loan.principal,
                interestAmount = (loan.principal * loan.interestRate / 100.0),
                paymentDate = loan.startDate,
                totalInstallments = loan.tenureMonths,
                paidInstallments = 0,
                onTap = { onLoanClick(loan.id) },
                onLongPress = { /* Show context menu */ }
            )
        }
    }
}

@Composable
private fun CustomerSubscriptionsTab(
    subscriptions: List<com.ijreddy.loanapp.data.local.entity.SubscriptionEntity>,
    customerName: String
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(subscriptions, key = { it.id }) { sub ->
            SubscriptionCard(
                customerName = customerName,
                amount = sub.amount,
                date = sub.start_date,
                receiptNumber = sub.id.take(8),
                lateFee = null,
                onTap = { },
                onLongPress = { }
            )
        }
    }
}

@Composable
private fun CustomerEntriesTab(entries: List<com.ijreddy.loanapp.data.local.entity.DataEntryEntity>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(entries, key = { it.id }) { entry ->
            DataEntryCard(
                customerName = null,
                amount = entry.amount,
                date = entry.date,
                receiptNumber = entry.id.take(8),
                type = DataEntryType.valueOf(entry.type.uppercase()),
                notes = entry.description,
                onTap = { },
                onLongPress = { }
            )
        }
    }
}
