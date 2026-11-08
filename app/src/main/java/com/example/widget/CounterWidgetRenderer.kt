package com.example.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.domain.UrgencyLevel

object CounterWidgetRenderer {

    fun renderSeekBarBitmap(
        progress: Float, // 0.0f to 1.0f
        accentColorHex: String,
        urgency: UrgencyLevel,
        width: Int = 480,
        height: Int = 24
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val strokeH = height * 0.42f
        val top = (height - strokeH) / 2f
        val bottom = top + strokeH
        val cornerRadius = strokeH / 2f

        // Bento Colors
        val (activeColor, trackColor) = when (urgency) {
            UrgencyLevel.YET_TO_START -> Pair(Color.parseColor("#818CF8"), Color.parseColor("#27272A"))
            UrgencyLevel.EXPIRED -> Pair(Color.parseColor("#94A3B8"), Color.parseColor("#334155"))
            UrgencyLevel.CRITICAL -> Pair(Color.parseColor("#F97316"), Color.parseColor("#7C2D12")) // Bento Orange
            UrgencyLevel.WARNING -> Pair(Color.parseColor("#3B82F6"), Color.parseColor("#1E3A5F")) // Bento Blue
            UrgencyLevel.COMPLETED -> Pair(Color.parseColor("#10B981"), Color.parseColor("#064E3B")) // Green
            UrgencyLevel.NORMAL -> {
                val parsed = try { Color.parseColor(accentColorHex) } catch (_: Exception) { Color.parseColor("#3B82F6") }
                Pair(parsed, Color.parseColor("#334155"))
            }
        }

        // 1. Background track
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = trackColor
            style = Paint.Style.FILL
        }
        val trackRect = RectF(0f, top, width.toFloat(), bottom)
        canvas.drawRoundRect(trackRect, cornerRadius, cornerRadius, trackPaint)

        // 2. Elapsed progress bar
        val clampedProgress = progress.coerceIn(0.0f, 1.0f)
        val elapsedWidth = (width.toFloat() * clampedProgress).coerceAtLeast(if (urgency == UrgencyLevel.YET_TO_START) 0f else strokeH)

        if (elapsedWidth > 0f) {
            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = activeColor
                style = Paint.Style.FILL
            }
            val progressRect = RectF(0f, top, elapsedWidth, bottom)
            canvas.drawRoundRect(progressRect, cornerRadius, cornerRadius, progressPaint)
        }

        // 3. Circular scrubber handle (only if active)
        if (urgency != UrgencyLevel.EXPIRED && urgency != UrgencyLevel.COMPLETED && urgency != UrgencyLevel.YET_TO_START) {
            val handleRadius = height * 0.44f
            val handleX = (width.toFloat() * clampedProgress).coerceIn(handleRadius, width.toFloat() - handleRadius)
            val handleY = height / 2f

            // Handle shadow
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(90, 0, 0, 0)
                style = Paint.Style.FILL
            }
            canvas.drawCircle(handleX, handleY + 1.5f, handleRadius + 1.5f, shadowPaint)

            // Outer white handle
            val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(handleX, handleY, handleRadius, handlePaint)

            // Outer border ring in active color
            val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = activeColor
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawCircle(handleX, handleY, handleRadius - 1.5f, ringPaint)
        }

        return bitmap
    }
}
