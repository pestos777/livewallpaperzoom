package com.zoomearth.wallpaper.extractor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class ZoomEarthImageExtractor(private val context: Context) {

    // Ahora la función acepta los 3 parámetros que MainActivity le está enviando (latitud, longitud y zoom)
    suspend fun extractCleanBitmap(lat: Any, lon: Any, zoom: Any): Bitmap? = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val webView = WebView(context)
            
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
            }

            webView.layout(0, 0, 1080, 1920)

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    val cleanMapJs = """
                        (function() {
                            var style = document.createElement('style');
                            style.innerHTML = `
                                .mapboxgl-control-container, .leaflet-control-container { display: none !important; }
                                header, nav, aside, footer, #header, #timeline, #panel, #search, #layers, #play, #zoom,
                                .panel, .menu, .button, .bar, .tool, .notifications, .play-controls,
                                .app-promo, [class*="promo"], [class*="modal"], [class*="banner"], 
                                .overlay, #app-download-dialog, .cookie-banner { display: none !important; }
                                body, html { margin: 0 !important; padding: 0 !important; overflow: hidden !important; }
                            `;
                            document.head.appendChild(style);

                            var buttons = document.querySelectorAll('button');
                            buttons.forEach(function(btn) {
                                var text = btn.innerText.toLowerCase();
                                if (text.includes('continuar') || text.includes('aceptar') || text.includes('accept')) {
                                    btn.click();
                                }
                            });
                        })();
                    """.trimIndent()

                    view.evaluateJavascript(cleanMapJs) {
                        view.postDelayed({
                            try {
                                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bitmap)
                                view.draw(canvas)
                                if (continuation.isActive) continuation.resume(bitmap)
                            } catch (e: Exception) {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        }, 3000)
                    }
                }
            }
            
            // Inyectamos las coordenadas directamente en la URL de Zoom Earth
            webView.loadUrl("https://zoom.earth/maps/satellite/#view=$lat,$lon,${zoom}z")
        }
    }
}
