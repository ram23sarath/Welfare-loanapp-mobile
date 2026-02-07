package com.ijreddy.loanapp.ui.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ijreddy.loanapp.R
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.ui.sheets.RecordLoanBottomSheet
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanListScreen(
    onLoanClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LoanListViewModel = hiltViewModel()
) {
    val loans by viewModel.loans.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val totalPrincipal by viewModel.totalPrincipal.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val customers by viewModel.customerList.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    var showCustomerPicker by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var showAddLoanSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(stringResource(R.string.loans))
                        Text(
                            text = "Total: ${currencyFormat.format(totalPrincipal)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // TODO: meaningful search/sort UI
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCustomerPicker = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Loan")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (loans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No loans found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = loans,
                    key = { it.id }
                ) { loan ->
                    LoanCard(
                        loan = loan,
                        onClick = { onLoanClick(loan.id) }
                    )
                }
            }
        }
    }

    if (showCustomerPicker) {
        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = { Text("Select Customer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (customers.isEmpty()) {
                        Text("No customers available")
                    } else {
                        customers.forEach { customer ->
                            TextButton(
                                onClick = {
                                    selectedCustomer = customer
                                    showCustomerPicker = false
                                    showAddLoanSheet = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(customer.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomerPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAddLoanSheet && selectedCustomer != null) {
        RecordLoanBottomSheet(
            customerId = selectedCustomer?.id.orEmpty(),
            customerName = selectedCustomer?.name.orEmpty(),
            onDismiss = { showAddLoanSheet = false },
            onSave = { originalAmount, interestAmount, paymentDate, totalInstallments, _ ->
                viewModel.addLoan(
                    customerId = selectedCustomer?.id.orEmpty(),
                    principal = BigDecimal.valueOf(originalAmount),
                    interestAmount = BigDecimal.valueOf(interestAmount),
                    startDate = paymentDate,
                    totalInstallments = totalInstallments
                )
                showAddLoanSheet = false
                true
            }
        )
    }
}

@Composable
fun LoanCard(
    loan: com.ijreddy.loanapp.ui.model.LoanUiModel,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = loan.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Display total instalments
                Text(
                    text = "${loan.totalInstalments} EMIs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹${String.format("%,.0f", loan.originalAmount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = loan.paymentDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

