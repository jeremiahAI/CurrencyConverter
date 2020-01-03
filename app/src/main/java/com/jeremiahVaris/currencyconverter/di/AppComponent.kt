package com.jeremiahVaris.currencyconverter.di

import android.app.Application
import com.jeremiahVaris.currencyconverter.ConverterApplication
import com.jeremiahVaris.currencyconverter.di.modules.ActivityModule
import com.jeremiahVaris.currencyconverter.di.modules.NetworkModule
import com.jeremiahVaris.currencyconverter.di.modules.PresentationModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        ActivityModule::class,
        NetworkModule::class,
        PresentationModule::class
    ]
)
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(converterApp: ConverterApplication)
}