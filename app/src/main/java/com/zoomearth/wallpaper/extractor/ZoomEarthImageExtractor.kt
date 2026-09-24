package com.zoomearth.wallpaper.extractor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class ZoomEarthImageExtractor(private val context: Context) {

    suspend fun extractCleanBitmap(lat: Any, lon: Any, zoom: Any): Bitmap? = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val webView = WebView(context)
            
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }

            // Forzamos un tamaño fijo para evitar el pantallazo negro
            webView.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
            )
            webView.layout(0, 0, 1080, 1920)

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    // 1. Inyectamos CSS puro para ocultar menús, botones y barras
                    val hideUiJs = """
                        var style = document.createElement('style');
                        style.innerHTML = `
                            header, nav, aside, footer, #header, #timeline, #panel, #search, 
                            .panel, .menu, .button, .bar, .tool, .notifications, .play-controls,
                            .mapboxgl-control-container, .leaflet-control-container,
                            .app-promo, .overlay { display: none !important; }
                        `;
                        document.head.appendChild(style);
                    """.trimIndent()
                    view.evaluateJavascript(hideUiJs, null)

                    // 2. Esperamos 3 segundos y sacamos la captura con el método que ya te funcionó
                    view.postDelayed({
                        try {
                            val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bitmap)
                            view.draw(canvas)
                            
                            if (continuation.isActive) continuation.resume(bitmap)
                        } catch (e: Exception) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    }, 3000)
                }
            }
            
            // Cargamos la URL usando los mismos parámetros que tienes en tu imagen
            webView.loadUrl("https://zoom.earth/maps/satellite/#view=$lat,$lon,${zoom}z/overlays=radar,labels:off")
        }
    }
}
