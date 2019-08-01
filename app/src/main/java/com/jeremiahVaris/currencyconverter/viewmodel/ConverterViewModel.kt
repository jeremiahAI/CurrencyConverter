package com.jeremiahVaris.currencyconverter.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jeremiahVaris.currencyconverter.repository.CurrencyInfoRepository
import com.jeremiahVaris.currencyconverter.repository.events.GetRatesFromFireBaseEvent
import com.jeremiahVaris.currencyconverter.repository.events.GetRatesFromFixerApiEvent
import com.jeremiahVaris.currencyconverter.repository.events.GetRatesFromRealmEvent
import com.jeremiahVaris.currencyconverter.repository.events.GetSupportedCurrenciesEvent
import com.jeremiahVaris.currencyconverter.repository.model.Currencies
import com.jeremiahVaris.currencyconverter.repository.model.Rates
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

class ConverterViewModel : ViewModel() {
    private val repository = CurrencyInfoRepository()
    private val _currencyList = MutableLiveData<Currencies>()
    /**
     * [MutableLiveData] of [TreeMap] that stores [Rates] against [Rates.date] as key.
     */
    private val _rates = MutableLiveData<TreeMap<String, Rates>>()


    init {
        repository.getSupportedCurrencies()
        EventBus.getDefault().register(this)
    }


    val currencyList: LiveData<Currencies>
        get() = _currencyList
    val rates: LiveData<TreeMap<String, Rates>>
        get() = _rates


    @Subscribe
    fun onCurrenciesListReceived(currencies: Currencies) {
        _currencyList.value = currencies
    }

    private fun Currencies.convertToString(): String {
        return currencyList.keys.run {
            var list = ""
            for (currency in this) {
                list += if (list.isBlank()) currency
                else ",$currency"
            }
            list
        }
    }


    /**
     * Called to get the latest rates for the current date.
     */
    fun getLatestRates() {
        // Todo: handle no internet case
        // Todo: handle currencies not yet loaded case.
        _currencyList.value?.let { repository.getLatestRates(it.convertToString()) }
            ?: repository.getSupportedCurrencies()

    }

    /**
     * Called when a [GetSupportedCurrenciesEvent] is posted on EventBus.
     * Updates the currencies list in the ViewModel.
     */
    @Subscribe
    fun updateSupportedCurrencies(supportedCurrenciesEvent: GetSupportedCurrenciesEvent) {
        _currencyList.value = supportedCurrenciesEvent.getResponse()
    }

    /**
     * Called when [Rates] are retrieved from FireBase Database. Caches rate data in Realm and updates viewModel data.
     * @param ratesEvent Event wrapper containing [Rates] object.
     */
    @Subscribe
    fun onRatesReceivedFromFirebase(ratesEvent: GetRatesFromFireBaseEvent) {
        repository.addRatesToRealmDatabase(ratesEvent.ratesObject)
        updateRatesData(ratesEvent.ratesObject)
    }

    /**
     * Called when [Rates] are retrieved from Fixer.io API. Caches rate data in Realm and FireBase and updates viewModel data.
     * @param ratesEvent Event wrapper containing [Rates] object.
     */
    @Subscribe
    fun onRatesReceivedFromFixerAPI(ratesEvent: GetRatesFromFixerApiEvent) {
        repository.cacheRatesData(ratesEvent.getResponse()!!)
        updateRatesData(ratesEvent.getResponse()!!)
    }

    /**
     * Called when [Rates] are retrieved from Realm Database. Updates viewModel data.
     * @param ratesEvent Event wrapper containing [Rates] object.
     */
    @Subscribe
    fun onRatesReceivedFromRealm(ratesEvent: GetRatesFromRealmEvent) {
        updateRatesData(ratesEvent.ratesObject)
    }

    private fun updateRatesData(ratesObject: Rates) {

        if (_rates.value == null) {
            _rates.value = TreeMap<String, Rates>().apply { put(ratesObject.date, ratesObject) }
        } else _rates.value?.put(ratesObject.date, ratesObject)
        Log.d("Rates", _rates.value?.keys.toString())
    }


    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

}