package com.jeremiahVaris.currencyconverter

import android.app.Application
import com.jeremiahVaris.currencyconverter.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

class ConverterApplication : Application(), HasAndroidInjector {

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Any>


    override fun onCreate() {
        super.onCreate()
        // initialize Dagger
        DaggerAppComponent.builder().application(this).build().inject(this)

        Realm.init(this)
        val config = RealmConfiguration.Builder()
//            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(config)
    }

    override fun androidInjector(): AndroidInjector<Any> = activityInjector

}