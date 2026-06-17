package com.example.chafund

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.chafund.core.di.appModules
import com.example.chafund.core.utils.AppLogger
import com.example.chafund.core.utils.MonthManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.java.KoinJavaComponent.get

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        AppLogger.init(BuildConfig.DEBUG)

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@App)
            modules(appModules())
        }

        // Register MonthManager to run month detection on every ON_RESUME
        val monthManager: MonthManager = get(MonthManager::class.java)
        ProcessLifecycleOwner.get().lifecycle.addObserver(monthManager)
    }
}
