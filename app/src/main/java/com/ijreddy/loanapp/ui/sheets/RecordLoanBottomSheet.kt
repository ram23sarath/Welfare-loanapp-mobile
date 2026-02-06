package com.ijreddy.loanapp.ui.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Bottom sheet for recording a new loan.
 * Replaces RecordLoanModal from web.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordLoanBottomSheet(
    customerId: String,
    customerName: String,
    onDismiss: () -> Unit,
    onSave: suspend (originalAmount: Double, interestAmount: Double, paymentDate: String, totalInstallments: Int, checkNumber: String?) -> Boolean,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var originalAmount by remember { mutableStateOf("") }
    var interestAmount by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var totalInstallments by remember { mutableStateOf("12") }
    var checkNumber by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    val isValid = originalAmount.toDoubleOrNull() != null && 
                  interestAmount.toDoubleOrNull() != null &&
                  totalInstallments.toIntOrNull() != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Record Loan",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Customer: $customerName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            HorizontalDivider()
            
            // Original Amount
            OutlinedTextField(
                value = originalAmount,
                onValueChange = { originalAmount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Original Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("₹ ") }
            )
            
            // Interest Amount
            OutlinedTextField(
                value = interestAmount,
                onValueChange = { interestAmount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Interest Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("₹ ") }
            )
            
            // Total display
            val total = (originalAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO)
                .add(interestAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO)
            if (total > BigDecimal.ZERO) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "₹ ${total.setScale(2, RoundingMode.HALF_UP)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Payment Date
            OutlinedTextField(
                value = paymentDate,
                onValueChange = {},
                label = { Text("Payment Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select date")
                    }
                }
            )
            
            // Total Installments
            OutlinedTextField(
                value = totalInstallments,
                onValueChange = { totalInstallments = it.filter { c -> c.isDigit() } },
                label = { Text("Total Installments") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            
            // Check Number (optional)
            OutlinedTextField(
                value = checkNumber,
                onValueChange = { checkNumber = it },
                label = { Text("Check Number (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Error message
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (isValid) {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val success = onSave(
                                        originalAmount.toDouble(),
                                        interestAmount.toDouble(),
                                        paymentDate,
                                        totalInstallments.toInt(),
                                        checkNumber.ifBlank { null }
                                    )
                                    if (success) {
                                        onDismiss()
                                    } else {
                                        errorMessage = "Failed to save loan"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "An error occurred"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isValid && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Loan")
                    }
                }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isValid && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Loan")
                    }
                }
            }
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            paymentDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
