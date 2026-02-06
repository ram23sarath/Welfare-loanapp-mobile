package com.ijreddy.loanapp.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Wrapper composable that adds swipe-to-action functionality to list items.
 * Swipe left to reveal edit action, swipe right to reveal delete action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableListItem(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    enableEdit: Boolean = true,
    enableDelete: Boolean = true,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (enableEdit) {
                        onEdit()
                    }
                    false // Don't dismiss, just trigger action
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    if (enableDelete) {
                        onDelete()
                    }
                    false // Don't dismiss, just trigger action
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { it * 0.3f } // Require 30% swipe to trigger
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            SwipeBackground(
                dismissDirection = dismissState.dismissDirection,
                enableEdit = enableEdit,
                enableDelete = enableDelete
            )
        },
        enableDismissFromStartToEnd = enableEdit,
        enableDismissFromEndToStart = enableDelete,
        content = { content() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(
    dismissDirection: SwipeToDismissBoxValue,
    enableEdit: Boolean,
    enableDelete: Boolean
) {
    val color by animateColorAsState(
        targetValue = when (dismissDirection) {
            SwipeToDismissBoxValue.StartToEnd -> if (enableEdit) Color(0xFF10B981) else Color.Transparent
            SwipeToDismissBoxValue.EndToStart -> if (enableDelete) Color(0xFFEF4444) else Color.Transparent
            SwipeToDismissBoxValue.Settled -> Color.Transparent
        },
        label = "backgroundColor"
    )
    
    val alignment = when (dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }
    
    val icon = when (dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
        SwipeToDismissBoxValue.Settled -> Icons.Default.Delete
    }
    
    val scale by animateFloatAsState(
        targetValue = if (dismissDirection == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
        label = "iconScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        if (dismissDirection != SwipeToDismissBoxValue.Settled) {
            Icon(
                imageVector = icon,
                contentDescription = when (dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> "Edit"
                    SwipeToDismissBoxValue.EndToStart -> "Delete"
                    else -> null
                },
                modifier = Modifier.scale(scale),
                tint = Color.White
            )
        }
    }
}
