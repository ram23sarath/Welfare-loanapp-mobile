package com.ijreddy.loanapp.ui.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ijreddy.loanapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    loanId: String,
    onNavigateBack: () -> Unit
) {
    var showRecordInstallmentSheet by remember { mutableStateOf(false) }

    // TODO: Load actual loan data from ViewModel
    val loan = remember {
        MockLoanDetail(
            id = loanId,
            customerName = "John Doe",
            originalAmount = 50000.0,
            interestAmount = 5000.0,
            paymentDate = "2024-01-15",
            totalInstalments = 12,
            installmentsPaid = 3
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.loan_detail)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showRecordInstallmentSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.record_installment)) },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Name Header
            item {
                Text(
                    text = loan.customerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Loan Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        LoanDetailRow(
                            label = stringResource(R.string.original_amount),
                            value = "₹${String.format("%,.0f", loan.originalAmount)}"
                        )
                        LoanDetailRow(
                            label = stringResource(R.string.interest_amount),
                            value = "₹${String.format("%,.0f", loan.interestAmount)}"
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        LoanDetailRow(
                            label = "Total",
                            value = "₹${String.format("%,.0f", loan.originalAmount + loan.interestAmount)}",
                            isTotal = true
                        )
                    }
                }
            }

            // Progress Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Installments Progress",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { loan.installmentsPaid.toFloat() / loan.totalInstalments },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${loan.installmentsPaid} of ${loan.totalInstalments} paid",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Other Details
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        LoanDetailRow(
                            label = stringResource(R.string.payment_date),
                            value = loan.paymentDate
                        )
                        LoanDetailRow(
                            label = stringResource(R.string.total_instalments),
                            value = loan.totalInstalments.toString()
                        )
                    }
                }
            }

            // TODO: Add installment history list
        }
    }

    // TODO: Implement Record Installment Bottom Sheet
    if (showRecordInstallmentSheet) {
        // RecordInstallmentSheet(
        //     loanId = loanId,
        //     onDismiss = { showRecordInstallmentSheet = false },
        //     onSuccess = { showRecordInstallmentSheet = false }
        // )
    }
}

@Composable
fun LoanDetailRow(
    label: String,
    value: String,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// Temporary mock data class
data class MockLoanDetail(
    val id: String,
    val customerName: String,
    val originalAmount: Double,
    val interestAmount: Double,
    val paymentDate: String,
    val totalInstalments: Int,
    val installmentsPaid: Int
)
