package com.example.chafund.core.di

import com.example.chafund.feature.fund.di.fundModule
import com.example.chafund.feature.history.di.historyModule
import com.example.chafund.feature.settings.di.settingsModule
import org.koin.core.module.Module

fun appModules(): List<Module> = listOf(
    databaseModule,
    storageModule,
    sessionModule,
    utilsModule,
    navigationModule,
    fundModule,
    historyModule,
    settingsModule,
)
