package com.ijreddy.loanapp.ui.records.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ijreddy.loanapp.ui.records.SubscriptionFormState
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionForm(
    state: SubscriptionFormState,
    onStateChange: (SubscriptionFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Amount
        OutlinedTextField(
            value = state.amount,
            onValueChange = { 
                if (it.all { c -> c.isDigit() || c == '.' }) {
                    onStateChange(state.copy(amount = it))
                }
            },
            label = { Text("Amount (₹)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            prefix = { Text("₹ ") }
        )

        // Date
        OutlinedTextField(
            value = state.date,
            onValueChange = {},
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Select date")
                }
            }
        )

        // Receipt Number
        OutlinedTextField(
            value = state.receipt,
            onValueChange = { onStateChange(state.copy(receipt = it)) },
            label = { Text("Receipt Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Late Fee
        OutlinedTextField(
            value = state.lateFee,
            onValueChange = { 
                if (it.all { c -> c.isDigit() || c == '.' }) {
                    onStateChange(state.copy(lateFee = it))
                }
            },
            label = { Text("Late Fee (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            prefix = { Text("₹ ") }
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
                            onStateChange(state.copy(date = date.format(DateTimeFormatter.ISO_LOCAL_DATE)))
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
