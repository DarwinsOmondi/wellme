package com.example.wellme

import android.app.Application
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.codeskop.sdk.android.CodeskopAndroid
import com.codeskop.sdk.core.CodeskopConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WellMeApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        
        val codeskopKey = BuildConfig.CODESKOP_KEY
        if (codeskopKey.isNotBlank()) {
            try {
                CodeskopAndroid.init(
                    this,
                    CodeskopConfig(apiKey = codeskopKey)
                )
                Log.d("WellMeApplication", "CodeSkop SDK initialized successfully.")
            } catch (e: Exception) {
                Log.e("WellMeApplication", "Failed to initialize CodeSkop SDK", e)
            }
        } else {
            Log.w("WellMeApplication", "CodeSkop Key is missing in BuildConfig.")
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }
}
