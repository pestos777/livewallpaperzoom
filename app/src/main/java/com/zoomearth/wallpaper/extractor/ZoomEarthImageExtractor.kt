package com.zoomearth.wallpaper.extractor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
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
                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
            }

            webView.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
            )
            webView.layout(0, 0, 1080, 1920)

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    // 1. Limpiamos la interfaz de Zoom Earth
                    val cleanMapJs = """
                        var style = document.createElement('style');
                        style.innerHTML = 'header, nav, aside, footer, .panel, .menu, .button, .overlay { display: none !important; }';
                        document.head.appendChild(style);
                    """.trimIndent()
                    view.evaluateJavascript(cleanMapJs, null)

                    // 2. Estrategia Python: Esperamos 5 segundos y sacamos el Base64 directamente del canvas HTML
                    view.postDelayed({
                        val getBase64Js = """
                            (function() {
                                var canvas = document.querySelector('canvas');
                                if (canvas) {
                                    return canvas.toDataURL('image/png');
                                }
                                return 'null';
                            })();
                        """.trimIndent()

                        view.evaluateJavascript(getBase64Js) { base64 ->
                            try {
                                if (base64 != null && base64 != "null" && base64 != "\"null\"" && base64.contains(",")) {
                                    // Limpiamos las comillas extra que añade el WebView y el encabezado de Base64
                                    val cleanBase64 = base64.substringAfter(",").replace("\"", "")
                                    val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    
                                    if (continuation.isActive) continuation.resume(bitmap)
                                } else {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                            } catch (e: Exception) {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        }
                    }, 5000)
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
            
            webView.loadUrl("https://zoom.earth/maps/satellite/#view=$lat,$lon,${zoom}z")
        }
    }
}
