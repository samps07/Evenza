package com.example

import com.example.ai.ExpiryCategoryDefaults
import com.example.data.EventEntity
import com.example.notifications.EventNotificationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpiryTrackerAndNotificationUnitTest {

    @Test
    fun testExpiryCategoryDefaults_Dairy() {
        val defaults = ExpiryCategoryDefaults.getDefaultsForCategory("Dairy")
        assertEquals("🥛", defaults.defaultIcon)
        assertEquals("#3B82F6", defaults.defaultColorHex)
        assertTrue(defaults.notify1DayBefore)
        assertTrue(defaults.notify6HoursBefore)
        assertFalse(defaults.notify1HourBefore)
        assertTrue(defaults.notifyAtExpiry)

        val milkDefaults = ExpiryCategoryDefaults.getDefaultsForCategory("milk")
        assertEquals("🥛", milkDefaults.defaultIcon)
    }

    @Test
    fun testExpiryCategoryDefaults_Medicine() {
        val defaults = ExpiryCategoryDefaults.getDefaultsForCategory("Medicine")
        assertEquals("💊", defaults.defaultIcon)
        assertEquals("#EC4899", defaults.defaultColorHex)
        assertTrue(defaults.notify1DayBefore)
        assertFalse(defaults.notify6HoursBefore)
        assertTrue(defaults.notify1HourBefore)
        assertTrue(defaults.notifyAtExpiry)
    }

    @Test
    fun testExpiryCategoryDefaults_Food() {
        val defaults = ExpiryCategoryDefaults.getDefaultsForCategory("Food")
        assertEquals("🍞", defaults.defaultIcon)
        assertEquals("#F97316", defaults.defaultColorHex)
        assertTrue(defaults.notify1DayBefore)
        assertTrue(defaults.notify6HoursBefore)
        assertTrue(defaults.notifyAtExpiry)
    }

    @Test
    fun testExpiryCategoryDefaults_Beverage() {
        val defaults = ExpiryCategoryDefaults.getDefaultsForCategory("Beverage")
        assertEquals("🧃", defaults.defaultIcon)
        assertEquals("#10B981", defaults.defaultColorHex)
        assertTrue(defaults.notify1DayBefore)
        assertTrue(defaults.notify6HoursBefore)
    }

    @Test
    fun testExpiryCategoryDefaults_Pantry() {
        val defaults = ExpiryCategoryDefaults.getDefaultsForCategory("Pantry")
        assertEquals("🥫", defaults.defaultIcon)
        assertEquals("#EAB308", defaults.defaultColorHex)
        assertTrue(defaults.notify1DayBefore)
        assertFalse(defaults.notify6HoursBefore)
    }

    @Test
    fun testExpiryCategoryDefaults_Cosmetics() {
        val defaults = ExpiryCategoryDefaults.getDefaultsForCategory("Cosmetics")
        assertEquals("💄", defaults.defaultIcon)
        assertEquals("#8B5CF6", defaults.defaultColorHex)
    }

    @Test
    fun testExpiryCategoryDefaults_GeneralFallback() {
        val defaults = ExpiryCategoryDefaults.getDefaultsForCategory("Electronics")
        assertEquals("⏳", defaults.defaultIcon)
        assertEquals("#6366F1", defaults.defaultColorHex)
    }

    @Test
    fun testEventNotificationManager_getCopyableText() {
        val eventBoth = EventEntity(
            title = "Milk",
            description = "Organic whole milk 1L",
            startTimeMillis = 0L,
            targetTimeMillis = 1000L,
            linkUrl = "https://example.com/item"
        )
        assertEquals("Organic whole milk 1L\nhttps://example.com/item", EventNotificationManager.getCopyableText(eventBoth))

        val eventDescOnly = EventEntity(
            title = "Aspirin",
            description = "Take 1 pill after dinner",
            startTimeMillis = 0L,
            targetTimeMillis = 1000L,
            linkUrl = ""
        )
        assertEquals("Take 1 pill after dinner", EventNotificationManager.getCopyableText(eventDescOnly))

        val eventLinkOnly = EventEntity(
            title = "Recipe",
            description = "",
            startTimeMillis = 0L,
            targetTimeMillis = 1000L,
            linkUrl = "https://recipe.com"
        )
        assertEquals("https://recipe.com", EventNotificationManager.getCopyableText(eventLinkOnly))

        val eventTitleOnly = EventEntity(
            title = "Gym Membership",
            description = "",
            startTimeMillis = 0L,
            targetTimeMillis = 1000L,
            linkUrl = ""
        )
        assertEquals("Gym Membership", EventNotificationManager.getCopyableText(eventTitleOnly))
    }

    @Test
    fun testExpiryDateCalculation() {
        val expiryDateStr = "2026-11-15"
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val parsedDate = sdf.parse(expiryDateStr)
        val cal = Calendar.getInstance().apply {
            time = parsedDate!!
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val targetMillis = cal.timeInMillis

        // Trigger time 1 day before:
        val oneDayBefore = targetMillis - (24 * 3600 * 1000L)
        val oneDayCal = Calendar.getInstance().apply { timeInMillis = oneDayBefore }
        assertEquals(14, oneDayCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, oneDayCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, oneDayCal.get(Calendar.MINUTE))
    }

    @Test
    fun testAiAnalysisState_Transitions() {
        val readingState = com.example.ai.AiAnalysisState.ReadingOcr("/path/to/img.jpg")
        assertEquals("/path/to/img.jpg", readingState.imagePath)

        val ocrState = com.example.ai.AiAnalysisState.OcrExtracted(
            imagePath = "/path/to/img.jpg",
            extractedText = "MILK 1L\nEXP 2026-12-01"
        )
        assertEquals(2, ocrState.lineCount)
        assertTrue(ocrState.charCount > 0)

        val errorToken = com.example.ai.AiAnalysisState.Error(
            message = "User has not enough tokens / insufficient credits"
        )
        assertTrue(errorToken.isTokenError)

        val errorGeneric = com.example.ai.AiAnalysisState.Error(
            message = "Connection timeout"
        )
        assertFalse(errorGeneric.isTokenError)
    }
}
