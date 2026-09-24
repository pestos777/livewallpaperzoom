package com.zoomearth.wallpaper
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class DirectMapDownloader {

    // Ejecutamos en IO para no bloquear la interfaz y evitar excepciones de red
    suspend fun downloadMapImage(staticApiUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(staticApiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 10000 // 10 segundos
                readTimeout = 10000
                doInput = true
                connect()
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                // Decodificamos el flujo de bytes directamente a un Bitmap de Android
                BitmapFactory.decodeStream(connection.inputStream)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
