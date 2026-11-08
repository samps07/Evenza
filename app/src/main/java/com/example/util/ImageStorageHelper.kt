package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageStorageHelper {

    fun saveUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val stream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            val dir = File(context.filesDir, "event_images")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val fileName = "img_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
            val destFile = File(dir, fileName)
            FileOutputStream(destFile).use { output ->
                stream.use { input ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveImage(context: Context, uri: Uri): String? = saveUriToInternalStorage(context, uri)

    private fun decodeSampledBitmapFromFile(filePath: String, maxDimension: Int): Bitmap? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, boundsOptions)

            var sampleSize = 1
            while (boundsOptions.outWidth / (sampleSize * 2) >= maxDimension &&
                boundsOptions.outHeight / (sampleSize * 2) >= maxDimension
            ) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            BitmapFactory.decodeFile(filePath, decodeOptions)
        } catch (_: Exception) {
            null
        }
    }

    fun loadBitmap(path: String, maxDimension: Int = 240, context: Context? = null): Bitmap? {
        return try {
            if (path.isBlank()) return null

            // 1. Content URI or resource URI
            if (path.startsWith("content://") || path.startsWith("android.resource://")) {
                if (context != null) {
                    val uri = Uri.parse(path)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        return inputStream.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                }
            }

            // 2. HTTP/HTTPS Web URL with cache
            if (path.startsWith("http://") || path.startsWith("https://")) {
                if (context != null) {
                    val cacheDir = File(context.cacheDir, "widget_images")
                    if (!cacheDir.exists()) cacheDir.mkdirs()
                    val hashName = "web_${Math.abs(path.hashCode())}.jpg"
                    val cachedFile = File(cacheDir, hashName)
                    if (cachedFile.exists() && cachedFile.length() > 0) {
                        return decodeSampledBitmapFromFile(cachedFile.absolutePath, maxDimension)
                    }
                    try {
                        val url = java.net.URL(path)
                        val connection = url.openConnection() as java.net.HttpURLConnection
                        connection.connectTimeout = 3000
                        connection.readTimeout = 3000
                        connection.inputStream.use { input ->
                            FileOutputStream(cachedFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        if (cachedFile.exists() && cachedFile.length() > 0) {
                            return decodeSampledBitmapFromFile(cachedFile.absolutePath, maxDimension)
                        }
                    } catch (_: Exception) {}
                }
            }

            // 3. Local file path
            val file = File(path)
            if (file.exists()) {
                return decodeSampledBitmapFromFile(file.absolutePath, maxDimension)
            }

            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadRoundedBitmap(path: String, sizePx: Int = 140, cornerRadiusPx: Float = 20f, context: Context? = null): Bitmap? {
        return try {
            val original = loadBitmap(path, sizePx * 2, context) ?: return null

            // Center-crop to square
            val minEdge = minOf(original.width, original.height)
            val cropX = (original.width - minEdge) / 2
            val cropY = (original.height - minEdge) / 2
            val squareBitmap = Bitmap.createBitmap(original, cropX, cropY, minEdge, minEdge)
            val scaledSquare = Bitmap.createScaledBitmap(squareBitmap, sizePx, sizePx, true)

            // Draw with rounded corners
            val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rectF = RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat())

            canvas.drawRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(scaledSquare, 0f, 0f, paint)

            output
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
