package com.jeremiahVaris.currencyconverter.rest.fixerIo.api

import com.jeremiahVaris.currencyconverter.repository.model.Currencies
import com.jeremiahVaris.currencyconverter.repository.model.Rates
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


/**
 * API for making calls to https://fixer.io/
 */
interface ApiFixer {

    /**
     * Endpoint to retrieve list of supported [Currencies]
     * @param ACCESS_KEY: Fixer.io API key
     */
    @GET("api/symbols")
    fun getSupportedCurrencies(
        @Query("access_key") ACCESS_KEY: String
    ): Call<Currencies>


    /**
     * Endpoint to retrieve latest [Rates]
     * @param ACCESS_KEY: Fixer.io API key
     * @param currencySymbols Comma separated list of symbols of currencies to be compared with base currency
     */
    @GET("api/latest")
    fun getLatestRates(
        @Query("access_key") ACCESS_KEY: String,
        @Query("symbols") currencySymbols: String
    ): Call<Rates>


    /**
     * Endpoint to retrieve historical [Rates]
     * @param ACCESS_KEY: Fixer.io API key
     * @param year: year of the date [Rates] should be checked for in YYYY format
     * @param month: month of the date [Rates] should be checked for in MM format
     * @param day: day of the date [Rates] should be checked for in DD format
     * @param currencySymbols Comma separated list of symbols of currencies to be compared with base currency
     */
    @GET("api/{year}-{month}-{day}")
    fun getHistoricalRates(
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String,
        @Query("access_key") ACCESS_KEY: String,
        @Query("symbols") currencySymbols: String
    ): Call<Rates>

}
