package com.zoomearth.wallpaper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zoomearth.wallpaper.service.WallpaperUpdateWorker

class UnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT || intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val updateWork = OneTimeWorkRequestBuilder<WallpaperUpdateWorker>().build()
            WorkManager.getInstance(context).enqueue(updateWork)
        }
    }
}
