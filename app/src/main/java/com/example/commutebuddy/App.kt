package com.example.commutebuddy

import android.app.Application
import android.preference.PreferenceManager
import com.google.firebase.FirebaseApp
import org.osmdroid.config.Configuration

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (_: Throwable) {
            // Ignore if google-services.json not present yet
        }
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, android.preference.PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = "com.example.commutebuddy"
    }
}


