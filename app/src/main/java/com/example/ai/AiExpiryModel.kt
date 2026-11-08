package com.example.ai

data class AiExpiryResult(
    val productName: String = "",
    val expiryDate: String = "", // Format: YYYY-MM-DD
    val expiryEpochMillis: Long? = null,
    val category: String = "General",
    val manufacturingDate: String = "",
    val quantity: String = "",
    val notes: String = "",
    val confidence: String = "medium", // "high", "medium", "low"
    val needsVerification: Boolean = false,
    val verificationReason: String = "",
    val rawOcrText: String = "",
    val imagePath: String = "",
    val rawAiResponse: String = ""
)

sealed class AiAnalysisState {
    object Idle : AiAnalysisState()
    
    // Step 1: Processing OCR on-device
    data class ReadingOcr(
        val imagePath: String,
        val ocrModeTitle: String = ""
    ) : AiAnalysisState()
    
    // Shown when user cancels automatic AI analysis or when API key is missing
    data class OcrExtracted(
        val imagePath: String,
        val extractedText: String,
        val lineCount: Int = extractedText.lines().filter { it.isNotBlank() }.size,
        val charCount: Int = extractedText.length,
        val isCancelled: Boolean = false,
        val warningMessage: String = ""
    ) : AiAnalysisState()
    
    // Step 2 (Automated): Sending extracted text to LLM, with live extracted text preview & Cancel button
    data class CallingAi(
        val imagePath: String,
        val ocrText: String,
        val model: String = "",
        val ocrModeTitle: String = "",
        val lineCount: Int = ocrText.lines().filter { it.isNotBlank() }.size,
        val charCount: Int = ocrText.length
    ) : AiAnalysisState()
    
    // Step 3: LLM response received, showing parsed fields & raw response for debugging
    data class ReviewResult(
        val imagePath: String,
        val ocrText: String,
        val result: AiExpiryResult,
        val rawAiResponse: String = ""
    ) : AiAnalysisState()
    
    // Success terminal state (when user confirms result and transitions to event form)
    data class Success(val result: AiExpiryResult) : AiAnalysisState()
    
    // Error state with image, OCR text, and actionable guidance
    data class Error(
        val message: String,
        val canRetry: Boolean = true,
        val rawOcr: String = "",
        val imagePath: String = "",
        val isTokenError: Boolean = message.contains("token", ignoreCase = true) || 
                message.contains("credit", ignoreCase = true) || 
                message.contains("balance", ignoreCase = true) ||
                message.contains("quota", ignoreCase = true)
    ) : AiAnalysisState()
}
