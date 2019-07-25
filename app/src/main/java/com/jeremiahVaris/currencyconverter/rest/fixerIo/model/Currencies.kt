package com.jeremiahVaris.currencyconverter.rest.fixerIo.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Currencies(
    @SerializedName("symbols")
    val currencyList: TreeMap<String, String>
)