package com.ijreddy.loanapp.ui.data

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import com.ijreddy.loanapp.ui.common.DataEntryCard
import com.ijreddy.loanapp.ui.common.DataEntryType
import com.ijreddy.loanapp.ui.common.SwipeableListItem
import com.ijreddy.loanapp.ui.sheets.RecordDataEntryBottomSheet

/**
 * Data entries screen with segmented control for type filtering.
 * Replaces DataPage table layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEntriesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DataViewModel = hiltViewModel()
) {
    val entries by viewModel.entries.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Entries") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Segmented button row for type filter
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SegmentedButton(
                    selected = selectedType == null,
                    onClick = { viewModel.setTypeFilter(null) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4),
                    label = { Text("All") }
                )
                SegmentedButton(
                    selected = selectedType == DataEntryType.CREDIT,
                    onClick = { viewModel.setTypeFilter(DataEntryType.CREDIT) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4),
                    label = { Text("Credit") }
                )
                SegmentedButton(
                    selected = selectedType == DataEntryType.DEBIT,
                    onClick = { viewModel.setTypeFilter(DataEntryType.DEBIT) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4),
                    label = { Text("Debit") }
                )
                SegmentedButton(
                    selected = selectedType == DataEntryType.EXPENSE,
                    onClick = { viewModel.setTypeFilter(DataEntryType.EXPENSE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4),
                    label = { Text("Expense") }
                )
            }
            
            // Entry list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (entries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No entries found")
                        }
                    }
                } else {
                    items(entries, key = { it.id }) { entry ->
                        SwipeableListItem(
                            onEdit = { },
                            onDelete = { }
                        ) {
                            DataEntryCard(
                                customerName = entry.customerName,
                                amount = entry.amount,
                                date = entry.date,
                                receiptNumber = entry.receiptNumber,
                                type = entry.type,
                                notes = entry.notes,
                                subtype = entry.subtype,
                                onTap = { },
                                onLongPress = { }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        RecordDataEntryBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { amount, date, type, description, category ->
                viewModel.addDataEntry(amount, date, type, description, category)
                showAddSheet = false
            }
        )
    }
}
