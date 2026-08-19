package com.bakjoul.testwithings

import android.app.Application
import com.bakjoul.testwithings.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(appModules)
        }
    }
}
