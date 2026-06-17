package com.example.chafund.feature.settings.di

import com.example.chafund.feature.settings.data.repository.SettingsRepositoryImpl
import com.example.chafund.feature.settings.domain.SettingsRepository
import com.example.chafund.feature.settings.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    viewModelOf(::SettingsViewModel)
}
