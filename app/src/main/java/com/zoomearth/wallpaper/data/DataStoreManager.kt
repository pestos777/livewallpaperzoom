package com.zoomearth.wallpaper.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "zoom_earth_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val ZOOM_EARTH_URL_KEY = stringPreferencesKey("zoom_earth_url")
        const val DEFAULT_URL = "https://zoom.earth/maps/satellite/#view=30.577,-18.135,6z/overlays=radar,labels:off"
    }

    val zoomEarthUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ZOOM_EARTH_URL_KEY] ?: DEFAULT_URL
    }

    suspend fun saveZoomEarthUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[ZOOM_EARTH_URL_KEY] = url
        }
    }
}
