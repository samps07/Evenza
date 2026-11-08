package com.example.domain

import java.util.Calendar
import java.util.Locale

data class ParsedCounterDraft(
    val title: String,
    val targetTimeMillis: Long,
    val icon: String,
    val category: String,
    val colorHex: String,
    val detectedTimeSnippet: String
)

object NaturalLanguageParser {

    private val ICON_KEYWORDS = mapOf(
        listOf("food", "meal", "coupon", "restaurant", "burger", "pizza", "coffee", "bistro", "lunch", "dinner", "snack") to ("🍔" to ("Food" to "#FB923C")),
        listOf("movie", "cinema", "film", "show", "theatre", "concert", "ticket", "game") to ("🎬" to ("Entertainment" to "#EC4899")),
        listOf("book", "library", "read", "study", "homework", "exam") to ("📚" to ("Personal" to "#38BDF8")),
        listOf("card", "gift", "amazon", "discount", "voucher", "deal", "store", "sale", "shopping") to ("🎁" to ("Shopping" to "#A855F7")),
        listOf("train", "flight", "plane", "travel", "trip", "hotel", "bus") to ("🚆" to ("Travel" to "#3B82F6")),
        listOf("parking", "car", "drive", "toll") to ("🚗" to ("Personal" to "#F59E0B")),
        listOf("subscription", "bill", "rent", "insurance", "bank", "pay", "tax", "renew") to ("💳" to ("Finance" to "#10B981")),
        listOf("doctor", "dentist", "pill", "medicine", "health", "pharmacy") to ("💊" to ("Health" to "#EF4444")),
        listOf("warranty", "license", "passport", "contract", "permit") to ("🛡️" to ("Important" to "#6366F1"))
    )

    fun parse(input: String): ParsedCounterDraft? {
        val trimmed = input.trim()
        if (trimmed.length < 3) return null

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }

        val lower = trimmed.lowercase(Locale.ROOT)
        var detectedSnippet = ""
        var targetTime: Long? = null

