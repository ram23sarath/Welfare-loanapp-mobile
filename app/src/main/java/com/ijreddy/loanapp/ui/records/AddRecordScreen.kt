package com.ijreddy.loanapp.ui.records

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ijreddy.loanapp.ui.components.CustomerSearchField
import com.ijreddy.loanapp.ui.components.GlassCard
import com.ijreddy.loanapp.ui.components.LoadingButton
import com.ijreddy.loanapp.ui.components.SegmentedControl
import com.ijreddy.loanapp.ui.records.forms.InstallmentForm
import com.ijreddy.loanapp.ui.records.forms.LoanForm
import com.ijreddy.loanapp.ui.records.forms.SubscriptionForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    onNavigateBack: () -> Unit,
    // Other navigation params logic might be simplified if we just pop back
    // The previous implementation had navigation to other screens, but Add Record is now a form screen.
    viewModel: AddRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val recordType by viewModel.recordType.collectAsState()
    
    // Forms
    val loanFormState by viewModel.loanForm.collectAsState()
    val subscriptionFormState by viewModel.subscriptionForm.collectAsState()
    val installmentFormState by viewModel.installmentForm.collectAsState()
    val customerLoans by viewModel.customerLoans.collectAsState()
    val existingInstallments by viewModel.existingInstallments.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Effects
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    
    // Success Dialog
    if (uiState.successMessage != null) {
        val context = androidx.compose.ui.platform.LocalContext.current
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { 
                viewModel.clearMessages()
                onNavigateBack() 
            },
            title = { Text("Success") },
            text = { Text("Record saved successfully. Share via WhatsApp?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        val customer = selectedCustomer 
                        if (customer != null) {
                            when (recordType) {
                                RecordType.LOAN -> {
                                    com.ijreddy.loanapp.ui.common.WhatsAppHelper.shareLoanSummary(
                                        context = context,
                                        phoneNumber = customer.phone,
                                        customerName = customer.name,
                                        loanAmount = loanFormState.originalAmount.toDoubleOrNull() ?: 0.0,
                                        totalInstallments = loanFormState.totalInstallments.toIntOrNull() ?: 0,
                                        paymentDate = loanFormState.paymentDate
                                    )
                                }
                                RecordType.SUBSCRIPTION -> {
                                    com.ijreddy.loanapp.ui.common.WhatsAppHelper.shareSubscriptionReceipt(
                                        context = context,
                                        phoneNumber = customer.phone,
                                        customerName = customer.name,
                                        amount = subscriptionFormState.amount.toDoubleOrNull() ?: 0.0,
                                        date = subscriptionFormState.date
                                    )
                                }
                                RecordType.INSTALLMENT -> {
                                    com.ijreddy.loanapp.ui.common.WhatsAppHelper.shareInstallmentReceipt(
                                        context = context,
                                        phoneNumber = customer.phone,
                                        customerName = customer.name,
                                        installmentNumber = installmentFormState.installmentNumber,
                                        amount = installmentFormState.amount.toDoubleOrNull() ?: 0.0,
                                        receiptNumber = installmentFormState.receipt
                                    )
                                }
                            }
                        }
                        viewModel.clearMessages()
                        onNavigateBack()
                    }
                ) { Text("Share") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.clearMessages()
                        onNavigateBack()
                    }
                ) { Text("Close") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Record") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Section
            item {
                if (selectedCustomer == null) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Select Customer",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        CustomerSearchField(
                            query = searchQuery,
                            onQueryChange = viewModel::setSearchQuery,
                            searchResults = searchResults,
                            onCustomerSelected = viewModel::selectCustomer,
                            isSearching = false // Not strictly needed
                        )
                    }
                } else {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Selected Customer",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            androidx.compose.material3.ListItem(
                                headlineContent = { Text(selectedCustomer!!.name) },
                                supportingContent = { Text(selectedCustomer!!.phone) },
                                trailingContent = {
                                    IconButton(onClick = viewModel::clearCustomer) {
                                        Icon(Icons.Default.Close, "Change customer")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (selectedCustomer != null) {
                // Record Type Selector
                item {
                    SegmentedControl(
                        items = RecordType.values().toList(),
                        selectedItem = recordType,
                        onItemSelection = viewModel::setRecordType,
                        itemLabel = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                    )
                }

                // Form Content
                item {
                    AnimatedContent(
                        targetState = recordType,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> -height } + fadeOut())
                        },
                        label = "form_transition"
                    ) { type ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            when (type) {
                                RecordType.LOAN -> {
                                    LoanForm(
                                        state = loanFormState,
                                        onStateChange = { viewModel.updateLoanForm(it) }
                                    )
                                }
                                RecordType.SUBSCRIPTION -> {
                                    SubscriptionForm(
                                        state = subscriptionFormState,
                                        onStateChange = { viewModel.updateSubscriptionForm(it) }
                                    )
                                }
                                RecordType.INSTALLMENT -> {
                                    InstallmentForm(
                                        state = installmentFormState,
                                        onStateChange = { viewModel.updateInstallmentForm(it) },
                                        customerLoans = customerLoans,
                                        existingInstallments = existingInstallments
                                    )
                                }
                            }
                        }
                    }
                }

                // Submit Button
                item {
                    LoadingButton(
                        onClick = viewModel::submit,
                        isLoading = uiState.isSaving,
                        enabled = !uiState.isSaving,
                        text = "Save Record",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
