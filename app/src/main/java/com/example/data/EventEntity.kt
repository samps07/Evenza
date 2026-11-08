package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countdown_events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTimeMillis: Long,
    val targetTimeMillis: Long,
    val icon: String = "⏳",
    val colorHex: String = "#6366F1",
    val category: String = "General",
    val priority: Int = 0,
    val createdDateMillis: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val customOrder: Int = 0,
    val notify1DayBefore: Boolean = false,
    val notify6HoursBefore: Boolean = false,
    val notify1HourBefore: Boolean = false,
    val notifyAtExpiry: Boolean = true,
    val linkUrl: String = "",
    val imageUrl: String = "",
    val customRemindersJson: String = ""
)
