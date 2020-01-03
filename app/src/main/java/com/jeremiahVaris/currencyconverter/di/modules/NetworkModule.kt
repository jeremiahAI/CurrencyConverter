package com.jeremiahVaris.currencyconverter.di.modules

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.jeremiahVaris.currencyconverter.BuildConfig
import com.jeremiahVaris.currencyconverter.rest.core.NetworkConnectionInterceptor
import com.jeremiahVaris.currencyconverter.rest.fixerIo.api.ApiFixer
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val BASE_URL = "http://data.fixer.io/"

@Module
class NetworkModule {
    private val TIMEOUT = 10
    private val SLEEP_TIMEOUT = 5
    private val USE_SLEEP_INTERCEPTOR = false


    @Singleton
    @Provides
//    @Named("mainRetrofit")
    fun providesRetrofit(context: Context): Retrofit {

        val okHttpClientBuilder = OkHttpClient.Builder()
        okHttpClientBuilder
            .connectTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
            .addInterceptor(NetworkConnectionInterceptor(context))

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            okHttpClientBuilder
                .addInterceptor(httpLoggingInterceptor)
        }

//        //simulate long running request
//        if (USE_SLEEP_INTERCEPTOR) {
//            val networkSleepInterceptor = NetworkSleepInterceptor(
//                SLEEP_TIMEOUT, TimeUnit.SECONDS
//            )
//            okHttpClientBuilder
//                .addInterceptor(networkSleepInterceptor)
//        }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClientBuilder.build())
            .build()
    }

    @Singleton
    @Provides
    fun gson(): Gson {
        return Gson()
    }

    @Singleton
    @Provides
    fun providesFixerApiService(retrofit: Retrofit): ApiFixer {
        return retrofit.create(ApiFixer::class.java)
    }


    @Provides
    @Singleton
    fun providesContext(application: Application): Context {
        return application.applicationContext
    }
}