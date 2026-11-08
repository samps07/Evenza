package com.example

import com.example.ai.OcrConfig
import com.example.ai.OcrMode
import com.example.ai.OpenRouterExpiryService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test ocr config defaults and modes`() {
        val defaultConfig = OcrConfig()
        assertEquals(OcrMode.DEEP_SCAN, defaultConfig.mode)
        assertEquals(1800, defaultConfig.maxResolution)
        assertTrue(defaultConfig.contrastBoost)
        assertTrue(defaultConfig.sharpening)

        // Verify all OcrModes have titles and estimated times
        OcrMode.values().forEach { mode ->
            assertNotNull(mode.title)
            assertNotNull(mode.subtitle)
            assertNotNull(mode.estimatedTime)
            assertTrue(mode.estimatedTime.startsWith("~"))
        }
    }

    @Test
    fun `test default system prompt contains event card fields`() {
        val prompt = OpenRouterExpiryService.DEFAULT_SYSTEM_PROMPT
        // Ensure prompt contains required JSON fields for EventEntity
        assertTrue(prompt.contains("productName"))
        assertTrue(prompt.contains("expiryDate"))
        assertTrue(prompt.contains("category"))
        assertTrue(prompt.contains("manufacturingDate"))
        assertTrue(prompt.contains("quantity"))
        assertTrue(prompt.contains("notes"))
        assertTrue(prompt.contains("confidence"))
        assertTrue(prompt.contains("needsVerification"))
        assertTrue(prompt.contains("{{TODAY}}"))
    }

    @Test
    fun `test custom prompt replacement with today reference`() {
        val customPrompt = "Extract data. Today is {{TODAY}}."
        val resolved = customPrompt.replace("{{TODAY}}", "2026-10-07")
        assertEquals("Extract data. Today is 2026-10-07.", resolved)
    }
}
