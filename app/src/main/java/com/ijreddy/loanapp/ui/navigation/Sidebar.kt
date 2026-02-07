package com.ijreddy.loanapp.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ijreddy.loanapp.ui.model.NavItem

/**
 * All navigation items matching the web app's Sidebar.tsx allNavItems array.
 */
val allNavItems = listOf(
    NavItem(
        route = Screen.AddCustomer.route,
        label = "Add Customer",
        icon = Icons.Filled.PersonAdd,
        isAdminOnly = true
    ),
    NavItem(
        route = Screen.AddRecord.route,
        label = "Add Record",
        icon = Icons.Filled.NoteAdd,
        isAdminOnly = true
    ),
    NavItem(
        route = Screen.Customers.route,
        label = "Customers",
        icon = Icons.Filled.People,
        isAdminOnly = true
    ),
    NavItem(
        route = Screen.Loans.route,
        label = "Loans",
        icon = Icons.Filled.AttachMoney
    ),
    NavItem(
        route = Screen.LoanSeniority.route,
        label = "Loan Seniority",
        icon = Icons.Filled.Star
    ),
    NavItem(
        route = Screen.Subscriptions.route,
        label = "Subscriptions",
        icon = Icons.Filled.History
    ),
    NavItem(
        route = Screen.Data.route,
        label = "Expenditure",
        icon = Icons.Filled.Storage
    ),
    NavItem(
        route = Screen.Summary.route,
        label = "Summary",
        icon = Icons.Filled.PieChart
    )
)

// Sidebar dimensions matching web app
private val SIDEBAR_EXPANDED_WIDTH = 256.dp
private val SIDEBAR_COLLAPSED_WIDTH = 80.dp

/**
 * Main Sidebar composable for tablet/desktop navigation.
 * Supports collapsed/expanded states with animated transitions.
 *
 * @param currentRoute The currently active route
 * @param isScopedCustomer Whether the user is a scoped customer (limited navigation)
 * @param isCollapsed Whether the sidebar is in collapsed (icon-only) mode
 * @param userName User's display name for avatar
 * @param userInitials User's initials for avatar fallback
 * @param onNavigate Callback when a navigation item is clicked
 * @param onToggleCollapse Callback to toggle collapsed state
 * @param onLogout Callback when logout is clicked
 */
@Composable
fun Sidebar(
    currentRoute: String?,
    isScopedCustomer: Boolean = false,
    isCollapsed: Boolean = false,
    userName: String = "",
    userInitials: String = "",
    onNavigate: (String) -> Unit,
    onToggleCollapse: () -> Unit = {},
    onLogout: () -> Unit
) {
    // Filter nav items based on user role
    val navItems = remember(isScopedCustomer) {
        val items = allNavItems.filter { !it.isAdminOnly || !isScopedCustomer }
        if (isScopedCustomer) {
            // Add Home item for scoped customers at the beginning
            listOf(
                NavItem(
                    route = Screen.Dashboard.route,
                    label = "Home",
                    icon = Icons.Filled.Home
                )
            ) + items
        } else {
            items
        }
    }

    // Animate sidebar width
    val sidebarWidth by animateDpAsState(
        targetValue = if (isCollapsed) SIDEBAR_COLLAPSED_WIDTH else SIDEBAR_EXPANDED_WIDTH,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "sidebar_width"
    )

    Surface(
        modifier = Modifier
            .width(sidebarWidth)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        shadowElevation = 4.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 16.dp)
        ) {
            // Header with hamburger toggle
            SidebarHeader(
                isCollapsed = isCollapsed,
                onToggleCollapse = onToggleCollapse
            )

            Spacer(modifier = Modifier.height(8.dp))

            // User avatar section
            SidebarAvatar(
                userName = userName,
                userInitials = userInitials,
                isCollapsed = isCollapsed
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Section label
            AnimatedVisibility(
                visible = !isCollapsed,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Navigate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    letterSpacing = 1.sp
                )
            }

            // Navigation items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                navItems.forEach { item ->
                    SidebarItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        isCollapsed = isCollapsed,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Logout button
            SidebarItem(
                item = NavItem(
                    route = "logout",
                    label = "Logout",
                    icon = Icons.AutoMirrored.Filled.ExitToApp
                ),
                isSelected = false,
                isCollapsed = isCollapsed,
                onClick = onLogout
            )

            // Footer
            AnimatedVisibility(
                visible = !isCollapsed,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Loan Management",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Sidebar header with app title and collapse toggle.
 */
@Composable
private fun SidebarHeader(
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = !isCollapsed,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "Loans",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        IconButton(onClick = onToggleCollapse) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = if (isCollapsed) "Expand sidebar" else "Collapse sidebar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * User avatar section showing profile picture or initials.
 */
@Composable
private fun SidebarAvatar(
    userName: String,
    userInitials: String,
    isCollapsed: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle with initials
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userInitials.ifEmpty { "U" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        AnimatedVisibility(
            visible = !isCollapsed,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = userName.ifEmpty { "User" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Individual sidebar navigation item with animation.
 */
@Composable
fun SidebarItem(
    item: NavItem,
    isSelected: Boolean,
    isCollapsed: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate scale on press (mimics web hover effect)
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "item_scale"
    )

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = if (isCollapsed) 8.dp else 12.dp,
                    vertical = 12.dp
                ),
            horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )

            AnimatedVisibility(
                visible = !isCollapsed,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = contentColor,
                    modifier = Modifier.padding(start = 12.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
