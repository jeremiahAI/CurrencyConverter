package com.jeremiahVaris.currencyconverter.repository

import android.os.Build
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jeremiahVaris.currencyconverter.realmDb.RealmClient
import com.jeremiahVaris.currencyconverter.repository.events.FirebaseConnectionStateEvent
import com.jeremiahVaris.currencyconverter.repository.events.GetRatesFromFireBaseEvent
import com.jeremiahVaris.currencyconverter.repository.events.GetRatesFromFixerApiEvent
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
    private var apiFixerRestClient: ApiFixerRestClient,
    private var sharedPrefsCache: SharedPrefsCache
) {
    private val fireBaseCurrentAccessKeyJsonKey = "current_key"
    private val fireBaseRatesDatabasePath = "rates"
    private val fireBaseFixerApiKeysPath = "keys"

    private var database = FirebaseDatabase.getInstance().reference.child(fireBaseRatesDatabasePath)
    private var keys = FirebaseDatabase.getInstance().reference.child(fireBaseFixerApiKeysPath)
    private val LOG_TAG = "CurrencyInfoRepo"
    private var ACCESS_KEY = ""

    /**
     * Caches [Rates] data in FireBase and Realm database, for fast and inexpensive lookup later.
     * @param rates [Rates] object containing rate data.
     */
    fun cacheRatesData(rates: Rates) {
        if (!rates.rates.isNullOrEmpty()) {
            addRatesToRealmDatabase(rates)
            addRatesToFireBase(rates)
        }
    }

    /**
     * Caches [Rates] data in FireBase database.
     */
    private fun addRatesToFireBase(rates: Rates) {
        rates.date?.let {
            if (!rates.rates.isNullOrEmpty()) {
                database.child(it).setValue(rates)
            }
        }
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
        apiFixerRestClient.getSupportedCurrencies(ACCESS_KEY)
    }




    /**
     * Get the [Rates] at the specified date from either local database, FireBase, or Fixer.io API, in that order of priority.
     * @param date Date in YYYY-MM-DD format
     * @param currencies List of currencies for which exchange rates are needed
     * @param isConnectedToFirebase Boolean that shows connectivity state
     * @param isForLatestRates Defaults to false. If set to true and there is no rates data available for the specified date,
     * a [GetAllRatesFromRealmEvent] is posted to the eventBus with all the previously stored rates, for the latest of them
     * to be used while a network call is being made.
     */
    fun getRates(
        date: String,
        currencies: String,
        isConnectedToFirebase: Boolean,
        isForLatestRates: Boolean = false
    ) {
        if (RealmClient.getRates(date, currencies)) { // If rates exist in local database
            Log.d(LOG_TAG, "Rates gotten from Realm Database")
        } else {
            if (isForLatestRates) RealmClient.getAllRates()
            getRatesFromNetwork(date, currencies, isConnectedToFirebase)
        }
    }

    /**
     * Get the [Rates] at the specified date from either FireBase, or Fixer.io API, in that order of priority.
     * Posts result in [GetRatesFromFireBaseEvent] or [GetRatesFromFixerApiEvent].
     * @param date Date in YYYY-MM-DD format
     */
    fun getRatesFromNetwork(date: String, currencies: String, isConnectedToFirebase: Boolean) {

        if (isConnectedToFirebase) {
            // Check FireBase
            database.child(date).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val ratesObject = dataSnapshot.getValue(FireBaseRates::class.java)
                    if (ratesObject != null) {
                        if (ratesObject.rates.isNullOrEmpty()) {// If data exists for that date in FireBase
                            if (ratesObject.toStandardFormat().hasCurrencies(currencies))
                                EventBus.getDefault().post(GetRatesFromFireBaseEvent(ratesObject.toStandardFormat()))
                            else getRatesFromFixerApi(date, currencies)
                        } else {// Else call Fixer.io API
                            getRatesFromFixerApi(date, currencies)
                        }
                    } else getRatesFromFixerApi(date, currencies)

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(LOG_TAG, "loadPost:onCancelled", databaseError.toException())
                    getRatesFromFixerApi(date, currencies)
                }
            })
        } else
            getRatesFromFixerApi(date, currencies)

    }

    private fun addFirebaseConnectionStateListener() {
        val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    EventBus.getDefault().post(FirebaseConnectionStateEvent(true))
                } else {
                    EventBus.getDefault().post(FirebaseConnectionStateEvent(false))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(LOG_TAG, "Listener was cancelled")
            }
        })
    }

    private fun getRatesFromFixerApi(date: String, currencies: String) {
        if (date == getCurrentDateAsString()) apiFixerRestClient.getLatestRates(
            currencies,
            ACCESS_KEY
        )
        else apiFixerRestClient.getHistoricalRates(currencies, date, ACCESS_KEY)
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
        ratesObject.rates?.let {
            if (!it.isNullOrEmpty()) {
                RealmClient.addRates(ratesObject)
            }
        }
    }

    fun cacheCurrenciesList(currencies: Currencies) {
        if (!currencies.currencyList.isNullOrEmpty())
            RealmClient.saveCurrencies(currencies)
    }

    init {
        addFirebaseConnectionStateListener()
        getFixerApiKey()
    }

    private fun getFixerApiKey() {
        sharedPrefsCache.getAccessKey()?.let {
            ACCESS_KEY = it
        }


        keys.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(LOG_TAG, "loadPost:onCancelled", databaseError.toException())

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentApiKey =
                    dataSnapshot.child(fireBaseCurrentAccessKeyJsonKey).getValue(String::class.java)
                if (currentApiKey != ACCESS_KEY) {
                    if (currentApiKey != null) {
                        ACCESS_KEY = currentApiKey
                        sharedPrefsCache.saveAccessKey(currentApiKey)
                    }
                    val x = "" +
                            ""
                    ACCESS_KEY += x
                }
            }

        })

    }

}

/**
 * @return Boolean showing whether this object has rates for all the currencies passed
 */
fun Rates.hasCurrencies(currenciesToBeCheckedFor: String): Boolean {

    var hasCurrencies = true
    for (currency in currenciesToBeCheckedFor.split(",")) {
        if (!rates!!.keys.contains(currency)) {
            hasCurrencies = false
            return hasCurrencies
        }// Else move on to the next
    }
    return hasCurrencies

}

