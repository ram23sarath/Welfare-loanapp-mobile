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
import com.ijreddy.loanapp.ui.common.DataEntryType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDataEntryBottomSheet(
    onDismiss: () -> Unit,
    onSave: (amount: Double, date: String, type: DataEntryType, description: String, category: String?) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DataEntryType.EXPENSE) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val isValid = amount.toDoubleOrNull() != null && description.isNotBlank()

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
            Text(
                text = "New Data Entry",
                style = MaterialTheme.typography.headlineSmall
            )

            HorizontalDivider()
            
            // Type Selection
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedType == DataEntryType.EXPENSE,
                    onClick = { selectedType = DataEntryType.EXPENSE },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    label = { Text("Expense") }
                )
                SegmentedButton(
                    selected = selectedType == DataEntryType.CREDIT,
                    onClick = { selectedType = DataEntryType.CREDIT },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    label = { Text("Credit") }
                )
                SegmentedButton(
                    selected = selectedType == DataEntryType.DEBIT,
                    onClick = { selectedType = DataEntryType.DEBIT },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    label = { Text("Debit") }
                )
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("₹ ") }
            )

            OutlinedTextField(
                value = date,
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

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            if (selectedType == DataEntryType.EXPENSE) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g. Travel, Office") }
                )
            }

            Button(
                onClick = {
                    if (isValid) {
                        isLoading = true
                        onSave(
                            amount.toDouble(),
                            date,
                            selectedType,
                            description,
                            if (category.isBlank()) null else category
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Entry")
                }
            }
        }
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        date = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}
