package com.aj.geminiproj

import android.app.Application
import com.aj.geminiproj.core.ai.di.aiModule
import com.aj.geminiproj.core.data.di.dataModule
import com.aj.geminiproj.features.chat.di.chatModule
import com.aj.geminiproj.navigation.di.navigationModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class GeminiProjApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase auto-initializes from google-services.json
        // No API key needed in code
        FirebaseApp.initializeApp(this)
        startKoin{
            androidContext(this@GeminiProjApplication)
            modules(aiModule, chatModule, dataModule, navigationModule)
        }
    }
}