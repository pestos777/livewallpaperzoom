package com.zoomearth.wallpaper.service

import android.app.WallpaperManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zoomearth.wallpaper.data.DataStoreManager
import com.zoomearth.wallpaper.extractor.ZoomEarthImageExtractor
import com.zoomearth.wallpaper.util.BitmapUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class WallpaperUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val dataStore = DataStoreManager(context)
            val url = dataStore.zoomEarthUrl.first()

            val displayMetrics = context.resources.displayMetrics
            val width = displayMetrics.widthPixels
            val height = displayMetrics.heightPixels

            val extractor = ZoomEarthImageExtractor(context)
            
            val rawBitmap = withTimeoutOrNull(30_000L) {
                extractor.extractCleanBitmap(url, width, height)
            } ?: return Result.retry()

            val finalBitmap = BitmapUtils.drawTimestampOnBitmap(context, rawBitmap)

            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(
                finalBitmap,
                null,
                true,
                WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
