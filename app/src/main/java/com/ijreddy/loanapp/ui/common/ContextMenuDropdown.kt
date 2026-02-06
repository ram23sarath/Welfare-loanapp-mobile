package com.ijreddy.loanapp.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * Context menu that appears on long-press for list items.
 * Replaces hover-based actions from web UI.
 */
@Composable
fun ContextMenuDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onView: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onRestore: (() -> Unit)? = null,
    customActions: List<ContextMenuAction> = emptyList(),
    offset: DpOffset = DpOffset(0.dp, 0.dp)
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = offset
    ) {
        onView?.let {
            DropdownMenuItem(
                text = { Text("View Details") },
                onClick = {
                    onDismiss()
                    it()
                },
                leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) }
            )
        }
        
        onEdit?.let {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    onDismiss()
                    it()
                },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )
        }
        
        onRestore?.let {
            DropdownMenuItem(
                text = { Text("Restore") },
                onClick = {
                    onDismiss()
                    it()
                },
                leadingIcon = { Icon(Icons.Default.Restore, contentDescription = null) }
            )
        }
        
        customActions.forEach { action ->
            DropdownMenuItem(
                text = { Text(action.label) },
                onClick = {
                    onDismiss()
                    action.onClick()
                },
                leadingIcon = action.icon?.let { { Icon(it, contentDescription = null) } }
            )
        }
        
        onDelete?.let {
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    onDismiss()
                    it()
                },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    ) 
                },
                colors = MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}

data class ContextMenuAction(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val onClick: () -> Unit
)
