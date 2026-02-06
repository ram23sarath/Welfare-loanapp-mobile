package com.ijreddy.loanapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Efficient lazy list with virtualization for large datasets.
 * Addresses P0: No virtualization in web app.
 */
@Composable
fun <T> EfficientLazyList(
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    isLoading: Boolean = false,
    emptyContent: @Composable () -> Unit = { EmptyListPlaceholder() },
    itemContent: @Composable (T) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading && items.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            items.isEmpty() -> {
                emptyContent()
            }
            else -> {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = items,
                        key = key
                    ) { item ->
                        itemContent(item)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyListPlaceholder(
    message: String = "No items found"
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Optimized search bar with debounced input.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    var localQuery by remember { mutableStateOf(query) }
    
    // Debounce: update parent only after typing stops
    LaunchedEffect(localQuery) {
        kotlinx.coroutines.delay(300)
        onQueryChange(localQuery)
    }
    
    OutlinedTextField(
        value = localQuery,
        onValueChange = { localQuery = it },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

/**
 * Pull-to-refresh wrapper.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()
    
    PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        content()
    }
}
