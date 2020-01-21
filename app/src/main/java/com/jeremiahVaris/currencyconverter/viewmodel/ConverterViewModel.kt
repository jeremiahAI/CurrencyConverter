package com.jeremiahVaris.currencyconverter.viewmodel

import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.jeremiahVaris.currencyconverter.repository.CurrencyInfoRepository
import com.jeremiahVaris.currencyconverter.repository.events.*
import com.jeremiahVaris.currencyconverter.repository.model.Currencies
import com.jeremiahVaris.currencyconverter.repository.model.FixerApiError
import com.jeremiahVaris.currencyconverter.repository.model.Rates
import com.jeremiahVaris.currencyconverter.rest.core.NoConnectivityException
import com.jeremiahVaris.currencyconverter.rest.core.base.NetworkFailureEvent
import com.jeremiahVaris.currencyconverter.viewmodel.AmountTypeToBeConverted.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject


class ConverterViewModel @Inject constructor(
    private val repository: CurrencyInfoRepository
) : ViewModel() {
    private var isConnectedToFirebase = false
    private var ratesInUse = MutableLiveData<Rates>()
    private val _currencyList = MutableLiveData<Currencies>()
    /**
     * [MutableLiveData] of [TreeMap] that stores [Rates] against [Rates.date] as key.
     */
    private val _rates = MutableLiveData<TreeMap<String, Rates>>()
    private val _firstCurrency = MutableLiveData<String>()
    private val _secondCurrency = MutableLiveData<String>()
    private val _firstEtAmount = MutableLiveData<Double>()
    private val _secondEtAmount = MutableLiveData<Double>()
    private val _secondEtAmountHint = MutableLiveData<Double>()
    private val _currentDate = MutableLiveData<String>()
    private val _dateOfRatesInUse = MutableLiveData<String>()
    private val _minorNetworkError = MutableLiveData<String>()
    private val _majorNetworkError = MutableLiveData<String>()
    private val _isRefreshing = MutableLiveData<Boolean>()


    var firstEtID = -1
    var secondEtID = -1

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
    val firstCurrencyFullName
        get() = _currencyList.value?.currencyList?.get(_firstCurrency.value) ?: ""

    val secondCurrencyFullName
        get() = _currencyList.value?.currencyList?.get(_secondCurrency.value) ?: ""

    val minorNetworkError: LiveData<String>
        get() = _minorNetworkError
    val majorNetworkError: LiveData<String>
        get() = _majorNetworkError
    val isRefreshing: LiveData<Boolean>
        get() = _isRefreshing
    val dateOfRatesInUse: LiveData<String>
        get() = Transformations.map(ratesInUse) { rates ->
            rates.timeStamp!!.fromTimestampToStringForDisplay()
        }


    init {
        EventBus.getDefault().register(this)
        _currentDate.value = getCurrentDate()
        _dateOfRatesInUse.value = _currentDate.value
//        getSupportedCurrencies()
//        _dateOfRatesInUse.value?.let {
//            getRatesAtDate(it, true)
//        }
    }

    fun getSupportedCurrencies() {
        repository.getSupportedCurrencies()
    }

    //**** Repository getters ****//

    /**
     * Called to get the latest rates for the current date.
     */
    private fun getLatestRates() {
        _isRefreshing.value = true
        _currentDate.value?.also {
            getRatesAtDate(it, true)
        }
            ?: getRatesAtDate(getCurrentDate(), true)
    }

    /**
     * Called to get the rates for the specified date.
     * @param date Date for which rates are to be gotten in YYYY-MM-DD format.
     */
    private fun getRatesAtDate(date: String, isForLatestRates: Boolean) {
        if (_dateOfRatesInUse.value == _currentDate.value) _isRefreshing.value = true

        _currencyList.value?.also {
            repository.getRates(
                date,
                it.convertToString(),
                isConnectedToFirebase,
                isForLatestRates
            )
        }
            ?: repository.getSupportedCurrencies()

    }


    //***** Subscriptions to events *****//


    /**
     * Called when a [GetSupportedCurrenciesEvent] is posted on EventBus.
     * Updates the currencies list in the ViewModel.
     */
    @Subscribe
    fun updateSupportedCurrencies(supportedCurrenciesEvent: GetSupportedCurrenciesEvent) {

        supportedCurrenciesEvent.getResponse<Currencies>().let {
            if (it != null) {
                if (!it.currencyList.isNullOrEmpty()) {
                    _currencyList.value = supportedCurrenciesEvent.getResponse()
                    repository.cacheCurrenciesList(it)
                    getLatestRates()
                } else {
                    val errorResponse = supportedCurrenciesEvent.getResponse<FixerApiError>()
                    if (errorResponse?.error?.code == 104)
                        repository.switchKeys()
                    _majorNetworkError.value = "Error retrieving currencies list."
                }
            } else _majorNetworkError.value = "Error retrieving currencies list."
        }
    }

    /**
     * Called when [Currencies] are retrieved from Realm Database.
     * Updates the currencies list in the ViewModel.
     * @param supportedCurrenciesEvent Event wrapper containing [Currencies] object.
     */
    @Subscribe
    fun updateSupportedCurrencies(supportedCurrenciesEvent: GetSupportedCurrenciesFromRealmEvent) {
        if (!supportedCurrenciesEvent.currencies.currencyList.isNullOrEmpty()) {
            _currencyList.value = supportedCurrenciesEvent.currencies
            getLatestRates()
        }
    }

    /**
     * Called when [Rates] are retrieved from FireBase Database. Caches rate data in Realm, updates viewModel data
     * and calls the [convert] function.
     * @param ratesEvent Event wrapper containing [Rates] object.
     */
    @Subscribe
    fun onRatesReceivedFromFirebase(ratesEvent: GetRatesFromFireBaseEvent) {
        if (!ratesEvent.ratesObject.rates.isNullOrEmpty()) {
            repository.addRatesToRealmDatabase(ratesEvent.ratesObject)
            onRatesReceived(ratesEvent.ratesObject)
        }
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
     * Called when all [Rates] are retrieved from Realm Database. Updates viewModel data and calls [convert].
     * @param ratesEvent Event wrapper containing [Rates] object.
     */
    @Subscribe
    fun onAllRatesInRealmReceived(ratesEvent: GetAllRatesFromRealmEvent) {
        val allRealmRatesMap = ratesEvent.allRealmRates
        allRealmRatesMap.forEach { (_, rates) ->
            updateRatesData(rates)
        }
        allRealmRatesMap.firstEntry()?.let {
            ratesInUse.value = it.value
            _dateOfRatesInUse.value = it.value.date
            convertHint()
        }
    }

    /**
     * Called when [getSupportedCurrencies] call fails.
     * @param networkFailureEvent Failure event containing event type, and throwable with error message.
     */
    @Subscribe
    fun onGetSupportedCurrenciesNetworkErrorReceived(networkFailureEvent: NetworkFailureEvent<GetSupportedCurrenciesEvent>) {
        if (_currencyList.value == null) _majorNetworkError.value =
            networkFailureEvent.throwable?.message ?: "An error occurred"
    }

    /**
     * Called when [getLatestRates] call fails.
     * @param networkFailureEvent Failure event containing event type, and throwable with error message.
     */
    @Subscribe
    fun onGetLatestRatesErrorReceived(networkFailureEvent: NetworkFailureEvent<GetRatesFromFixerApiEvent>) {
        _isRefreshing.value = false
        if (ratesInUse.value != null) _minorNetworkError.value =
            networkFailureEvent.throwable?.message ?: "An error occurred"
        else _majorNetworkError.value =
            networkFailureEvent.throwable?.message ?: "An error occurred"
    }

    /**
     * Called when any network call fails.
     * @param networkFailureEvent Failure event containing event type, and throwable with error message.
     */
    @Subscribe
    fun onNetworkErrorReceived(networkFailureEvent: NetworkFailureEvent<Any?>) {
        if (networkFailureEvent.throwable is NoConnectivityException) {
            if (ratesInUse.value == null) _majorNetworkError.value =
                networkFailureEvent.throwable?.message ?: "An error occurred"
            else _minorNetworkError.value =
                networkFailureEvent.throwable?.message ?: "An error occurred"
        }
    }

    /**
     * Called
     */
    fun onFirebaseConnectionStateReceived(firebaseConnectionStateEvent: FirebaseConnectionStateEvent) {
        isConnectedToFirebase = firebaseConnectionStateEvent.isConnected
    }

    private fun onRatesReceived(ratesObject: Rates) {
        _isRefreshing.value = false
        ratesInUse.value = ratesObject
        _dateOfRatesInUse.value = ratesObject.date
        updateRatesData(ratesObject)
        convertHint()
    }

    /**
     * Updates the [Rates] data contained in this viewModel in [_rates]
     * @param ratesObject Day's rates to be added to rates data.
     */
    private fun updateRatesData(ratesObject: Rates) {

        if (_rates.value == null) {
            _rates.value = TreeMap<String, Rates>().apply { put(ratesObject.date!!, ratesObject) }
        } else _rates.value?.put(ratesObject.date!!, ratesObject)
        Log.d("Rates", _rates.value?.keys.toString())
    }


    fun convertFirstAmount() {
        _firstEtAmount.value?.let {
            convert(it, FIRST_AMOUNT)
        }
    }

    fun convertSecondAmount() {
        _secondEtAmount.value?.let {
            convert(it, SECOND_AMOUNT)
        }
    }

    fun convertHint() {
        convert(1.0, HINT)
    }

    private fun convert(amount: Double, amountTypeToBeConverted: AmountTypeToBeConverted) {
        if (rates.value != null) {
            if (_rates.value!!.containsKey(_dateOfRatesInUse.value!!)) {
                var fromCurrencyValue = 0.0
                var toCurrencyValue = 0.0

                when (amountTypeToBeConverted) {
                    FIRST_AMOUNT -> {
                        _firstCurrency.value?.let { firstCurrency ->
                            _secondCurrency.value?.let { secondCurrency ->
                                fromCurrencyValue =
                                    ratesInUse.value!!.rates!![firstCurrency]!!.toDouble()
                                toCurrencyValue =
                                    ratesInUse.value!!.rates!![secondCurrency]!!.toDouble()

                                _secondEtAmount.value =
                                    amount * toCurrencyValue / fromCurrencyValue

                                Log.d("Converted value", _secondEtAmount.value.toString())
                            }
                        }
                    }
                    SECOND_AMOUNT -> {
                        _firstCurrency.value?.let { firstCurrency ->
                            _secondCurrency.value?.let { secondCurrency ->
                                fromCurrencyValue =
                                    ratesInUse.value!!.rates!![secondCurrency]!!.toDouble()
                                toCurrencyValue =
                                    ratesInUse.value!!.rates!![firstCurrency]!!.toDouble()

                                _firstEtAmount.value =
                                    amount * toCurrencyValue / fromCurrencyValue

                                Log.d("Converted value", _firstEtAmount.value.toString())
                            }
                        }
                    }
                    HINT -> {
                        _firstCurrency.value?.let { firstCurrency ->
                            _secondCurrency.value?.let { secondCurrency ->
                                fromCurrencyValue =
                                    ratesInUse.value!!.rates!![firstCurrency]!!.toDouble()
                                toCurrencyValue =
                                    ratesInUse.value!!.rates!![secondCurrency]!!.toDouble()

                                _secondEtAmountHint.value = toCurrencyValue / fromCurrencyValue

                                Log.d("Converted value", _secondEtAmountHint.value.toString())
                            }

                        }
                    }
                }
            }
//            else
//                getRatesAtDate(
//                    _dateOfRatesInUse.value!!,
//                    _dateOfRatesInUse.value == _currentDate.value
//                )
        }
//        else
//            getRatesAtDate(_dateOfRatesInUse.value!!, _dateOfRatesInUse.value == _currentDate.value)
    }

    /**
     * Refreshes the [_rates] to get the latest value.
     */
    fun refresh() {
        _isRefreshing.value = true

        _currencyList.value?.also {
            repository.getRatesFromNetwork(
                _dateOfRatesInUse.value!!,
                it.convertToString(),
                isConnectedToFirebase
            )
        }
            ?: repository.getSupportedCurrencies()
    }

    fun setFirstCurrency(firstCurrency: String) {
        _firstCurrency.value = firstCurrency
    }

    fun setSecondCurrency(secondCurrency: String) {
        _secondCurrency.value = secondCurrency
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

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

}

/**
 * @return Current date in yyyy-MM-dd format.
 */
private fun getCurrentDate(): String {
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

/**
 * Converts a timestamp to string formatted for display
 * @return Formatted string or an empty string if something is wrong.
 */
private fun String.fromTimestampToStringForDisplay(): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM, yyyy. hh:mm aaa z")
        val date = Date(this.toLong() * 1000)
        sdf.format(date)
    } catch (e: Exception) {
        ""
    }
}

private fun Currencies.convertToString(): String {
    return currencyList?.let {
        it.keys.run {
            var list = ""
            for (currency in this) {
                list += if (list.isBlank()) currency
                else ",$currency"
            }
            list
        }
    } ?: ""
}


private enum class AmountTypeToBeConverted {
    FIRST_AMOUNT,
    SECOND_AMOUNT,
    HINT
}
