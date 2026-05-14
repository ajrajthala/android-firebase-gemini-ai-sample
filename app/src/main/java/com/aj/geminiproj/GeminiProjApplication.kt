package com.aj.geminiproj

import android.app.Application
import com.aj.geminiproj.core.ai.firebase.di.aiModule
import com.aj.geminiproj.core.common.di.commonModule
import com.aj.geminiproj.core.data.di.dataModule
import com.aj.geminiproj.features.chat.di.chatModule
import com.aj.geminiproj.navigation.di.navigationModule
import com.aj.geminiproj.tools.calendar.di.calendarToolModule
import com.aj.geminiproj.tools.contacts.di.contactsToolModule
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
            modules(
                //core
                aiModule,
                commonModule,
                dataModule,

                //tools
                contactsToolModule,
                calendarToolModule,


                // features
                chatModule,  navigationModule,)
        }
    }
}