package com.example.ai

object ExpiryCategoryDefaults {

    data class ReminderDefaults(
        val notify1DayBefore: Boolean,
        val notify6HoursBefore: Boolean,
        val notify1HourBefore: Boolean,
        val notifyAtExpiry: Boolean,
        val defaultIcon: String,
        val defaultColorHex: String
    )

    fun getDefaultsForCategory(category: String): ReminderDefaults {
        return when (category.trim().lowercase()) {
            "dairy", "milk", "cheese", "yogurt" -> ReminderDefaults(
                notify1DayBefore = true,
                notify6HoursBefore = true,
                notify1HourBefore = false,
                notifyAtExpiry = true,
                defaultIcon = "🥛",
                defaultColorHex = "#3B82F6"
            )
            "food", "bakery", "meat", "produce", "fruits", "vegetables" -> ReminderDefaults(
                notify1DayBefore = true,
                notify6HoursBefore = true,
                notify1HourBefore = false,
                notifyAtExpiry = true,
                defaultIcon = "🍞",
                defaultColorHex = "#F97316"
            )
            "medicine", "pharmacy", "health", "pills", "supplements" -> ReminderDefaults(
                notify1DayBefore = true,
                notify6HoursBefore = false,
                notify1HourBefore = true,
                notifyAtExpiry = true,
                defaultIcon = "💊",
                defaultColorHex = "#EC4899"
            )
            "beverage", "drinks", "juice" -> ReminderDefaults(
                notify1DayBefore = true,
                notify6HoursBefore = true,
                notify1HourBefore = false,
                notifyAtExpiry = true,
                defaultIcon = "🧃",
                defaultColorHex = "#10B981"
            )
            "pantry", "canned", "spices", "dry goods" -> ReminderDefaults(
                notify1DayBefore = true,
                notify6HoursBefore = false,
                notify1HourBefore = false,
                notifyAtExpiry = true,
                defaultIcon = "🥫",
                defaultColorHex = "#EAB308"
            )
            "cosmetics", "skincare", "beauty", "personal care" -> ReminderDefaults(
                notify1DayBefore = true,
                notify6HoursBefore = false,
                notify1HourBefore = false,
                notifyAtExpiry = true,
                defaultIcon = "💄",
                defaultColorHex = "#8B5CF6"
            )
            else -> ReminderDefaults(
                notify1DayBefore = true,
                notify6HoursBefore = false,
                notify1HourBefore = false,
                notifyAtExpiry = true,
                defaultIcon = "⏳",
                defaultColorHex = "#6366F1"
            )
        }
    }
}
