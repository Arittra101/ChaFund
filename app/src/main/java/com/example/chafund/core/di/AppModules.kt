package com.example.chafund.core.di

import org.koin.core.module.Module

fun appModules(): List<Module> = listOf(
    databaseModule,
    storageModule,
    sessionModule,
    utilsModule,
    // fundModule,       // CHF-30
    // historyModule,    // CHF-36
    // settingsModule,   // CHF-43
)
