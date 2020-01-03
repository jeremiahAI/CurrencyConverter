package com.jeremiahVaris.currencyconverter.di.modules

import com.jeremiahVaris.currencyconverter.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity
}