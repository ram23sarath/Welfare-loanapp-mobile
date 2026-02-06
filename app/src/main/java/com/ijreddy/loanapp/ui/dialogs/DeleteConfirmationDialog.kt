package com.ijreddy.loanapp.ui.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Confirmation dialog for delete actions.
 * Replaces DeleteConfirmationModal from web.
 */
@Composable
fun DeleteConfirmationDialog(
    isOpen: Boolean,
    title: String = "Confirm Delete",
    message: String,
    confirmText: String = "Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isPermanent: Boolean = false
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = if (isPermanent) Icons.Default.Warning else Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(title)
            },
            text = {
                Text(message)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Warning dialog for permanent deletion (cannot be restored).
 */
@Composable
fun PermanentDeleteDialog(
    isOpen: Boolean,
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DeleteConfirmationDialog(
        isOpen = isOpen,
        title = "Permanently Delete?",
        message = "This will permanently delete \"$itemName\". This action cannot be undone.",
        confirmText = "Delete Forever",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isPermanent = true
    )
}

/**
 * Soft delete dialog (can be restored from trash).
 */
@Composable
fun SoftDeleteDialog(
    isOpen: Boolean,
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DeleteConfirmationDialog(
        isOpen = isOpen,
        title = "Move to Trash?",
        message = "\"$itemName\" will be moved to trash. You can restore it later.",
        confirmText = "Move to Trash",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isPermanent = false
    )
}
