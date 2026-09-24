package com.zoomearth.wallpaper.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BitmapUtils {

    fun drawTimestampOnBitmap(context: Context, originalBitmap: Bitmap): Bitmap {
        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val timestampText = "Última actualización: ${dateFormat.format(Date())}"

        val fontSize = (bitmap.width / 40f).coerceIn(24f, 48f)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFE500")
            textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textWidth = textPaint.measureText(timestampText)
        val fontMetrics = textPaint.fontMetrics

        val marginX = bitmap.width * 0.05f
        val marginY = bitmap.height * 0.08f
        val x = bitmap.width - textWidth - marginX
        val y = bitmap.height - marginY

        val padding = 16f
        val backgroundRect = RectF(
            x - padding,
            y + fontMetrics.top - padding,
            x + textWidth + padding,
            y + fontMetrics.bottom + padding
        )

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(140, 0, 0, 0)
        }

        canvas.drawRoundRect(backgroundRect, 12f, 12f, bgPaint)
        canvas.drawText(timestampText, x, y, textPaint)

        return bitmap
    }
}
