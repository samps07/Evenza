package com.example.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

interface AiExpiryService {
    suspend fun analyzeOcrText(
        ocrText: String,
        apiKey: String,
        baseUrl: String = OpenRouterExpiryService.DEFAULT_BASE_URL,
        model: String = OpenRouterExpiryService.DEFAULT_MODEL,
        customPrompt: String? = null,
        availableCategories: List<com.example.data.CategoryItem>? = null
    ): Pair<AiExpiryResult, String>

    suspend fun testConnection(
        apiKey: String,
        baseUrl: String = OpenRouterExpiryService.DEFAULT_BASE_URL,
        model: String = OpenRouterExpiryService.DEFAULT_MODEL
    ): String
}

class OpenRouterExpiryService : AiExpiryService {

    companion object {
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1/chat/completions"
        const val DEFAULT_MODEL = "~openai/gpt-sol-latest"
        const val MODEL_GPT_4O_MINI = "openai/gpt-4o-mini"
        const val MODEL_FREE_LLAMA = "meta-llama/llama-3.3-70b-instruct:free"
        const val MODEL_GEMINI = "google/gemini-2.0-flash-001"
        private const val TAG = "OpenRouterService"

        val DEFAULT_SYSTEM_PROMPT = """
You are an expert product expiry date and label analyzer.
You will be given raw OCR text extracted from a product label, box, carton, blister pack, or receipt.
Extract the product information and return ONLY a valid, single JSON object with no markdown fences, no backticks, and no extra commentary.

Today's reference date is {{TODAY}}.

JSON format:
{
  "productName": "Exact Brand and Product Name (or empty string if not found)",
  "expiryDate": "YYYY-MM-DD (e.g. 2026-11-15, or empty string if uncertain)",
  "category": "Food" | "Medicine" | "Cosmetics" | "Dairy" | "Pantry" | "Beverage" | "Personal Care" | "General",
  "manufacturingDate": "YYYY-MM-DD (or empty string)",
  "quantity": "Net weight/volume/count (e.g. 500ml, 12 capsules, or empty string)",
  "notes": "Short helpful note (e.g. Keep refrigerated, Batch #, or empty string)",
  "confidence": "high" | "medium" | "low",
  "needsVerification": true | false,
  "verificationReason": "Brief explanation if date or title was hard to read"
}

Strict rules:
1. If a date is ambiguous, missing, or cannot be verified with confidence, leave expiryDate as "" and set needsVerification to true. Never fabricate or hallucinate dates.
2. For 2-digit years (e.g. 15/12/26 or 11/27), accurately resolve them (e.g. 2026, 2027).
3. Standardize month names (e.g. "EXP 05 NOV 26" -> "2026-11-05", "BEST BEFORE 12/2026" -> "2026-12-31").
4. If multiple dates appear (e.g. MFG vs EXP), identify which one is Expiry/Best Before/Use By.
5. Return strictly the raw JSON string without ```json or markdown.
        """.trimIndent()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun testConnection(
        apiKey: String,
        baseUrl: String,
        model: String
    ): String = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank()) {
            throw IllegalStateException("API key is blank. Please enter your OpenRouter or OpenAI API key.")
        }
        val effectiveUrl = if (baseUrl.isNotBlank()) baseUrl.trim() else DEFAULT_BASE_URL
        val effectiveModel = if (model.isNotBlank()) model.trim() else DEFAULT_MODEL

