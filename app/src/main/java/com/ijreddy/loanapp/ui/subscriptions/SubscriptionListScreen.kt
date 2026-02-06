package com.ijreddy.loanapp.ui.subscriptions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.ijreddy.loanapp.ui.components.PullToRefreshContainer
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ijreddy.loanapp.ui.common.ContextMenuDropdown
import com.ijreddy.loanapp.ui.common.SubscriptionCard
import com.ijreddy.loanapp.ui.common.SwipeableListItem
import com.ijreddy.loanapp.ui.dialogs.SoftDeleteDialog

/**
 * Subscription list screen with card layout, swipe actions, and pull-to-refresh.
 * Replaces SubscriptionTableView from web.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionListScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var isSearchExpanded by remember { mutableStateOf(false) }
    var contextMenuId by remember { mutableStateOf<String?>(null) }
    var deleteDialogId by remember { mutableStateOf<String?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
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
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Subscription")
            }
        },
        modifier = modifier
    ) { padding ->
        PullToRefreshContainer(
            isRefreshing = isLoading,
            onRefresh = { /* Sync handled by SyncManager */ },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subscriptions, key = { it.id }) { sub ->
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
    
    // Dialogs & Sheets
    deleteDialogId?.let { id ->
        val sub = subscriptions.find { it.id == id }
        SoftDeleteDialog(
            isOpen = true,
            itemName = "Subscription for ${sub?.customerName ?: "Unknown"}",
            onConfirm = { 
                viewModel.softDelete(id)
                deleteDialogId = null 
            },
            onDismiss = { deleteDialogId = null }
        )
    }
    
    if (showAddSheet) {
        // Placeholder for Add Sheet - needs to be implemented or connected
    }
}
