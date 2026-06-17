package com.example.chafund.core.di

import com.example.chafund.AppViewModel
import com.example.chafund.feature.fund.di.fundModule
import com.example.chafund.feature.history.di.historyModule
import com.example.chafund.feature.settings.di.settingsModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val appViewModelModule = module {
    viewModelOf(::AppViewModel)
}

fun appModules(): List<Module> = listOf(
    databaseModule,
    storageModule,
    sessionModule,
    utilsModule,
    navigationModule,
    appViewModelModule,
    fundModule,
    historyModule,
    settingsModule,
)
