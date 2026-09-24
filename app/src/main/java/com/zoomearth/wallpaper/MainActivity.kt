package com.zoomearth.wallpaper

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zoomearth.wallpaper.data.DataStoreManager
import com.zoomearth.wallpaper.extractor.ZoomEarthImageExtractor
import com.zoomearth.wallpaper.util.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { DataStoreManager(context) }

    val savedUrl by dataStore.zoomEarthUrl.collectAsState(initial = DataStoreManager.DEFAULT_URL)
    var inputUrl by remember(savedUrl) { mutableStateOf(savedUrl) }

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Zoom Earth Wallpaper") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                label = { Text("URL de Zoom Earth") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            dataStore.saveZoomEarthUrl(inputUrl)
                            Toast.makeText(context, "URL Guardada", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Guardar URL")
                }

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            dataStore.saveZoomEarthUrl(inputUrl)
                            val extractor = ZoomEarthImageExtractor(context)
                            val displayMetrics = context.resources.displayMetrics
                            
                            val raw = withTimeoutOrNull(20_000L) {
                                extractor.extractCleanBitmap(
                                    inputUrl,
                                    displayMetrics.widthPixels,
                                    displayMetrics.heightPixels
                                )
                            }
                            
                            if (raw != null) {
                                previewBitmap = BitmapUtils.drawTimestampOnBitmap(context, raw)
                            } else {
                                Toast.makeText(context, "Error o tiempo agotado", Toast.LENGTH_SHORT).show()
                            }
                            isLoading = false
                        }
                    }
                ) {
                    Text("Previsualizar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else previewBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                    )
                } ?: Text(
                    text = "Presiona 'Previsualizar' para cargar.",
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                enabled = previewBitmap != null,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    previewBitmap?.let { bmp ->
                        scope.launch(Dispatchers.IO) {
                            val wpManager = WallpaperManager.getInstance(context)
                            wpManager.setBitmap(
                                bmp,
                                null,
                                true,
                                WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                            )
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "¡Fondo actualizado!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            ) {
                Text("Establecer como Fondo")
            }
        }
    }
}
