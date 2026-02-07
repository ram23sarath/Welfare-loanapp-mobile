package com.ijreddy.loanapp.ui.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing a navigation item in the sidebar.
 * Maps to the web app's allNavItems array structure.
 */
data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isAdminOnly: Boolean = false
)
