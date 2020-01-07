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
import com.jeremiahVaris.currencyconverter.rest.core.base.NoNetworkEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ConverterViewModel @Inject constructor(
    private val repository: CurrencyInfoRepository
) : ViewModel() {
    private var ratesAtSpecifiedDate: Rates? = null
    private var amountBeingConverted: Int = 0
    private val FIRST_AMOUNT = 111
    private val SECOND_AMOUNT = 222
    private val HINT = 333
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
    private val _secondEtAmountHint = MutableLiveData<Double>()
    private val _currentDate = MutableLiveData<String>()
    private val _dateOfCurrentRates = MutableLiveData<String>()
    private val _networkError = MutableLiveData<NoNetworkEvent>()
    private val _isRefreshing = MutableLiveData<Boolean>()


    var firstEtID = -1
    var secondEtID = -1


    init {
        getSupportedCurrencies()
        EventBus.getDefault().register(this)
        getRatesAtDate("")
        _currentDate.value =
            getCurrentDate()
    }

    fun getSupportedCurrencies() {
        repository.getSupportedCurrencies()
    }

    /**
     * @return Current date in yyyy-MM-dd format.
     */
    private fun getCurrentDate(): String {
        val c = Calendar.getInstance().time

        val df = SimpleDateFormat("yyyy-MM-dd")
        val formattedDate: String = df.format(c)
        return formattedDate
    }


    val currencyList: LiveData<Currencies>
        get() = _currencyList
    val rates: LiveData<TreeMap<String, Rates>>
        get() = _rates
    val firstEtValue: LiveData<Double>
        get() = _firstEtAmount
    val secondEtValue: LiveData<Double>
        get() = _secondEtAmount
    val secondEtHint: LiveData<Double>
        get() = _secondEtAmountHint
    val firstCurrencyFullName: String?
        get() = _currencyList.value?.currencyList?.get(_firstCurrency.value)
    val secondCurrencyFullName: String?
        get() = _currencyList.value?.currencyList?.get(_secondCurrency.value)
    val networkError: LiveData<NoNetworkEvent>
        get() = _networkError
    val isRefreshing: LiveData<Boolean>
        get() = _isRefreshing
    val dateOfSpecifiedRates: LiveData<String>
        get() = MutableLiveData<String>().apply {
            this.value = ratesAtSpecifiedDate?.timeStamp?.fromTimestampToStringForDisplay()
        }


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
        onRatesReceived(ratesEvent.ratesObject)
    }

    private fun onRatesReceived(ratesObject: Rates) {
        _isRefreshing.value = false
        ratesAtSpecifiedDate = ratesObject
        updateRatesData(ratesObject)
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
        onRatesReceived(ratesEvent.getResponse()!!)
    }

    /**
     * Called when [Rates] are retrieved from Realm Database. Updates viewModel data and calls [convert].
     * @param ratesEvent Event wrapper containing [Rates] object.
     */
    @Subscribe
    fun onRatesReceivedFromRealm(ratesEvent: GetRatesFromRealmEvent) {
        onRatesReceived(ratesEvent.ratesObject)
    }

    /**
     * Called when [Rates] are retrieved from Realm Database. Updates viewModel data and calls [convert].
     * @param ratesEvent Event wrapper containing [Rates] object.
     */
    @Subscribe
    fun onNoNetworkErrorReceived(noNetworkEvent: NoNetworkEvent) {
        _networkError.value = noNetworkEvent
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

    fun setFirstCurrency(firstCurrency: String) {
        _firstCurrency.value = firstCurrency
    }

    fun setSecondCurrency(secondCurrency: String) {
        _secondCurrency.value = secondCurrency
    }

    private fun convert() {
        if (rates.value != null) {
            if (_rates.value!!.containsKey(_currentDate.value!!)) {
                var fromCurrencyValue = 0.0
                var toCurrencyValue = 0.0

                when (amountBeingConverted) {
                    FIRST_AMOUNT -> {
                        fromCurrencyValue =
                            ratesAtSpecifiedDate!!.rates[_firstCurrency.value]!!.toDouble()
                        toCurrencyValue =
                            ratesAtSpecifiedDate!!.rates[_secondCurrency.value]!!.toDouble()

                        _secondEtAmount.value =
                            amountToBeConverted * toCurrencyValue / fromCurrencyValue

                        Log.d("Converted value", _secondEtAmount.value.toString())
                    }
                    SECOND_AMOUNT -> {
                        fromCurrencyValue =
                            ratesAtSpecifiedDate!!.rates[_secondCurrency.value]!!.toDouble()
                        toCurrencyValue =
                            ratesAtSpecifiedDate!!.rates[_firstCurrency.value]!!.toDouble()

                        _firstEtAmount.value =
                            amountToBeConverted * toCurrencyValue / fromCurrencyValue

                        Log.d("Converted value", _firstEtAmount.value.toString())
                    }
                    HINT -> {
                        fromCurrencyValue =
                            ratesAtSpecifiedDate!!.rates[_firstCurrency.value]!!.toDouble()
                        toCurrencyValue =
                            ratesAtSpecifiedDate!!.rates[_secondCurrency.value]!!.toDouble()

                        _secondEtAmountHint.value =
                            amountToBeConverted * toCurrencyValue / fromCurrencyValue

                        Log.d("Converted value", _secondEtAmountHint.value.toString())
                    }
                }
            } else
                getRatesAtDate(_currentDate.value!!)
        } else
            getRatesAtDate(_currentDate.value!!)
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

    fun convertHint() {
        amountBeingConverted = HINT
        amountToBeConverted = 1.0
        convert()
    }

    /**
     * Refreshes the [_rates] to get the latest value.
     */
    fun refresh() {
        _isRefreshing.value = true

        _currencyList.value?.let {
            repository.getRatesFromNetwork(getCurrentDate(), it.convertToString())
        }
            ?: repository.getSupportedCurrencies()
    }


}

/**
 * Converts a timestamp to string formatted for display
 * @return Formatted string or null if something is wrong.
 */
private fun String.fromTimestampToStringForDisplay(): String {
    // Todo: implement
    return "7th Jan, 2020. 01:00 GMT+1"
}
