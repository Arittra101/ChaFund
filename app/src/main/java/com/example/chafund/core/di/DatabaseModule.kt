package com.example.chafund.core.di

import androidx.room.Room
import com.example.chafund.core.data.database.ChaFundDb
import com.example.chafund.core.data.database.seed.SeedCallback
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            ChaFundDb::class.java,
            "chafund.db",
        )
            .addCallback(SeedCallback())
            // .addMigrations(MIGRATION_1_2) // add before next schema change
            .build()
    }
    single { get<ChaFundDb>().monthDao() }
    single { get<ChaFundDb>().timeCategoryDao() }
    single { get<ChaFundDb>().entryDao() }
    single { get<ChaFundDb>().expenseDao() }
    single { get<ChaFundDb>().historyDao() }
}
