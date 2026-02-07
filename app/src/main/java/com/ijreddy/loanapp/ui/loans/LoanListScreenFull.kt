package com.ijreddy.loanapp.ui.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.ijreddy.loanapp.ui.components.PullToRefreshContainer
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ijreddy.loanapp.ui.common.ContextMenuAction
import com.ijreddy.loanapp.ui.common.ContextMenuDropdown
import com.ijreddy.loanapp.ui.common.LoanCard
import com.ijreddy.loanapp.ui.common.SwipeableListItem
import com.ijreddy.loanapp.ui.dialogs.SoftDeleteDialog
import com.ijreddy.loanapp.ui.sheets.RecordLoanBottomSheet
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


/**
 * Full loan list screen with swipe actions and pull-to-refresh.
 * Replaces LoanTableView from web with card-based layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanListScreenFull(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var showAddLoanSheet by remember { mutableStateOf(false) }
    var contextMenuLoanId by remember { mutableStateOf<String?>(null) }
    var deleteDialogLoanId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    // Mock data - replace with ViewModel
    val loans = remember {
        listOf(
            LoanData("1", "Priya Sharma", 50000.0, 5000.0, "2024-01-15", 12, 8, null),
            LoanData("2", "Rahul Verma", 30000.0, 3000.0, "2024-06-10", 6, 6, "CHK-001"),
            LoanData("3", "Sneha Reddy", 100000.0, 10000.0, "2024-03-20", 24, 12, null),
            LoanData("4", "Amit Kumar", 25000.0, 2500.0, "2024-08-05", 10, 3, "CHK-002"),
            LoanData("5", "Kavitha Nair", 75000.0, 7500.0, "2024-02-28", 18, 15, null)
        )
    }
    
    val filteredLoans = if (searchQuery.isBlank()) loans else {
        loans.filter { it.customerName.contains(searchQuery, ignoreCase = true) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search loans...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text("Loans")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) searchQuery = ""
                    }) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = { /* Show filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddLoanSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Loan")
            }
        },
        modifier = modifier
    ) { padding ->
        val scope = rememberCoroutineScope()
        
        PullToRefreshContainer(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                scope.launch {
                    delay(1000)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredLoans, key = { it.id }) { loan ->
                    Box {
                        SwipeableListItem(
                            onEdit = { /* Edit loan */ },
                            onDelete = { deleteDialogLoanId = loan.id }
                        ) {
                            LoanCard(
                                customerName = loan.customerName,
                                originalAmount = loan.originalAmount,
                                interestAmount = loan.interestAmount,
                                paymentDate = loan.paymentDate,
                                totalInstallments = loan.totalInstallments,
                                paidInstallments = loan.paidInstallments,
                                checkNumber = loan.checkNumber,
                                onTap = { onNavigateToDetail(loan.id) },
                                onLongPress = { contextMenuLoanId = loan.id }
                            )
                        }
                        
                        // Context menu
                        ContextMenuDropdown(
                            expanded = contextMenuLoanId == loan.id,
                            onDismiss = { contextMenuLoanId = null },
                            onView = { onNavigateToDetail(loan.id) },
                            onEdit = { /* Navigate to edit */ },
                            onDelete = { deleteDialogLoanId = loan.id },
                            customActions = listOf(
                                ContextMenuAction(
                                    label = "Record Installment",
                                    icon = Icons.Default.Payment,
                                    onClick = { /* Show installment sheet */ }
                                )
                            )
                        )
                    }
                }
            }
        }
    }
    
    // Add loan bottom sheet
    if (showAddLoanSheet) {
        RecordLoanBottomSheet(
            customerId = "", // Select customer first in real implementation
            customerName = "Select Customer",
            onDismiss = { showAddLoanSheet = false },
            onSave = { _, _, _, _, _ ->
                showAddLoanSheet = false
                true
            }
        )
    }
    
    // Delete confirmation dialog
    deleteDialogLoanId?.let { loanId ->
        val loan = loans.find { it.id == loanId }
        SoftDeleteDialog(
            isOpen = true,
            itemName = "Loan for ${loan?.customerName ?: "Unknown"}",
            onConfirm = {
                // Delete loan
                deleteDialogLoanId = null
            },
            onDismiss = { deleteDialogLoanId = null }
        )
    }
}

private suspend fun launch(block: suspend () -> Unit) {
    kotlinx.coroutines.coroutineScope { block() }
}

private data class LoanData(
    val id: String,
    val customerName: String,
    val originalAmount: Double,
    val interestAmount: Double,
    val paymentDate: String,
    val totalInstallments: Int,
    val paidInstallments: Int,
    val checkNumber: String?
)
