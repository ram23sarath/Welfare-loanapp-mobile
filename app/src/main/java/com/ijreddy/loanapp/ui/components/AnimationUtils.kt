package com.ijreddy.loanapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Modifier to apply a shake animation when triggered.
 */
fun Modifier.shake(enabled: Boolean, onAnimationEnd: () -> Unit = {}): Modifier = composed {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(enabled) {
        if (enabled) {
            for (i in 0..5) {
                val offset = if (i % 2 == 0) 10f else -10f
                offsetX.animateTo(
                    targetValue = offset,
                    animationSpec = tween(durationMillis = 50)
                )
            }
            offsetX.animateTo(0f)
            onAnimationEnd()
        }
    }

    this.offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) }
}
