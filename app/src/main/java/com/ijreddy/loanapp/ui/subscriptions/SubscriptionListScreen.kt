package com.ijreddy.loanapp.ui.subscriptions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ijreddy.loanapp.ui.common.ContextMenuDropdown
import com.ijreddy.loanapp.ui.common.SubscriptionCard
import com.ijreddy.loanapp.ui.common.SwipeableListItem
import com.ijreddy.loanapp.ui.dialogs.SoftDeleteDialog
import kotlinx.coroutines.delay

/**
 * Subscription list screen with card layout, swipe actions, and pull-to-refresh.
 * Replaces SubscriptionTableView from web.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionListScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var contextMenuId by remember { mutableStateOf<String?>(null) }
    var deleteDialogId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    // Mock data
    val subscriptions = remember {
        listOf(
            SubscriptionData("1", "Priya Sharma", 2000.0, "2024-12-01", "REC-001", null),
            SubscriptionData("2", "Rahul Verma", 1500.0, "2024-12-01", "REC-002", 100.0),
            SubscriptionData("3", "Sneha Reddy", 2500.0, "2024-11-15", "REC-003", null),
            SubscriptionData("4", "Amit Kumar", 3000.0, "2024-11-01", "REC-004", 200.0),
            SubscriptionData("5", "Kavitha Nair", 1800.0, "2024-10-28", "REC-005", null)
        )
    }
    
    val filtered = if (searchQuery.isBlank()) subscriptions else {
        subscriptions.filter { it.customerName.contains(searchQuery, ignoreCase = true) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text("Subscriptions")
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
                        if (!isSearchExpanded) searchQuery = ""
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
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Subscription")
            }
        },
        modifier = modifier
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                kotlinx.coroutines.MainScope().launch {
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
                items(filtered, key = { it.id }) { sub ->
                    Box {
                        SwipeableListItem(
                            onEdit = { },
                            onDelete = { deleteDialogId = sub.id }
                        ) {
                            SubscriptionCard(
                                customerName = sub.customerName,
                                amount = sub.amount,
                                date = sub.date,
                                receiptNumber = sub.receiptNumber,
                                lateFee = sub.lateFee,
                                onTap = { },
                                onLongPress = { contextMenuId = sub.id }
                            )
                        }
                        
                        ContextMenuDropdown(
                            expanded = contextMenuId == sub.id,
                            onDismiss = { contextMenuId = null },
                            onEdit = { },
                            onDelete = { deleteDialogId = sub.id }
                        )
                    }
                }
            }
        }
    }
    
    deleteDialogId?.let { id ->
        val sub = subscriptions.find { it.id == id }
        SoftDeleteDialog(
            isOpen = true,
            itemName = "Subscription for ${sub?.customerName ?: "Unknown"}",
            onConfirm = { deleteDialogId = null },
            onDismiss = { deleteDialogId = null }
        )
    }
}

private suspend fun launch(block: suspend () -> Unit) {
    kotlinx.coroutines.coroutineScope { block() }
}

private data class SubscriptionData(
    val id: String,
    val customerName: String,
    val amount: Double,
    val date: String,
    val receiptNumber: String,
    val lateFee: Double?
)
