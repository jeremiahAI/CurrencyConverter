package com.jeremiahVaris.currencyconverter.repository.model

import com.google.gson.annotations.SerializedName
import java.util.*


/**
 * FireBase model for [rates] between base currency and other currencies.
 * Adapted version of the standard [Rates], since FireBase does not support [TreeMap]
 */
data class FireBaseRates(
    @SerializedName("timestamp")
    val timeStamp: String = "",

    @SerializedName("historical")
    val historical: Boolean = true,

    @SerializedName("base")
    val baseCurrency: String = "EUR",

    /**
     * Date at which rates are gotten, in YYYY-MM-YY format.
     */
    @SerializedName("date")
    val date: String = "",

    @SerializedName("rates")
    val rates: HashMap<String, String> = HashMap()
) {
    fun toStandardFormat(): Rates {
        return Rates(timeStamp, historical, baseCurrency, date, rates.convertToTreeMap())
    }
}

private fun HashMap<String, String>.convertToTreeMap(): TreeMap<String, String> {
    val treeMap = TreeMap<String, String>()
    for ((key, value) in this) {
        treeMap[key] = value
    }
    return treeMap
}

