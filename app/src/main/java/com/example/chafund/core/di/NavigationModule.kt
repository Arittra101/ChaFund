package com.example.chafund.core.di

import com.example.chafund.navigation.Navigator
import com.example.chafund.navigation.NavigatorImpl
import org.koin.dsl.module

val navigationModule = module {
    single<Navigator> { NavigatorImpl() }
}
