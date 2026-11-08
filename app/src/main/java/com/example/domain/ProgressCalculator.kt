package com.example.domain

import com.example.data.EventEntity
import kotlin.math.max

enum class UrgencyLevel {
    YET_TO_START,
    NORMAL,
    WARNING,
    CRITICAL,
    EXPIRED,
    COMPLETED
}

data class EventProgress(
    val progress: Float, // 0.0f to 1.0f
    val remainingMillis: Long,
    val isExpired: Boolean,
    val isCompleted: Boolean,
    val isYetToStart: Boolean = false,
    val formattedRemaining: String,
    val exactRemainingText: String,
    val urgencyLevel: UrgencyLevel,
    val percentText: String
)

object ProgressCalculator {

    fun calculate(event: EventEntity, nowMillis: Long = System.currentTimeMillis()): EventProgress {
        if (event.isCompleted) {
            return EventProgress(
                progress = 1.0f,
                remainingMillis = 0L,
                isExpired = false,
                isCompleted = true,
                isYetToStart = false,
                formattedRemaining = "Completed",
                exactRemainingText = "Completed",
                urgencyLevel = UrgencyLevel.COMPLETED,
                percentText = "100%"
            )
        }

        val start = event.startTimeMillis
        val end = event.targetTimeMillis

        // If current time is before the start time, event is yet to start
        if (nowMillis < start) {
            val untilStart = start - nowMillis
            val shortUntil = formatDurationShort(untilStart)
            return EventProgress(
                progress = 0.0f,
                remainingMillis = end - nowMillis,
                isExpired = false,
                isCompleted = false,
                isYetToStart = true,
                formattedRemaining = "Yet to start",
                exactRemainingText = "Starts in " + formatExactDuration(untilStart),
                urgencyLevel = UrgencyLevel.YET_TO_START,
                percentText = "0%"
            )
        }

        val totalDuration = max(1000L, end - start)
        val elapsed = nowMillis - start

        val rawProgress = elapsed.toFloat() / totalDuration.toFloat()
        val progress = rawProgress.coerceIn(0.0f, 1.0f)
        val remaining = end - nowMillis
        val isExpired = remaining <= 0

        val formattedRemaining = if (isExpired) {
            val expiredAgo = nowMillis - end
            val agoText = formatDurationShort(expiredAgo)
            if (agoText.isNotEmpty()) "Expired ($agoText ago)" else "Expired"
        } else {
            "${formatDurationShort(remaining)} left"
        }

        val exactRemainingText = if (isExpired) {
            "Expired"
        } else {
            formatExactDuration(remaining)
        }

        val percentInt = (progress * 100).toInt().coerceIn(0, 100)
        val percentText = "$percentInt%"

        val urgency = when {
            isExpired -> UrgencyLevel.EXPIRED
            remaining < 24 * 3600 * 1000L -> UrgencyLevel.CRITICAL // < 24 hours
            remaining < 3 * 24 * 3600 * 1000L -> UrgencyLevel.WARNING // < 3 days
            else -> UrgencyLevel.NORMAL
        }

        return EventProgress(
            progress = progress,
            remainingMillis = remaining,
            isExpired = isExpired,
            isCompleted = false,
            isYetToStart = false,
            formattedRemaining = formattedRemaining,
            exactRemainingText = exactRemainingText,
            urgencyLevel = urgency,
            percentText = percentText
        )
    }

    private fun formatDurationShort(durationMillis: Long): String {
        if (durationMillis <= 0) return "0 m"

        val minutes = durationMillis / 60000L
        val hours = minutes / 60
        val days = hours / 24
        val months = days / 30

        return when {
            months > 0 -> {
                val remDays = days % 30
                if (remDays > 0) "$months mo $remDays d" else "$months mo"
            }
            days >= 2 -> "$days days"
            days == 1L -> {
                val remHours = hours % 24
                if (remHours > 0) "1 day $remHours h" else "1 day"
            }
            hours >= 1 -> {
                val remMinutes = minutes % 60
                if (remMinutes > 0 && hours < 6) "$hours h $remMinutes m" else "$hours hours"
            }
            minutes >= 1 -> "$minutes m"
            else -> "< 1 min"
        }
    }

    fun formatExactDuration(durationMillis: Long): String {
        if (durationMillis <= 0) return "0 minutes remaining"

        val minutes = durationMillis / 60000L
        if (minutes < 1) return "< 1 minute remaining"

        val min = minutes % 60
        val hours = minutes / 60
        val hr = hours % 24
        val days = hours / 24

        val parts = mutableListOf<String>()
        if (days > 0) parts.add("$days day${if (days > 1) "s" else ""}")
        if (hr > 0) parts.add("$hr hour${if (hr > 1) "s" else ""}")
        if (min > 0) parts.add("$min minute${if (min > 1) "s" else ""}")

        return if (parts.isEmpty()) "< 1 minute remaining" else parts.joinToString(" ") + " remaining"
    }
}
