package com.hereliesaz.magnom

import android.app.Application
import com.hereliesaz.magnom.di.androidModule
import com.hereliesaz.magnom.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MagNomApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MagNomApp)
            modules(appModule, androidModule)
        }
    }
}
