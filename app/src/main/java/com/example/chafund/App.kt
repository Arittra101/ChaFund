package com.example.chafund

import android.app.Application
import com.example.chafund.core.di.appModules
import com.example.chafund.core.utils.AppLogger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        AppLogger.init(BuildConfig.DEBUG)

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@App)
            modules(appModules())
        }

        // MonthManager lifecycle observer registered in CHF-20
    }
}
