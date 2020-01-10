package com.jeremiahVaris.currencyconverter.repository

import android.os.Build
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jeremiahVaris.currencyconverter.realmDb.RealmClient
import com.jeremiahVaris.currencyconverter.repository.events.GetRatesFromFireBaseEvent
import com.jeremiahVaris.currencyconverter.repository.model.Currencies
import com.jeremiahVaris.currencyconverter.repository.model.FireBaseRates
import com.jeremiahVaris.currencyconverter.repository.model.Rates
import com.jeremiahVaris.currencyconverter.rest.fixerIo.client.ApiFixerRestClient
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class CurrencyInfoRepository @Inject constructor(
    private var apiFixerRestClient: ApiFixerRestClient
) {
    private val fireBaseRatesDatabasePath = "rates"

    private var database = FirebaseDatabase.getInstance().reference.child(fireBaseRatesDatabasePath)
    private val LOG_TAG = "CurrencyInfoRepo"

    /**
     * Caches [Rates] data in FireBase and Realm database, for fast and inexpensive lookup later.
     * @param rates [Rates] object containing rate data.
     */
    fun cacheRatesData(rates: Rates) {
        addRatesToRealmDatabase(rates)
        addRatesToFireBase(rates)
    }

    /**
     * Caches [Rates] data in FireBase database.
     */
    private fun addRatesToFireBase(rates: Rates) {
        database.child(rates.date).setValue(rates)
    }

    /**
     * Gets list of supported [Currencies] from Fixer.io API
     * Result gets returned via [Eventbus]
     */
    fun getSupportedCurrencies() {
        if (RealmClient.getCurrencies()) { // If rates exist in local database
            Log.d(LOG_TAG, "Currencies gotten from Realm Database")
        } else {
            getCurrenciesFromNetwork()
        }
    }

    private fun getCurrenciesFromNetwork() {
        apiFixerRestClient.getSupportedCurrencies()
    }

    /**
     * Get latest [Rates] from either local database, FireBase, or Fixer.io API, in that order of priority.
     * Result gets returned via [Eventbus]
     */
    fun getLatestRates(currencies: String) {
        getRates(getCurrentDateAsString(), currencies)
    }


    /**
     * Get the [Rates] at the specified date from either local database, FireBase, or Fixer.io API, in that order of priority.
     * @param date Date in YYYY-MM-DD format
     */
    fun getRates(date: String, currencies: String) {
        if (RealmClient.getRates(date, currencies)) { // If rates exist in local database
            Log.d(LOG_TAG, "Rates gotten from Realm Database")
        } else {
            getRatesFromNetwork(date, currencies)
        }
    }

    /**
     * Get the [Rates] at the specified date from either FireBase, or Fixer.io API, in that order of priority.
     * @param date Date in YYYY-MM-DD format
     */
    fun getRatesFromNetwork(date: String, currencies: String) {

        // Check FireBase
        database.child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val ratesObject = dataSnapshot.getValue(FireBaseRates::class.java)
                if (ratesObject != null) {// If data exists for that date in FireBase
                    if (ratesObject.toStandardFormat().hasCurrencies(currencies))
                        EventBus.getDefault().post(GetRatesFromFireBaseEvent(ratesObject.toStandardFormat()))
                    else getRatesFromFixerApi(date, currencies)
                } else {// Else call Fixer.io API
                    getRatesFromFixerApi(date, currencies)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(LOG_TAG, "loadPost:onCancelled", databaseError.toException())
                getRatesFromFixerApi(date, currencies)
            }
        })
    }

    private fun getRatesFromFixerApi(date: String, currencies: String) {
        if (date == getCurrentDateAsString()) apiFixerRestClient.getLatestRates(currencies)
        else apiFixerRestClient.getHistoricalRates(currencies, date)
    }

    /**
     * @return the current date in YYYY-MM-DD format
     */
    private fun getCurrentDateAsString(): String {
        lateinit var currentDate: String
        currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd")
            current.format(formatter)
        } else {
            val date = Date()
            val formatter = SimpleDateFormat("YYYY-MM-dd")
            formatter.format(date)
        }
        return currentDate
    }

    fun addRatesToRealmDatabase(ratesObject: Rates) {
        RealmClient.addRates(ratesObject)
    }

    fun cacheCurrenciesList(currencies: Currencies) {
        RealmClient.saveCurrencies(currencies)
    }


}

/**
 * @return Boolean showing whether this object has rates for all the currencies passed
 */
fun Rates.hasCurrencies(currenciesToBeCheckedFor: String): Boolean {

    var hasCurrencies = true
    for (currency in currenciesToBeCheckedFor.split(",")) {
        if (!rates.keys.contains(currency)) {
            hasCurrencies = false
            return hasCurrencies
        }// Else move on to the next
    }
    return hasCurrencies

}

