package com.jeremiahVaris.currencyconverter.rest.fixerIo.client

import com.google.gson.JsonObject
import com.jeremiahVaris.currencyconverter.repository.events.GetRatesFromFixerApiEvent
import com.jeremiahVaris.currencyconverter.repository.events.GetSupportedCurrenciesEvent
import com.jeremiahVaris.currencyconverter.repository.model.Currencies
import com.jeremiahVaris.currencyconverter.repository.model.Rates
import com.jeremiahVaris.currencyconverter.rest.core.RestRequest
import com.jeremiahVaris.currencyconverter.rest.core.base.BaseRestClient
import com.jeremiahVaris.currencyconverter.rest.fixerIo.api.ApiFixer
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton


/**
 * REST client implementation of [BaseRestClient] for making HTTP calls.
 *
 * Singleton instance of [ApiFixerRestClient].
 *
 * @return instance of [ApiFixerRestClient].
 */
@Singleton
class ApiFixerRestClient @Inject constructor(
    var mApiFixer: ApiFixer
) : BaseRestClient() {

    //    private var mApiFixerRestCall: RestCall<Any>? = null

    /**
     * Invoke [ApiFixer.getLatestRates] via [Call] request.
     *
     * @param currencySymbols Comma separated list of symbols of currencySymbols to be compared with base currency
     * Response type is [Rates]
     */
    fun getLatestRates(currencySymbols: String, ACCESS_KEY: String) {
        val apiLatestRatesCall = mApiFixer.getLatestRates(
            ACCESS_KEY,
            currencySymbols
        )

        val restRequest = RestRequest.Builder<JsonObject>()
            .call(apiLatestRatesCall)
            .addBaseResponseEvent(GetRatesFromFixerApiEvent())
            .shouldUseStickyIntent(true)
            .build()

        call<JsonObject>(restRequest)
    }

    /**
     * Invoke [ApiFixer.getHistoricalRates] via [Call] request.
     * @param date Date in YYYY-MM-DD format
     * @param currencySymbols Comma separated list of symbols of currencySymbols to be compared with base currency
     * Response type is [Rates]
     */
    fun getHistoricalRates(
        currencySymbols: String,
        date: String,
        ACCESS_KEY: String
    ) {
        val apiLatestRatesCall = mApiFixer.getHistoricalRates(
            date.substring(0, 4), // year
            date.substring(5, 7), // month
            date.substring(8, 10), // day
            ACCESS_KEY,
            currencySymbols
        )

        val restRequest = RestRequest.Builder<JsonObject>()
            .call(apiLatestRatesCall)
            .addBaseResponseEvent(GetRatesFromFixerApiEvent())
            .shouldUseStickyIntent(true)
            .build()

        call<JsonObject>(restRequest)
    }

    /**
     * Invoke [ApiFixer.getSupportedCurrencies] via [Call] request.
     * Response type is [Currencies]
     */
    fun getSupportedCurrencies(ACCESS_KEY: String) {
        val apiLatestRatesCall = mApiFixer.getSupportedCurrencies(
            ACCESS_KEY
        )

        val restRequest = RestRequest.Builder<JsonObject>()
            .call(apiLatestRatesCall)
            .addBaseResponseEvent(GetSupportedCurrenciesEvent())
            .shouldUseStickyIntent(true)
            .build()

        // make Network call
//        mApiFixerRestCall =
        call<JsonObject>(restRequest)
    }

//    /**
//     * Cancel getLatestRates [Call] request.
//     */
//    fun cancelGetLatestRates() {
//        cancelCall(mApiFixerRestCall)
//    }


}