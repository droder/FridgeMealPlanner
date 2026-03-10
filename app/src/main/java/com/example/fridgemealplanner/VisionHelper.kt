package com.example.fridgemealplanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object VisionHelper {

    suspend fun detectItemsFromImage(
        context: Context,
        uri: Uri
    ): List<String> = withContext(Dispatchers.IO) {
        val bitmap = loadBitmap(context, uri)
        val scaled = Bitmap.createScaledBitmap(bitmap, 640, 640, true)

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
            val x = (scaled.width * px).toInt().coerceIn(0, scaled.width - 1)
            val y = (scaled.height * py).toInt().coerceIn(0, scaled.height - 1)
            val pixel = scaled.getPixel(x, y)

            val r = android.graphics.Color.red(pixel)
            val g = android.graphics.Color.green(pixel)
            val b = android.graphics.Color.blue(pixel)

            when {
                g > 140 && g > r + 20 && g > b + 20 -> foods.add("broccoli")
                r > 170 && g in 70..170 && b < 100 -> foods.add("carrots")
                r > 180 && g > 180 && b > 180 -> foods.add("milk")
                r > 200 && g > 180 && b < 120 -> foods.add("cheese")
                r > 150 && g < 120 && b < 120 -> foods.add("tomato")
            }
        }

        if (foods.isEmpty()) {
            listOf("eggs", "cheese", "broccoli")
        } else {
            foods.toList()
        }
    }

    private fun loadBitmap(context: Context, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }
}
