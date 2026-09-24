package com.zoomearth.wallpaper.extractor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ZoomEarthImageExtractor(private val context: Context) {

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun extractCleanBitmap(url: String, width: Int, height: Int): Bitmap? = suspendCancellableCoroutine { continuation ->
        val mainHandler = Handler(Looper.getMainLooper())
        
        mainHandler.post {
            val webView = WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                
                measure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                )
                layout(0, 0, measuredWidth, measuredHeight)
            }

            continuation.invokeOnCancellation {
                mainHandler.post { webView.destroy() }
            }

            val hideUiScript = """
                (function() {
                    var css = '.header, .timeline, .legend, .buttons, .sidebar, .controls, .nav, .ad, .search, .play-button { display: none !important; }';
                    var style = document.createElement('style');
                    style.type = 'text/css';
                    style.appendChild(document.createTextNode(css));
                    document.head.appendChild(style);
                })();
            """.trimIndent()

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    webView.evaluateJavascript(hideUiScript, null)

                    mainHandler.postDelayed({
                        try {
                            webView.measure(
                                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                            )
                            webView.layout(0, 0, webView.measuredWidth, webView.measuredHeight)

                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bitmap)
                            webView.draw(canvas)
                            
                            if (continuation.isActive) {
                                continuation.resume(bitmap)
                            }
                        } catch (e: Exception) {
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                        } finally {
                            webView.destroy()
                        }
                    }, 4000)
                }
            }

            webView.loadUrl(url)
        }
    }
}
