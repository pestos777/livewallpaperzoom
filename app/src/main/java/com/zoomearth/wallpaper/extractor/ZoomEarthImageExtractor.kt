package com.zoomearth.wallpaper.extractor

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient

class ZoomEarthImageExtractor(private val context: Context) {

    // URL por defecto de Zoom Earth
    val defaultUrl: String = "https://zoom.earth/"

    fun setupWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // Agente de usuario de escritorio para evitar avisos móviles
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

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
