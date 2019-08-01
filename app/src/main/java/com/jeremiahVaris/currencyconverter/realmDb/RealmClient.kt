package com.jeremiahVaris.currencyconverter.realmDb

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeremiahVaris.currencyconverter.realmDb.models.RealmRates
import com.jeremiahVaris.currencyconverter.repository.events.GetRatesFromRealmEvent
import com.jeremiahVaris.currencyconverter.repository.hasCurrencies
import com.jeremiahVaris.currencyconverter.repository.model.Rates
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import java.util.*


object RealmClient {
    var realm: Realm = Realm.getDefaultInstance()
    val TAG = "RealmClient"

    /**
     * Gets [RealmRates] from Realm database. Rates Object result posted via [GetRatesFromRealmEvent].
     * @param date Date to be checked in YYYY-MM-DD format
     * @return Value showing whether rates exist in realm database for the specified date
     */
    fun getRates(date: String, currencies: String): Boolean {
        val rates = realm.where(RealmRates::class.java).equalTo("date", date).findFirst()

        // Post result if available
        return if (rates != null) {
            if (rates.isValid && convertToTreeMapFormat(rates)!!.hasCurrencies(currencies)) {
                // If Realm Database has all requested currencies
                EventBus.getDefault().post(GetRatesFromRealmEvent(convertToTreeMapFormat(rates)!!))
                true
            } else false
        } else false
    }

    /**
     * Adds [RealmRates] to the Realm database.
     * @param ratesObject Rates object containing the rates.
     */
    fun addRates(ratesObject: Rates) {
//        // Overwrite realm instance for this thread.
//        val backGroundrealm = Realm.getDefaultInstance()

        // Attempt to add data
        realm.executeTransactionAsync({
            val realmRates = convertToRealmRatesObject(ratesObject)

            it.copyToRealmOrUpdate(realmRates)
        }, {
            Log.d(TAG, "On Success: Data Written Successfully!")
        }, {
            Log.d(TAG, "On Error: Error in saving Data: ${it.message}")
        })

//        realm.executeTransactionAsync({ realm ->
//
//            with(realm.where(RealmRates::class.java).equalTo("date", ratesObject.date)) {
//                val ratesInTreeMapFormat = convertToTreeMapFormat(this.findFirst()!!)
//                if (isValid && ratesObject == ratesInTreeMapFormat) {
//                    // If the record already exists for that date and is up to date
//                    // pass
//                } else {//
//                    val realmRates = realm.createObject(RealmRates::class.java)
//                    realmRates.apply {
//                        isHistorical = ratesObject.isHistorical
//                        date = ratesObject.date
//                        baseCurrency = ratesObject.baseCurrency
//                        timeStamp = ratesObject.timeStamp
//                        this.rates = Gson().toJson(ratesObject.rates)
//                    }
//                }
//            }
//        }, {
//            //OnSuccess
//            Log.d(TAG, "On Success: Data Written Successfully!")
//        }, {
//            //OnFailure
//            Log.d(TAG, "On Error: Error in saving Data: ${it.message}")
//        })
    }

    /**
     * Converts a [Rates] object to a [RealmRates] object, to be able to save in the Realm database.
     * @param ratesObject [Rates] object to be converted.
     * @return Converted [RealmRates] object.
     */
    private fun convertToRealmRatesObject(ratesObject: Rates): RealmRates {
        return RealmRates().apply {
            isHistorical = ratesObject.isHistorical
            date = ratesObject.date
            baseCurrency = ratesObject.baseCurrency
            timeStamp = ratesObject.timeStamp
            this.rates = Gson().toJson(ratesObject.rates)
        }
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