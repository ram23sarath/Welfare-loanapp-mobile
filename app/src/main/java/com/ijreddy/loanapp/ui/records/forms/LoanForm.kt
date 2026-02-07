package com.ijreddy.loanapp.ui.records.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ijreddy.loanapp.ui.records.LoanFormState
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanForm(
    state: LoanFormState,
    onStateChange: (LoanFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Original Amount
        OutlinedTextField(
            value = state.originalAmount,
            onValueChange = { 
                if (it.all { c -> c.isDigit() || c == '.' }) {
                    onStateChange(state.copy(originalAmount = it))
                }
            },
            label = { Text("Original Amount (₹)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            prefix = { Text("₹ ") }
        )

        // Total Repayable Amount
        OutlinedTextField(
            value = state.totalRepayable,
            onValueChange = {
                if (it.all { c -> c.isDigit() || c == '.' }) {
                    onStateChange(state.copy(totalRepayable = it))
                }
            },
            label = { Text("Total Repayable Amount (₹)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            prefix = { Text("₹ ") },
            supportingText = {
                val original = state.originalAmount.toDoubleOrNull()
                val total = state.totalRepayable.toDoubleOrNull()
                if (original != null && total != null && total < original) {
                    Text("Must be ≥ Original Amount", color = MaterialTheme.colorScheme.error)
                }
            }
        )

        // Installment Details (Derived)
        val original = state.originalAmount.toBigDecimalOrNull()
        val total = state.totalRepayable.toBigDecimalOrNull()
        val installments = state.totalInstallments.toIntOrNull()

        if (total != null && installments != null && installments > 0) {
            val perInstallment = total.divide(BigDecimal(installments), 2, RoundingMode.HALF_UP)
            
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Per Installment:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "₹ $perInstallment",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Total Installments
        OutlinedTextField(
            value = state.totalInstallments,
            onValueChange = { 
                if (it.all { c -> c.isDigit() }) {
                    onStateChange(state.copy(totalInstallments = it))
                }
            },
            label = { Text("Total Installments") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        // Payment Date
        OutlinedTextField(
            value = state.paymentDate,
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

        // Check Number
        OutlinedTextField(
            value = state.checkNumber,
            onValueChange = { onStateChange(state.copy(checkNumber = it)) },
            label = { Text("Check Number (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }

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
                            onStateChange(state.copy(paymentDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
