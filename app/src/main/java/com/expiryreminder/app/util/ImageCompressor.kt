package com.expiryreminder.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageCompressor {
    private const val TARGET_WIDTH = 1280
    private const val TARGET_HEIGHT = 960
    private const val WEBP_QUALITY = 75

    fun compressImage(context: Context, uri: Uri, outputDir: File? = null): String? {
        return try {
            val dir = outputDir ?: File(context.filesDir, "images").also { it.mkdirs() }
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) return null

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            val tempFile = File(context.cacheDir, "temp_image")
            inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input?.copyTo(output)
                }
            }

            BitmapFactory.decodeFile(tempFile.absolutePath, options)

            val actualWidth = options.outWidth
            val actualHeight = options.outHeight

            var inSampleSize = 1
            if (actualHeight > TARGET_HEIGHT || actualWidth > TARGET_WIDTH) {
                val halfHeight = actualHeight / 2
                val halfWidth = actualWidth / 2
                while ((halfHeight / inSampleSize) >= TARGET_HEIGHT &&
                    (halfWidth / inSampleSize) >= TARGET_WIDTH) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath, decodeOptions)
                ?: return null

            val scaledBitmap = if (bitmap.width > TARGET_WIDTH || bitmap.height > TARGET_HEIGHT) {
                val scale = minOf(
                    TARGET_WIDTH.toFloat() / bitmap.width,
                    TARGET_HEIGHT.toFloat() / bitmap.height
                )
                val newWidth = (bitmap.width * scale).toInt()
                val newHeight = (bitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            val outputFile = File(outputDir, "${System.currentTimeMillis()}.webp")
            FileOutputStream(outputFile).use { outputStream ->
                scaledBitmap.compress(
                    Bitmap.CompressFormat.WEBP,
                    WEBP_QUALITY,
                    outputStream
                )
            }

            bitmap.recycle()
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            tempFile.delete()

            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
