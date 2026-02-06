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
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Overview", "Loans", "Subscriptions", "Entries")
    var selectedTab by remember { mutableStateOf(0) }
    
    // Mock data - replace with ViewModel
    val customer = remember {
        CustomerData(
            id = customerId,
            name = "Priya Sharma",
            phone = "9876543210",
            stationName = "Jubilee Hills Fire Station",
            email = "@9876543210@loanapp.local",
            totalLoansAmount = 150000.0,
            totalSubscriptionsAmount = 24000.0,
            totalDataEntriesAmount = 5000.0
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer.name) },
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
                0 -> CustomerOverviewTab(customer)
                1 -> CustomerLoansTab(
                    customerId = customerId,
                    onLoanClick = onNavigateToLoan
                )
                2 -> CustomerSubscriptionsTab(customerId = customerId)
                3 -> CustomerEntriesTab(customerId = customerId)
            }
        }
    }
}

@Composable
private fun CustomerOverviewTab(customer: CustomerData) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    
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
                    
                    InfoRow(icon = Icons.Default.Phone, label = "Phone", value = customer.phone)
                    InfoRow(icon = Icons.Default.LocationOn, label = "Station", value = customer.stationName)
                    InfoRow(icon = Icons.Default.Email, label = "Email", value = customer.email)
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
                    amount = currencyFormat.format(customer.totalLoansAmount),
                    icon = Icons.Default.AccountBalance,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Subscriptions",
                    amount = currencyFormat.format(customer.totalSubscriptionsAmount),
                    icon = Icons.Default.Repeat,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            SummaryCard(
                title = "Data Entries",
                amount = currencyFormat.format(customer.totalDataEntriesAmount),
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
    customerId: String,
    onLoanClick: (String) -> Unit
) {
    // Mock loans data
    val loans = remember {
        listOf(
            MockLoan("1", "Priya Sharma", 50000.0, 5000.0, "2024-01-15", 12, 8),
            MockLoan("2", "Priya Sharma", 30000.0, 3000.0, "2024-06-10", 6, 2)
        )
    }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(loans, key = { it.id }) { loan ->
            LoanCard(
                customerName = loan.customerName,
                originalAmount = loan.originalAmount,
                interestAmount = loan.interestAmount,
                paymentDate = loan.paymentDate,
                totalInstallments = loan.totalInstallments,
                paidInstallments = loan.paidInstallments,
                onTap = { onLoanClick(loan.id) },
                onLongPress = { /* Show context menu */ }
            )
        }
    }
}

@Composable
private fun CustomerSubscriptionsTab(customerId: String) {
    val subscriptions = remember {
        listOf(
            MockSubscription("1", "Priya Sharma", 2000.0, "2024-12-01", "REC-001"),
            MockSubscription("2", "Priya Sharma", 2000.0, "2024-11-01", "REC-002", 100.0)
        )
    }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(subscriptions, key = { it.id }) { sub ->
            SubscriptionCard(
                customerName = sub.customerName,
                amount = sub.amount,
                date = sub.date,
                receiptNumber = sub.receiptNumber,
                lateFee = sub.lateFee,
                onTap = { },
                onLongPress = { }
            )
        }
    }
}

@Composable
private fun CustomerEntriesTab(customerId: String) {
    val entries = remember {
        listOf(
            MockEntry("1", 5000.0, "2024-12-15", "ENT-001", DataEntryType.CREDIT, "Bonus payment"),
            MockEntry("2", 1500.0, "2024-12-10", "ENT-002", DataEntryType.DEBIT, null)
        )
    }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(entries, key = { it.id }) { entry ->
            DataEntryCard(
                customerName = null,
                amount = entry.amount,
                date = entry.date,
                receiptNumber = entry.receiptNumber,
                type = entry.type,
                notes = entry.notes,
                onTap = { },
                onLongPress = { }
            )
        }
    }
}

// Data classes for mock data
private data class CustomerData(
    val id: String,
    val name: String,
    val phone: String,
    val stationName: String,
    val email: String,
    val totalLoansAmount: Double,
    val totalSubscriptionsAmount: Double,
    val totalDataEntriesAmount: Double
)

private data class MockLoan(
    val id: String,
    val customerName: String,
    val originalAmount: Double,
    val interestAmount: Double,
    val paymentDate: String,
    val totalInstallments: Int,
    val paidInstallments: Int
)

private data class MockSubscription(
    val id: String,
    val customerName: String,
    val amount: Double,
    val date: String,
    val receiptNumber: String,
    val lateFee: Double? = null
)

private data class MockEntry(
    val id: String,
    val amount: Double,
    val date: String,
    val receiptNumber: String,
    val type: DataEntryType,
    val notes: String?
)
