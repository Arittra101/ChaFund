package com.example.chafund.feature.history.di

import com.example.chafund.feature.history.data.repository.HistoryRepositoryImpl
import com.example.chafund.feature.history.domain.HistoryRepository
import com.example.chafund.feature.history.presentation.daily.DailyHistoryViewModel
import com.example.chafund.feature.history.presentation.daydetail.DayDetailViewModel
import com.example.chafund.feature.history.presentation.monthly.MonthlyHistoryViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val historyModule = module {
    singleOf(::HistoryRepositoryImpl) bind HistoryRepository::class
    viewModelOf(::DailyHistoryViewModel)
    viewModelOf(::DayDetailViewModel)
    viewModelOf(::MonthlyHistoryViewModel)
}
