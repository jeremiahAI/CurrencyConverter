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
    var amountBeingConverted: Int = 0
    val FIRST_AMOUNT = 111
    val SECOND_AMOUNT = 222
    private val repository = CurrencyInfoRepository()
    private val _currencyList = MutableLiveData<Currencies>()
    /**
     * [MutableLiveData] of [TreeMap] that stores [Rates] against [Rates.date] as key.
     */
    private val _rates = MutableLiveData<TreeMap<String, Rates>>()
    private val _firstCurrency = MutableLiveData<String>()
    private val _secondCurrency = MutableLiveData<String>()
    private var amountToBeConverted = 1.0
    private val _firstEtAmount = MutableLiveData<Double>()
    private val _secondEtAmount = MutableLiveData<Double>()
    private val _date = MutableLiveData<String>()

    var firstEtID = -1
    var secondEtID = -1


    init {
        repository.getSupportedCurrencies()
        EventBus.getDefault().register(this)
        getRatesAtDate("")
        _date.value = "2019-08-10"
        _firstCurrency.value = "USD"
        _secondCurrency.value = "NGN"
    }


    val currencyList: LiveData<Currencies>
        get() = _currencyList
    val rates: LiveData<TreeMap<String, Rates>>
        get() = _rates
    val firstEtValue: LiveData<Double>
        get() = _firstEtAmount
    val secondEtValue: LiveData<Double>
        get() = _secondEtAmount


    @Subscribe
    fun onCurrenciesListReceived(currencies: Currencies) {
        _currencyList.value = currencies
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


    fun Currencies.convertToString(): String {
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
     * Called to get the rates for the specified date.
     * @param date Date for which rates are to be gotten in YYYY-MM-DD format.
     */
    fun getRatesAtDate(date: String) {
        // Todo: handle no internet case
        // Todo: handle currencies not yet loaded case.
        _currencyList.value?.let { repository.getRates(date, it.convertToString()) }
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
     * Called when [Rates] are retrieved from FireBase Database. Caches rate data in Realm, updates viewModel data
     * and calls the [convert] function.
     * @param ratesEvent Event wrapper containing [Rates] object.
     */
    @Subscribe
    fun onRatesReceivedFromFirebase(ratesEvent: GetRatesFromFireBaseEvent) {
        repository.addRatesToRealmDatabase(ratesEvent.ratesObject)
        updateRatesData(ratesEvent.ratesObject)
        convertFirstAmount()
    }

    /**
     * Called when [Rates] are retrieved from Fixer.io API. Caches rate data in Realm and FireBase, updates viewModel data
     * and calls the [convert] function.
     * @param ratesEvent Event wrapper containing [Rates] object.
     */
    @Subscribe
    fun onRatesReceivedFromFixerAPI(ratesEvent: GetRatesFromFixerApiEvent) {
        // Todo: handle API call errors. E.g for simulation, wrong API key or wrong date.
        repository.cacheRatesData(ratesEvent.getResponse()!!)
        updateRatesData(ratesEvent.getResponse()!!)
        convertFirstAmount()
    }

    /**
     * Called when [Rates] are retrieved from Realm Database. Updates viewModel data and calls [convert].
     * @param ratesEvent Event wrapper containing [Rates] object.
     */
    @Subscribe
    fun onRatesReceivedFromRealm(ratesEvent: GetRatesFromRealmEvent) {
        updateRatesData(ratesEvent.ratesObject)
        convertFirstAmount()
    }

    /**
     * Updates the [Rates] data contained in this viewModel in [_rates]
     * @param ratesObject Day's rates to be added to rates data.
     */
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

    fun setFirstCurrency(fromCurrency: String) {
        _firstCurrency.value = fromCurrency
    }

    fun setSecondCurrency(toCurrency: String) {
        _secondCurrency.value = toCurrency
    }

    fun convert() {
        if (rates.value != null) {
            if (_rates.value!!.containsKey(_date.value!!)) {
                val ratesAtSpecifiedDate = _rates.value!![_date.value!!]
                var fromCurrencyValue = 0.0
                var toCurrencyValue = 0.0

                when (amountBeingConverted) {
                    FIRST_AMOUNT -> {
                        fromCurrencyValue =
                            ratesAtSpecifiedDate!!.rates[_firstCurrency.value]!!.toDouble()
                        toCurrencyValue =
                            ratesAtSpecifiedDate.rates[_secondCurrency.value]!!.toDouble()

                        _secondEtAmount.value =
                            amountToBeConverted * toCurrencyValue / fromCurrencyValue

                        Log.d("Converted value", _secondEtAmount.value.toString())
                    }
                    SECOND_AMOUNT -> {
                        fromCurrencyValue =
                            ratesAtSpecifiedDate!!.rates[_secondCurrency.value]!!.toDouble()
                        toCurrencyValue =
                            ratesAtSpecifiedDate.rates[_firstCurrency.value]!!.toDouble()

                        _firstEtAmount.value =
                            amountToBeConverted * toCurrencyValue / fromCurrencyValue

                        Log.d("Converted value", _firstEtAmount.value.toString())
                    }
                }
            } else
                getRatesAtDate(_date.value!!)
        } else
            getRatesAtDate(_date.value!!)
    }

    fun setAmountToBeConverted(amount: Double) {
        amountToBeConverted = amount
    }

    fun setFirstEtAmount(amount: Double) {
        _firstEtAmount.value = amount
    }

    fun setSecondEtAmount(amount: Double) {
        _secondEtAmount.value = amount
    }


    fun setFirstEtAmountAndConvert(amount: Double) {
        if (_firstEtAmount.value != amount) {
            _firstEtAmount.value = amount
            convertFirstAmount()
        }
    }

    fun setSecondEtAmountAndConvert(amount: Double) {
        if (_secondEtAmount.value != amount) {
            _secondEtAmount.value = amount
            convertSecondAmount()
        }
    }

    fun convertFirstAmount() {
        amountBeingConverted = FIRST_AMOUNT
        _firstEtAmount.value?.let {
            amountToBeConverted = it
            convert()
        }
    }

    fun convertSecondAmount() {
        amountBeingConverted = SECOND_AMOUNT
        _secondEtAmount.value?.let {
            amountToBeConverted = it
            convert()
        }
    }


}