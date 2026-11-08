package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.EventEntity
import com.example.domain.EventSorter
import com.example.domain.NaturalLanguageParser
import com.example.domain.ProgressCalculator
import com.example.domain.SortMode
import com.example.domain.UrgencyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Evenza", appName)
    }

    @Test
    fun `test natural language parsing`() {
        val draft1 = NaturalLanguageParser.parse("Food coupon 5 days")
        assertNotNull(draft1)
        assertEquals("Food coupon", draft1!!.title)
        assertEquals("🍔", draft1.icon)
        assertEquals("Food", draft1.category)

        val draft2 = NaturalLanguageParser.parse("Movie tomorrow 8pm")
        assertNotNull(draft2)
        assertEquals("Movie", draft2!!.title)
        assertEquals("🎬", draft2.icon)

        val draft3 = NaturalLanguageParser.parse("Gift card expires Dec 31")
        assertNotNull(draft3)
        assertEquals("Gift card", draft3!!.title)
        assertEquals("🎁", draft3.icon)
    }

    @Test
    fun `test progress calculation`() {
        val now = 1000000L
        val event = EventEntity(
            title = "Test Coupon",
            startTimeMillis = 0L,
            targetTimeMillis = 2000000L
        )
        val progress = ProgressCalculator.calculate(event, now)
        assertEquals(0.5f, progress.progress, 0.01f)
        assertEquals("50%", progress.percentText)
    }

    @Test
    fun `test event sorting by urgency`() {
        val now = 1000000L
        val eventNear = EventEntity(id = 1, title = "Near", startTimeMillis = 0, targetTimeMillis = now + 10000)
        val eventFar = EventEntity(id = 2, title = "Far", startTimeMillis = 0, targetTimeMillis = now + 50000)
        val eventExpired = EventEntity(id = 3, title = "Expired", startTimeMillis = 0, targetTimeMillis = now - 5000)

        val sorted = EventSorter.sort(listOf(eventFar, eventExpired, eventNear), SortMode.URGENCY, now)
        assertEquals(listOf(1L, 2L, 3L), sorted.map { it.id })
    }
}
