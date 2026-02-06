package com.ijreddy.loanapp.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Dialog shown when user has been inactive for too long.
 * Replaces InactivityLogoutModal from web.
 */
@Composable
fun InactivityLogoutDialog(
    isOpen: Boolean,
    remainingSeconds: Int = 60,
    onStayLoggedIn: () -> Unit,
    onLogout: () -> Unit
) {
    var countdown by remember(remainingSeconds) { mutableStateOf(remainingSeconds) }
    
    LaunchedEffect(isOpen, remainingSeconds) {
        if (isOpen) {
            countdown = remainingSeconds
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            if (countdown <= 0) {
                onLogout()
            }
        }
    }
    
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onStayLoggedIn,
            icon = {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text("Session Timeout")
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("You've been inactive for a while.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("You will be logged out in:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$countdown seconds",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (countdown <= 10) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            },
            confirmButton = {
                Button(onClick = onStayLoggedIn) {
                    Text("Stay Logged In")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onLogout) {
                    Text("Logout Now")
                }
            }
        )
    }
}
