package com.example.chafund.core.di

import com.example.chafund.core.data.storage.DataStoreLocalStorage
import com.example.chafund.core.data.storage.LocalStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storageModule = module {
    single<LocalStorage> { DataStoreLocalStorage(androidContext()) }
}
