package com.ijreddy.loanapp.ui.seniority

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.ui.common.SwipeableListItem
import com.ijreddy.loanapp.ui.dialogs.SoftDeleteDialog
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanSeniorityScreen(
    onNavigateBack: () -> Unit,
    viewModel: SeniorityViewModel = hiltViewModel()
) {
    val seniorityList by viewModel.seniorityWithCustomers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var deleteId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search by name or phone") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text("Loan Seniority")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) viewModel.setSearchQuery("")
                    }) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCustomerPicker = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add to seniority")
            }
        }
    ) { padding ->
        if (seniorityList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No seniority entries yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(seniorityList, key = { it.seniority.id }) { item ->
                    SwipeableListItem(
                        onEdit = { },
                        onDelete = { deleteId = item.seniority.id }
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${item.seniority.position}. ${item.customerName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = item.customerPhone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                item.seniority.loan_type?.let { loanType ->
                                    Text(
                                        text = "Type: $loanType",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                item.seniority.loan_request_date?.let { requestDate ->
                                    Text(
                                        text = "Requested: $requestDate",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                item.seniority.station_name?.let { station ->
                                    Text(
                                        text = "Station: $station",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomerPicker) {
        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = { Text("Select Customer") },
            text = {
                if (customers.isEmpty()) {
                    Text("No customers available")
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(customers, key = { it.id }) { customer ->
                                TextButton(
                                    onClick = {
                                        selectedCustomer = customer
                                        showCustomerPicker = false
                                        showAddSheet = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(customer.name)
                                }
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

    if (showAddSheet && selectedCustomer != null) {
        AddSeniorityBottomSheet(
            customer = selectedCustomer!!,
            onDismiss = { showAddSheet = false },
            onSave = { station, type, date ->
                coroutineScope.launch {
                    viewModel.addToSeniority(
                        customerId = selectedCustomer!!.id,
                        stationName = station,
                        loanType = type,
                        loanRequestDate = date
                    )
                }
                showAddSheet = false
            }
        )
    }

    deleteId?.let { entryId ->
        SoftDeleteDialog(
            isOpen = true,
            itemName = "Seniority entry",
            onConfirm = {
                coroutineScope.launch {
                    viewModel.removeFromSeniority(entryId)
                }
                deleteId = null
            },
            onDismiss = { deleteId = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSeniorityBottomSheet(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onSave: (station: String?, type: String?, date: String?) -> Unit
) {
    var stationName by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf("") }
    var requestDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add ${customer.name} to Queue",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = stationName,
                onValueChange = { stationName = it },
                label = { Text("Station (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = loanType,
                onValueChange = { loanType = it },
                label = { Text("Loan Type (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = requestDate,
                onValueChange = {},
                label = { Text("Request Date (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select date")
                    }
                }
            )

            Button(
                onClick = {
                    onSave(
                        stationName.takeIf { it.isNotBlank() },
                        loanType.takeIf { it.isNotBlank() },
                        requestDate.takeIf { it.isNotBlank() }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add to Queue")
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
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        requestDate = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