        // Pattern 1: "... in X days" or "... X days" or "... X days left"
        val daysRegex = Regex("""(?:\bin\s+)?(\d+)\s*(?:days?|d)\b""", RegexOption.IGNORE_CASE)
        val daysMatch = daysRegex.find(lower)
        if (daysMatch != null) {
            val count = daysMatch.groupValues[1].toIntOrNull() ?: 1
            cal.add(Calendar.DAY_OF_YEAR, count)
            // Default to end of that day (23:59)
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 0)
            targetTime = cal.timeInMillis
            detectedSnippet = daysMatch.value
        }

        // Pattern 2: "... in X hours" or "... X hours" or "... X hrs"
        if (targetTime == null) {
            val hoursRegex = Regex("""(?:\bin\s+)?(\d+)\s*(?:hours?|hrs?|h)\b""", RegexOption.IGNORE_CASE)
            val hoursMatch = hoursRegex.find(lower)
            if (hoursMatch != null) {
                val count = hoursMatch.groupValues[1].toIntOrNull() ?: 1
                cal.add(Calendar.HOUR_OF_DAY, count)
                targetTime = cal.timeInMillis
                detectedSnippet = hoursMatch.value
            }
        }

        // Pattern 3: "... in X minutes" or "... X mins"
        if (targetTime == null) {
            val minRegex = Regex("""(?:\bin\s+)?(\d+)\s*(?:minutes?|mins?|m)\b""", RegexOption.IGNORE_CASE)
            val minMatch = minRegex.find(lower)
            if (minMatch != null) {
                val count = minMatch.groupValues[1].toIntOrNull() ?: 30
                cal.add(Calendar.MINUTE, count)
                targetTime = cal.timeInMillis
                detectedSnippet = minMatch.value
            }
        }

        // Pattern 4: "tomorrow [at] 8pm" or "tomorrow"
        if (targetTime == null && lower.contains("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val timeMatch = Regex("""(?:at\s+)?(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""", RegexOption.IGNORE_CASE).find(lower)
            if (timeMatch != null && timeMatch.value != "tomorrow") {
                val hourRaw = timeMatch.groupValues[1].toIntOrNull() ?: 12
                val min = timeMatch.groupValues[2].toIntOrNull() ?: 0
                val ampm = timeMatch.groupValues[3].lowercase(Locale.ROOT)
                val hour = when (ampm) {
                    "pm" -> if (hourRaw < 12) hourRaw + 12 else hourRaw
                    "am" -> if (hourRaw == 12) 0 else hourRaw
                    else -> hourRaw
                }
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)
                detectedSnippet = "tomorrow " + timeMatch.value
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 20) // default 8 PM tomorrow
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                detectedSnippet = "tomorrow"
            }
            targetTime = cal.timeInMillis
        }

        // Pattern 5: "tonight"
        if (targetTime == null && lower.contains("tonight")) {
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 0)
            targetTime = cal.timeInMillis
            detectedSnippet = "tonight"
        }

        // Pattern 6: Day of week: "Friday", "due Friday", "next Monday"
        if (targetTime == null) {
            val daysOfWeek = mapOf(
                "sunday" to Calendar.SUNDAY,
                "monday" to Calendar.MONDAY,
                "tuesday" to Calendar.TUESDAY,
                "wednesday" to Calendar.WEDNESDAY,
                "thursday" to Calendar.THURSDAY,
                "friday" to Calendar.FRIDAY,
                "saturday" to Calendar.SATURDAY
            )
            for ((dayName, dayConstant) in daysOfWeek) {
                if (lower.contains(dayName)) {
                    val currentDay = cal.get(Calendar.DAY_OF_WEEK)
                    var diff = dayConstant - currentDay
                    if (diff <= 0) diff += 7
                    cal.add(Calendar.DAY_OF_YEAR, diff)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 0)
                    targetTime = cal.timeInMillis
                    detectedSnippet = dayName
                    break
                }
            }
        }

        // Pattern 7: Specific month date: "Dec 31", "September 10", "Jan 1"
        if (targetTime == null) {
            val months = mapOf(
                "jan" to Calendar.JANUARY, "feb" to Calendar.FEBRUARY, "mar" to Calendar.MARCH,
                "apr" to Calendar.APRIL, "may" to Calendar.MAY, "jun" to Calendar.JUNE,
                "jul" to Calendar.JULY, "aug" to Calendar.AUGUST, "sep" to Calendar.SEPTEMBER,
                "oct" to Calendar.OCTOBER, "nov" to Calendar.NOVEMBER, "dec" to Calendar.DECEMBER
            )
            val monthRegex = Regex("""\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\s+(\d{1,2})\b""", RegexOption.IGNORE_CASE)
            val match = monthRegex.find(lower)
            if (match != null) {
                val mStr = match.groupValues[1].lowercase(Locale.ROOT)
                val day = match.groupValues[2].toIntOrNull() ?: 1
                val monthConstant = months[mStr] ?: Calendar.JANUARY
                cal.set(Calendar.MONTH, monthConstant)
                cal.set(Calendar.DAY_OF_MONTH, day)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 0)
                if (cal.timeInMillis < now) {
                    cal.add(Calendar.YEAR, 1)
                }
                targetTime = cal.timeInMillis
                detectedSnippet = match.value
            }
        }

        // If no time detected, default to 3 days from now
        val resolvedTargetTime = targetTime ?: (now + 3 * 24 * 3600 * 1000L)
        if (detectedSnippet.isEmpty()) {
            detectedSnippet = "3 days (default)"
        }

        // Clean title: remove keywords like "due", "expires", "in", and detectedSnippet
        var cleanTitle = trimmed
        if (detectedSnippet.isNotEmpty() && targetTime != null) {
            cleanTitle = cleanTitle.replace(Regex(Regex.escape(detectedSnippet), RegexOption.IGNORE_CASE), "")
        }
        cleanTitle = cleanTitle
            .replace(Regex("""\b(due|expires|starts|departing|departure|renews|in|at)\b""", RegexOption.IGNORE_CASE), "")
            .trim()
            .replace(Regex("""\s+"""), " ")

        if (cleanTitle.isEmpty()) {
            cleanTitle = trimmed.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        } else {
            cleanTitle = cleanTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }

        // Detect Icon, Category, Color
        var icon = "⏳"
        var category = "General"
        var colorHex = "#6366F1"

        val searchIn = lower
        for ((keywords, meta) in ICON_KEYWORDS) {
            if (keywords.any { searchIn.contains(it) }) {
                icon = meta.first
                category = meta.second.first
                colorHex = meta.second.second
                break
            }
        }

        return ParsedCounterDraft(
            title = cleanTitle,
            targetTimeMillis = resolvedTargetTime,
            icon = icon,
            category = category,
            colorHex = colorHex,
            detectedTimeSnippet = detectedSnippet
        )
    }
}
