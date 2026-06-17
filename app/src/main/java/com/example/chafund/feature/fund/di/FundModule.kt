package com.example.chafund.feature.fund.di

import com.example.chafund.feature.fund.data.repository.FundRepositoryImpl
import com.example.chafund.feature.fund.domain.FundRepository
import com.example.chafund.feature.fund.presentation.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val fundModule = module {
    singleOf(::FundRepositoryImpl) bind FundRepository::class
    viewModelOf(::HomeViewModel)
}
