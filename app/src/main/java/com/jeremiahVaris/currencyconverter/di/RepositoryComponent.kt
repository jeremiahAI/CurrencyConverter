package com.jeremiahVaris.currencyconverter.di

import com.jeremiahVaris.currencyconverter.di.modules.CacheModule
import com.jeremiahVaris.currencyconverter.di.modules.NetworkModule
import com.jeremiahVaris.currencyconverter.repository.CurrencyInfoRepository
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
//        AndroidInjectionModule::class,
//        ActivityModule::class,
        NetworkModule::class,
//        PresentationModule::class,
        CacheModule::class
    ]
)
interface RepositoryComponent {
    fun inject(repository: CurrencyInfoRepository)
}