package com.zoomearth.wallpaper.extractor

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient

class ZoomEarthImageExtractor(private val context: Context) {

    fun setupWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // Agente de usuario de PC para evitar que la web cargue versiones móviles limitadas
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // CSS extremo: Oculta botones de mapas, cabeceras, líneas de tiempo y paneles.
                val cleanMapJs = """
                    (function() {
                        var style = document.createElement('style');
                        style.innerHTML = `
                            /* Ocultar todos los controles nativos del motor del mapa (Mapbox/Leaflet) */
                            .mapboxgl-control-container,
                            .leaflet-control-container { display: none !important; }
                            
                            /* Ocultar interfaz principal de Zoom Earth (Línea de tiempo, menús, barras) */
                            header, nav, aside, footer,
                            #header, #timeline, #panel, #search, #layers, #play, #zoom,
                            .panel, .menu, .button, .bar, .tool, .notifications, .play-controls,
                            
                            /* Ocultar cuadros promocionales y avisos de cookies/descargas */
                            .app-promo, [class*="promo"], [class*="modal"], [class*="banner"], 
                            .overlay, #app-download-dialog, .cookie-banner { 
                                display: none !important; 
                            }
                            
                            /* Forzar que el contenedor principal del mapa ocupe toda la pantalla sin márgenes */
                            body, html { margin: 0 !important; padding: 0 !important; overflow: hidden !important; }
                        `;
                        document.head.appendChild(style);

                        // Clic automático en botones de "Aceptar cookies" o "Continuar" si aparecen
                        var buttons = document.querySelectorAll('button');
                        buttons.forEach(function(btn) {
                            var text = btn.innerText.toLowerCase();
                            if (text.includes('continuar') || text.includes('aceptar') || text.includes('accept')) {
                                btn.click();
                            }
                        });
                    })();
                """.trimIndent()

                view?.evaluateJavascript(cleanMapJs, null)
            }
        }
    }
}
