package com.jeremiahVaris.currencyconverter.rest.fixerIo.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Model for [rates] between base currency and other currencies
 */
data class Rates(
    @SerializedName("timestamp")
    val timeStamp: String,

    @SerializedName("historical")
    val isHistorical: Boolean,

    @SerializedName("base")
    val baseCurrency: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("rates")
    val rates: TreeMap<String, Double>
)