package com.example.chafund.core.di

import com.example.chafund.feature.fund.di.fundModule
import org.koin.core.module.Module

fun appModules(): List<Module> = listOf(
    databaseModule,
    storageModule,
    sessionModule,
    utilsModule,
    navigationModule,
    fundModule,
    // historyModule,    // CHF-36
    // settingsModule,   // CHF-43
)
