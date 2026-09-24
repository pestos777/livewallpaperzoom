package com.zoomearth.wallpaper.extractor

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient

class ZoomEarthImageExtractor(private val context: Context) {

    fun setupWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // 1. Simula un navegador de escritorio (PC) para evitar que Zoom Earth detecte Android
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // 2. Oculta mediante CSS los carteles de "Descargar app", modales y barras superiores
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

                        // Clic automático en "Continuar" si la web lo requiere
                        var buttons = document.getElementsByTagName('button');
                        for (var i = 0; i < buttons.length; i++) {
                            if (buttons[i].innerText.includes('Continuar')) {
                                buttons[i].click();
                            }
                        }
                    })();
                """.trimIndent()

                view?.evaluateJavascript(hideOverlayCss, null)
            }
        }
    }
}
