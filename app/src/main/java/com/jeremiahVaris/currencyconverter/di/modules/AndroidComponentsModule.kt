package com.jeremiahVaris.currencyconverter.di.modules

import com.jeremiahVaris.currencyconverter.MainActivity
import com.jeremiahVaris.currencyconverter.di.RatesVisualizationFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AndroidComponentsModule {
    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun contributeRatesVisualizationFragment(): RatesVisualizationFragment
}