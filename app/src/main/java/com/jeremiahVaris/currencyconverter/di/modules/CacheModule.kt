package com.jeremiahVaris.currencyconverter.di.modules

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CacheModule {

    private val key: String = "shared_preferences"

    @Singleton
    @Provides
    fun providesSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            key, Context.MODE_PRIVATE
        )
    }

}