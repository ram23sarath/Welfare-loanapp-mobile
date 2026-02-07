package com.ijreddy.loanapp.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A segmented control for selecting between multiple options.
 */
@Composable
fun <T> SegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelection: (T) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    selectedTextColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedTextColor: Color = MaterialTheme.colorScheme.onSurface
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val itemWidth = maxWidth / items.size
        val selectedIndex = items.indexOf(selectedItem)
        
        val indicatorOffset by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = tween(durationMillis = 200),
            label = "indicator"
        )

        // Indicator
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(itemWidth)
                .fillMaxHeight()
                .padding(4.dp)
                .clip(RoundedCornerShape(cornerRadius - 4.dp))
                .background(color)
        )

        // Text Labels
        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .fillMaxHeight()
                        .clickable { onItemSelection(item) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemLabel(item),
                        color = if (index == selectedIndex) selectedTextColor else unselectedTextColor,
                        fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
