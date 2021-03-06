package com.jeremiahVaris.currencyconverter.di

import android.app.Application
import com.jeremiahVaris.currencyconverter.ConverterApplication
import com.jeremiahVaris.currencyconverter.di.modules.AndroidComponentsModule
import com.jeremiahVaris.currencyconverter.di.modules.CacheModule
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
        AndroidComponentsModule::class,
        NetworkModule::class,
        PresentationModule::class,
        CacheModule::class
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