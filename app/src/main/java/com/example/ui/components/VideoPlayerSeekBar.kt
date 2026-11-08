package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.UrgencyLevel

@Composable
fun VideoPlayerSeekBar(
    progress: Float, // 0.0f to 1.0f
    accentColor: Color,
    urgencyLevel: UrgencyLevel,
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    trackHeight: Dp = 8.dp,
    showThumb: Boolean = true,
    trackColorOverride: Color? = null,
    activeColorOverride: Color? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0.0f, 1.0f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "seekBarProgress"
    )

    val activeColor = activeColorOverride ?: when (urgencyLevel) {
        UrgencyLevel.EXPIRED -> Color(0xFF94A3B8)
        UrgencyLevel.CRITICAL -> Color(0xFFF97316) // Bento Orange 500
        UrgencyLevel.WARNING -> Color(0xFFF59E0B) // Amber
        UrgencyLevel.COMPLETED -> Color(0xFF10B981) // Emerald Green
        UrgencyLevel.YET_TO_START -> Color(0xFF6366F1) // Indigo / Blue
        UrgencyLevel.NORMAL -> accentColor
    }

    val trackColor = trackColorOverride ?: when (urgencyLevel) {
        UrgencyLevel.EXPIRED -> Color(0xFFE2E8F0)
        UrgencyLevel.CRITICAL -> Color(0xFFFED7AA).copy(alpha = 0.6f) // Bento Orange Track
        UrgencyLevel.WARNING -> Color(0xFFFDE68A).copy(alpha = 0.6f)
        UrgencyLevel.COMPLETED -> Color(0xFFA7F3D0).copy(alpha = 0.6f)
        UrgencyLevel.YET_TO_START -> Color(0xFFE0E7FF).copy(alpha = 0.6f)
        UrgencyLevel.NORMAL -> activeColor.copy(alpha = 0.2f)
    }

    val thumbColor = Color.White
    val isExpiredOrDone = urgencyLevel == UrgencyLevel.EXPIRED || urgencyLevel == UrgencyLevel.COMPLETED

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .testTag("video_player_seek_bar")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val trackH = trackHeight.toPx()
            val topOffset = (canvasHeight - trackH) / 2f
            val corner = CornerRadius(trackH / 2f, trackH / 2f)

            // 1. Inactive/Remaining background track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, topOffset),
                size = Size(canvasWidth, trackH),
                cornerRadius = corner,
                style = Fill
            )

            // 2. Elapsed progress bar
            val elapsedW = (canvasWidth * animatedProgress).coerceAtLeast(if (animatedProgress > 0f) trackH else 0f)
            if (elapsedW > 0f) {
                drawRoundRect(
                    color = activeColor,
                    topLeft = Offset(0f, topOffset),
                    size = Size(elapsedW, trackH),
                    cornerRadius = corner,
                    style = Fill
                )
            }

            // 3. Circular scrubber handle/thumb matching Bento HTML
            // HTML: <div class='absolute w-4 h-4 bg-white border-2 border-orange-500 rounded-full -top-1 shadow-md'></div>
            if (showThumb && !isExpiredOrDone) {
                val thumbRadius = 8.dp.toPx() // 16dp diameter
                val thumbX = (canvasWidth * animatedProgress).coerceIn(thumbRadius, canvasWidth - thumbRadius)
                val thumbY = canvasHeight / 2f

                // Drop shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.18f),
                    radius = thumbRadius + 1.5f,
                    center = Offset(thumbX, thumbY + 1.5f)
                )

                // White circle body
                drawCircle(
                    color = thumbColor,
                    radius = thumbRadius,
                    center = Offset(thumbX, thumbY)
                )

                // Border ring (2dp) of active color
                drawCircle(
                    color = activeColor,
                    radius = thumbRadius - 1.dp.toPx(),
                    center = Offset(thumbX, thumbY),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
