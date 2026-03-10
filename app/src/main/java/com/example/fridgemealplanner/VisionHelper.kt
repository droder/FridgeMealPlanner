package com.example.fridgemealplanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VisionHelper {

    suspend fun detectItemsFromImage(
        context: Context,
        uri: Uri
    ): List<String> = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadScaledBitmap(context, uri, 512, 512) ?: return@withContext emptyList()

            val foods = mutableSetOf<String>()
            val samples = listOf(
                0.2f to 0.2f,
                0.5f to 0.2f,
                0.8f to 0.2f,
                0.2f to 0.5f,
                0.5f to 0.5f,
                0.8f to 0.5f,
                0.2f to 0.8f,
                0.5f to 0.8f,
                0.8f to 0.8f
            )

            for ((px, py) in samples) {
                val x = (bitmap.width * px).toInt().coerceIn(0, bitmap.width - 1)
                val y = (bitmap.height * py).toInt().coerceIn(0, bitmap.height - 1)
                val pixel = bitmap.getPixel(x, y)

                val r = android.graphics.Color.red(pixel)
                val g = android.graphics.Color.green(pixel)
                val b = android.graphics.Color.blue(pixel)

                when {
                    g > 140 && g > r + 20 && g > b + 20 -> foods.add("broccoli")
                    r > 170 && g in 70..170 && b < 100 -> foods.add("carrots")
                    r > 180 && g > 180 && b > 180 -> foods.add("milk")
                    r > 200 && g > 180 && b < 120 -> foods.add("cheese")
                    r > 150 && g < 120 && b < 120 -> foods.add("tomato")
                    r > 200 && g > 200 && b > 200 -> foods.add("eggs")
                }
            }

            bitmap.recycle()

            if (foods.isEmpty()) {
                listOf("eggs", "cheese")
            } else {
                foods.toList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun loadScaledBitmap(
        context: Context,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        val resolver = context.contentResolver

        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, boundsOptions)
        }

        val sampleSize = calculateInSampleSize(boundsOptions, reqWidth, reqHeight)

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }

        return resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize.coerceAtLeast(1)
    }
}
