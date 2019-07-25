package com.jeremiahVaris.currencyconverter.rest.fixerIo.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Model for Currency symbols and their full names
 */
data class Currencies(
    @SerializedName("symbols")
    val currencyList: TreeMap<String, String>
)