        val jsonBody = JSONObject().apply {
            put("model", effectiveModel)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "Respond with 'OK' only.")
                })
            })
            put("max_tokens", 10)
        }

        val request = Request.Builder()
            .url(effectiveUrl)
            .addHeader("Authorization", "Bearer $trimmedKey")
            .addHeader("HTTP-Referer", "https://evenza.app")
            .addHeader("X-OpenRouter-Title", "Evenza Expiry Tracker")
            .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val startTime = System.currentTimeMillis()
        val response = try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            throw IllegalStateException("Network failed to reach $effectiveUrl: ${e.localizedMessage ?: e.message}")
        }
        val latency = System.currentTimeMillis() - startTime
        val body = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errObj = JSONObject(body)
                errObj.optJSONObject("error")?.optString("message")
                    ?: errObj.optString("message", "HTTP ${response.code}: $body")
            } catch (_: Exception) {
                "HTTP ${response.code}: $body"
            }
            throw IllegalStateException("Endpoint returned error ($effectiveModel): $errorMsg")
        }

        "Connected successfully to $effectiveModel! (Latency: ${latency}ms)"
    }

    override suspend fun analyzeOcrText(
        ocrText: String,
        apiKey: String,
        baseUrl: String,
        model: String,
        customPrompt: String?,
        availableCategories: List<com.example.data.CategoryItem>?
    ): Pair<AiExpiryResult, String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank()) {
            throw IllegalStateException("API key is missing. Please configure your OpenRouter or OpenAI API key in Settings.")
        }

        if (ocrText.isBlank()) {
            throw IllegalArgumentException("No text was detected from the image. Please try another angle or enter manually.")
        }

        val effectiveUrl = if (baseUrl.isNotBlank()) baseUrl.trim() else DEFAULT_BASE_URL
        val effectiveModel = if (model.isNotBlank()) model.trim() else DEFAULT_MODEL

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val basePromptTemplate = if (!customPrompt.isNullOrBlank()) {
            customPrompt.trim()
        } else {
            DEFAULT_SYSTEM_PROMPT
        }

        var systemPrompt = if (basePromptTemplate.contains("{{TODAY}}")) {
            basePromptTemplate.replace("{{TODAY}}", todayStr)
        } else {
            "$basePromptTemplate\n\nReference: Today's date is $todayStr."
        }

        if (availableCategories != null && availableCategories.isNotEmpty()) {
            val catsInfo = availableCategories.joinToString(", ") { "${it.name} (alerts ${it.notifyDaysBefore} days before)" }
            systemPrompt += "\n\nAvailable app categories and their due-in alert settings: [$catsInfo]. Please categorize this product accurately strictly into one of these user categories."
        }

        val jsonBody = JSONObject().apply {
            put("model", effectiveModel)
            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "OCR Extracted Text:\n\"\"\"\n$ocrText\n\"\"\"")
                })
            }
            put("messages", messagesArray)
            put("temperature", 0.1)
            // Limit completion tokens to ~500 to prevent OpenRouter credit-reservation failures on low-balance accounts
            put("max_tokens", 500)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val requestBuilder = Request.Builder()
            .url(effectiveUrl)
            .addHeader("Authorization", "Bearer $trimmedKey")
            .addHeader("HTTP-Referer", "https://evenza.app")
            .addHeader("X-OpenRouter-Title", "Evenza Expiry Tracker")
            .post(requestBody)

        val response = try {
            client.newCall(requestBuilder.build()).execute()
        } catch (e: Exception) {
            throw IllegalStateException("Network connection failed to $effectiveUrl: ${e.localizedMessage ?: e.message}")
        }

        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            Log.e(TAG, "AI API error ${response.code}: $responseBody")
            val errorMsg = try {
                val errObj = JSONObject(responseBody)
                errObj.optJSONObject("error")?.optString("message")
                    ?: errObj.optString("message", "API request failed with code ${response.code}")
            } catch (_: Exception) {
                "API request failed with code ${response.code}: $responseBody"
            }
            throw IllegalStateException(errorMsg)
        }

        val parsedResult = parseAiResponse(responseBody, ocrText)
        Pair(parsedResult, responseBody)
    }

    private fun parseAiResponse(responseJson: String, rawOcr: String): AiExpiryResult {
        return try {
            val root = JSONObject(responseJson)
            val choices = root.getJSONArray("choices")
            if (choices.length() == 0) {
                throw IllegalStateException("AI returned an empty response choices array.")
            }
            val messageObj = choices.getJSONObject(0).getJSONObject("message")
            var content = messageObj.getString("content").trim()

            // Strip any Markdown code blocks if present
            if (content.startsWith("```json")) {
                content = content.removePrefix("```json").trim()
            } else if (content.startsWith("```")) {
                content = content.removePrefix("```").trim()
            }
            if (content.endsWith("```")) {
                content = content.removeSuffix("```").trim()
            }

            // If there is extra text around the JSON object, extract between first { and last }
            val firstBrace = content.indexOf('{')
            val lastBrace = content.lastIndexOf('}')
            val cleanJsonString = if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                content.substring(firstBrace, lastBrace + 1)
            } else {
                content
            }

            val parsedJson = JSONObject(cleanJsonString)

            val productName = parsedJson.optString("productName", "").trim()
            val expiryDate = parsedJson.optString("expiryDate", "").trim()
            val category = parsedJson.optString("category", "General").trim().ifEmpty { "General" }
            val mfgDate = parsedJson.optString("manufacturingDate", "").trim()
            val quantity = parsedJson.optString("quantity", "").trim()
            val notes = parsedJson.optString("notes", "").trim()
            val confidence = parsedJson.optString("confidence", "medium").trim().lowercase()
            var needsVerification = parsedJson.optBoolean("needsVerification", false)
            val verificationReason = parsedJson.optString("verificationReason", "").trim()

            // Parse Epoch millis for expiry date
            var epochMillis: Long? = null
            if (expiryDate.isNotBlank()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val date = sdf.parse(expiryDate)
                    if (date != null) {
                        val cal = Calendar.getInstance().apply {
                            time = date
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        epochMillis = cal.timeInMillis
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not parse date '$expiryDate'", e)
                    needsVerification = true
                }
            } else {
                needsVerification = true
            }

            if (productName.isBlank()) {
                needsVerification = true
            }

            AiExpiryResult(
                productName = productName,
                expiryDate = expiryDate,
                expiryEpochMillis = epochMillis,
                category = category,
                manufacturingDate = mfgDate,
                quantity = quantity,
                notes = notes,
                confidence = confidence,
                needsVerification = needsVerification,
                verificationReason = verificationReason,
                rawOcrText = rawOcr,
                rawAiResponse = content
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse structured JSON: ${e.message}", e)
            throw IllegalStateException("Failed to parse AI response: ${e.localizedMessage}")
        }
    }
}
