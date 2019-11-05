package com.jeremiahVaris.currencyconverter.repository.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Standard model for [rates] between base currency and other currencies.
 * This model is based on Fixer.io API response format.
 */
data class Rates(
    @SerializedName("timestamp")
    val timeStamp: String = "",

    @SerializedName("historical")
    val isHistorical: Boolean = false,

    @SerializedName("base")
    val baseCurrency: String = "EUR",

    /**
     * Date at which rates are gotten, in YYYY-MM-YY format.
     */
    @SerializedName("date")
    val date: String = "",

    @SerializedName("rates")
    val rates: TreeMap<String, String> = TreeMap()


)