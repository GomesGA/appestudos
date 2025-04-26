package com.example.appestudos

import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.appestudos.ui.theme.AppEstudosTheme
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val apiKey = applicationInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""

        if (apiKey.isEmpty()) {
            throw RuntimeException("Google Maps API Key not found or empty!")
        }

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        setContent {
            AppEstudosTheme {
                AppNavigation()
            }
        }
    }
}