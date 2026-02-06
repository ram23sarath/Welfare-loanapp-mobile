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
import com.ijreddy.loanapp.ui.common.DataEntryCard
import com.ijreddy.loanapp.ui.common.DataEntryType
import com.ijreddy.loanapp.ui.common.SwipeableListItem

/**
 * Data entries screen with segmented control for type filtering.
 * Replaces DataPage table layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEntriesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf<DataEntryType?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    
    // Mock data
    val entries = remember {
        listOf(
            EntryData("1", "Priya Sharma", 5000.0, "2024-12-15", "ENT-001", DataEntryType.CREDIT, "Bonus", null),
            EntryData("2", "Rahul Verma", 1500.0, "2024-12-10", "ENT-002", DataEntryType.DEBIT, null, null),
            EntryData("3", null, 2500.0, "2024-12-08", "ENT-003", DataEntryType.EXPENSE, null, "Office Supplies"),
            EntryData("4", "Sneha Reddy", 3000.0, "2024-12-05", "ENT-004", DataEntryType.CREDIT, "Refund", null),
            EntryData("5", null, 1200.0, "2024-12-01", "ENT-005", DataEntryType.EXPENSE, null, "Travel"),
            EntryData("6", "Amit Kumar", 800.0, "2024-11-28", "ENT-006", DataEntryType.DEBIT, null, null)
        )
    }
    
    val filteredEntries = if (selectedType == null) entries else {
        entries.filter { it.type == selectedType }
    }
    
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
                    onClick = { selectedType = null },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4),
                    label = { Text("All") }
                )
                SegmentedButton(
                    selected = selectedType == DataEntryType.CREDIT,
                    onClick = { selectedType = DataEntryType.CREDIT },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4),
                    label = { Text("Credit") }
                )
                SegmentedButton(
                    selected = selectedType == DataEntryType.DEBIT,
                    onClick = { selectedType = DataEntryType.DEBIT },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4),
                    label = { Text("Debit") }
                )
                SegmentedButton(
                    selected = selectedType == DataEntryType.EXPENSE,
                    onClick = { selectedType = DataEntryType.EXPENSE },
                    shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4),
                    label = { Text("Expense") }
                )
            }
            
            // Entry list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEntries, key = { it.id }) { entry ->
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

private data class EntryData(
    val id: String,
    val customerName: String?,
    val amount: Double,
    val date: String,
    val receiptNumber: String,
    val type: DataEntryType,
    val notes: String?,
    val subtype: String?
)
