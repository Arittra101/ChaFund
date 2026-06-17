package com.example.chafund.core.di

import com.example.chafund.core.domain.DefaultDispatcherProvider
import com.example.chafund.core.domain.DispatcherProvider
import com.example.chafund.core.utils.MonthManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val utilsModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    singleOf(::MonthManager)
}
