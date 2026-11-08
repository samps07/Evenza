package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import com.example.util.ImageStorageHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object OcrHelper {

    suspend fun extractTextFromImage(context: Context, imageUri: Uri, config: OcrConfig = OcrConfig()): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(visionText.text)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    suspend fun extractTextFromPath(path: String, config: OcrConfig = OcrConfig()): String = withContext(Dispatchers.Default) {
        val baseBitmap: Bitmap = ImageStorageHelper.loadBitmap(path, maxDimension = config.maxResolution)
            ?: throw IllegalArgumentException("Unable to decode image at path: $path")

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        when (config.mode) {
            OcrMode.FAST -> {
                // Single-pass direct scan on base image
                runMlKit(recognizer, baseBitmap)
            }

            OcrMode.ENHANCED -> {
                // Enhanced single pass with high contrast & sharpening
                val enhancedBitmap = enhanceImage(
                    source = baseBitmap,
                    contrast = if (config.contrastBoost) 1.55f else 1.0f,
                    brightness = -15f,
                    sharpen = config.sharpening
                )
                runMlKit(recognizer, enhancedBitmap)
            }

            OcrMode.DEEP_SCAN -> {
                // Dual-pass: base image + enhanced image, merging detected lines
                val pass1 = runMlKit(recognizer, baseBitmap)

                val enhancedBitmap = enhanceImage(
                    source = baseBitmap,
                    contrast = if (config.contrastBoost) 1.65f else 1.2f,
                    brightness = -20f,
                    sharpen = config.sharpening
                )
                val pass2 = runMlKit(recognizer, enhancedBitmap)

                mergeTextPasses(listOf(pass1, pass2))
            }

            OcrMode.MAX_PRECISION -> {
                // Multi-pass: base + enhanced + 90° & 270° rotations
                val passes = mutableListOf<String>()

                passes.add(runMlKit(recognizer, baseBitmap))

                val enhancedBitmap = enhanceImage(
                    source = baseBitmap,
                    contrast = if (config.contrastBoost) 1.7f else 1.2f,
                    brightness = -20f,
                    sharpen = config.sharpening
                )
                passes.add(runMlKit(recognizer, enhancedBitmap))

                // Check 90° and 270° orientation
                val rot90 = rotateBitmap(baseBitmap, 90f)
                passes.add(runMlKit(recognizer, rot90))

                val rot270 = rotateBitmap(baseBitmap, 270f)
                passes.add(runMlKit(recognizer, rot270))

                mergeTextPasses(passes)
            }
        }
    }

    private suspend fun runMlKit(
        recognizer: com.google.mlkit.vision.text.TextRecognizer,
        bitmap: Bitmap
    ): String = suspendCancellableCoroutine { continuation ->
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    /**
     * Applies high-contrast luminance and high-frequency edge filtering to clarify
     * faint dot-matrix expiry stamps and low-contrast labels.
     */
    private fun enhanceImage(
        source: Bitmap,
        contrast: Float,
        brightness: Float,
        sharpen: Boolean
    ): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // ColorMatrix for contrast boost and slight darkening of background noise
        val cm = ColorMatrix().apply {
            // First convert partially toward high contrast grayscale/luminance
            set(floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }

        canvas.drawBitmap(source, 0f, 0f, paint)

        // If sharpening is enabled, run a fast edge-accentuation convolution pass
        if (sharpen && width > 200 && height > 200) {
            try {
                return applyUnsharpFilter(output)
            } catch (_: Exception) {
                // Fallback to contrast-enhanced bitmap
            }
        }

        return output
    }

    /**
     * Lightweight 3x3 unsharp mask sharpening filter over bitmap pixels.
     * Clarifies dot-matrix stamps by strengthening high-contrast edges.
     */
    private fun applyUnsharpFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val outputPixels = IntArray(width * height)
        System.arraycopy(pixels, 0, outputPixels, 0, pixels.size)

        // Sharpen kernel:
        //  0 -1  0
        // -1  5 -1
        //  0 -1  0
        for (y in 1 until height - 1) {
            val rowOffset = y * width
            val topRowOffset = (y - 1) * width
            val bottomRowOffset = (y + 1) * width

            for (x in 1 until width - 1) {
                val center = pixels[rowOffset + x]
                val top = pixels[topRowOffset + x]
                val bottom = pixels[bottomRowOffset + x]
                val left = pixels[rowOffset + x - 1]
                val right = pixels[rowOffset + x + 1]

                val cr = (center shr 16) and 0xFF
                val cg = (center shr 8) and 0xFF
                val cb = center and 0xFF

                val tr = (top shr 16) and 0xFF
                val tg = (top shr 8) and 0xFF
                val tb = top and 0xFF

                val br = (bottom shr 16) and 0xFF
                val bg = (bottom shr 8) and 0xFF
                val bb = bottom and 0xFF

                val lr = (left shr 16) and 0xFF
                val lg = (left shr 8) and 0xFF
                val lb = left and 0xFF

                val rr = (right shr 16) and 0xFF
                val rg = (right shr 8) and 0xFF
                val rb = right and 0xFF

                var nr = (5 * cr - tr - br - lr - rr)
                var ng = (5 * cg - tg - bg - lg - rg)
                var nb = (5 * cb - tb - bb - lb - rb)

                if (nr < 0) nr = 0 else if (nr > 255) nr = 255
                if (ng < 0) ng = 0 else if (ng > 255) ng = 255
                if (nb < 0) nb = 0 else if (nb > 255) nb = 255

                outputPixels[rowOffset + x] = (0xFF shl 24) or (nr shl 16) or (ng shl 8) or nb
            }
        }

        val sharpened = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        sharpened.setPixels(outputPixels, 0, width, 0, 0, width, height)
        return sharpened
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * Intelligently merges text lines extracted from multiple passes,
     * removing duplicates while prioritizing lines containing numbers, dates, and expiry keywords.
     */
    private fun mergeTextPasses(passes: List<String>): String {
        val seenNormalized = mutableSetOf<String>()
        val resultLines = mutableListOf<String>()

        // Date & expiry priority keywords
        val priorityKeywords = listOf("EXP", "BEST", "USE", "BEFORE", "MFG", "LOT", "BATCH", "B.NO", "DATE")

        for (pass in passes) {
            val lines = pass.lines()
            for (rawLine in lines) {
                val trimmed = rawLine.trim()
                if (trimmed.length < 2) continue

                // Normalize for duplicate comparison: lowercase and strip punctuation
                val normalized = trimmed.lowercase().replace(Regex("[^a-z0-9]"), "")
                if (normalized.isBlank()) continue

                if (!seenNormalized.contains(normalized)) {
                    seenNormalized.add(normalized)
                    resultLines.add(trimmed)
                } else {
                    // If seen but current version has clear digits or punctuation, keep the more informative one
                    val hasKeywords = priorityKeywords.any { trimmed.contains(it, ignoreCase = true) }
                    val hasDigits = trimmed.any { it.isDigit() }
                    if ((hasKeywords || hasDigits) && !resultLines.contains(trimmed)) {
                        resultLines.add(trimmed)
                    }
                }
            }
        }

        return resultLines.joinToString("\n")
    }
}
