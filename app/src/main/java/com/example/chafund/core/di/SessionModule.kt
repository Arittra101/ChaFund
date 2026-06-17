package com.example.chafund.core.di

import com.example.chafund.core.data.session.Session
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sessionModule = module {
    singleOf(::Session)
}
