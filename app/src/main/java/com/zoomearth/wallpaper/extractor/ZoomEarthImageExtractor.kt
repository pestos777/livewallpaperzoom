package com.zoomearth.wallpaper.extractor

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient

class ZoomEarthImageExtractor(private val context: Context) {

    fun setupWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // User-Agent de PC para forzar la vista de escritorio sin anuncios móviles
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Inyección de CSS para ocultar el cartel de "Descargar app", encabezados y avisos
                val hideOverlayCss = """
                    (function() {
                        var style = document.createElement('style');
                        style.innerHTML = `
                            .app-promo, 
                            [class*="promo"], 
                            [class*="modal"], 
                            [class*="banner"], 
                            .overlay,
                            #app-download-dialog,
                            header,
                            .site-header { display: none !important; }
                        `;
                        document.head.appendChild(style);
                    })();
                """.trimIndent()

                view?.evaluateJavascript(hideOverlayCss, null)
            }
        }
    }
}
