package com.jeremiahVaris.currencyconverter.realmDb

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeremiahVaris.currencyconverter.realmDb.models.RealmRates
import com.jeremiahVaris.currencyconverter.rest.fixerIo.model.Rates
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import java.util.*


object RealmClient {
    var realm: Realm = Realm.getDefaultInstance()
    val TAG = "RealmClient"

    /**
     * Gets [RealmRates] from Realm database.
     * @param date: Date to be checked in YYYY-MM-DD format
     */
    private fun getRates(date: String) {
        val rates = realm.where(RealmRates::class.java).equalTo("date", date).findFirst()
        EventBus.getDefault().post(rates)
    }

    fun addRates(ratesObject: Rates) {
        realm.executeTransactionAsync({ realm ->

            with(realm.where(RealmRates::class.java).equalTo("date", ratesObject.date)) {
                if (isValid && ratesObject == convertToTreeMapFormat(this.findFirst()!!)) {
                    // If the record already exists for that date and is up to date
                    // pass
                } else {//
                    val realmRates = realm.createObject(RealmRates::class.java)
                    realmRates.apply {
                        isHistorical = ratesObject.isHistorical
                        date = ratesObject.date
                        baseCurrency = ratesObject.baseCurrency
                        timeStamp = ratesObject.timeStamp
                        this.rates = Gson().toJson(ratesObject.rates)
                    }
                }

            }


        }, {
            //OnSuccess
            Log.d(TAG, "On Success: Data Written Successfully!")
            getRates(ratesObject.date)
        }, {
            Log.d(TAG, "On Error: Error in saving Data!")
        })
    }

    /**
     * Converts the JSON-formatted [RealmRates] object to a TreeMap-formatted [Rates] object
     */
    private fun convertToTreeMapFormat(ratesObject: RealmRates): Rates? {
        return if (hasNonNullVales(ratesObject)) {
            val ratesInTreeMapFormat = Rates(
                ratesObject.timeStamp!!,
                ratesObject.isHistorical!!,
                ratesObject.baseCurrency!!,
                ratesObject.date!!,
                Gson().fromJson(ratesObject.rates, object : TypeToken<TreeMap<String, String>>() {}.type)
            )
            ratesInTreeMapFormat
        } else null
    }

    /**
     * Checkes that a [RealmRates] object has nonnull values
     */
    private fun hasNonNullVales(ratesObject: RealmRates): Boolean {
        return ratesObject.run {
            !timeStamp.isNullOrEmpty()
                    && isHistorical != null
                    && !baseCurrency.isNullOrEmpty()
                    && !date.isNullOrEmpty()
                    && !rates.isNullOrEmpty()
        }

    }


}