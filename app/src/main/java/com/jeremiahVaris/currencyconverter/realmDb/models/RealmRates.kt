package com.jeremiahVaris.currencyconverter.realmDb.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Realm model for [rates] between base currency and other currencies
 */
open class RealmRates(
//    @SerializedName("timestamp")
    var timeStamp: String? = null,

//    @SerializedName("historical")
    var isHistorical: Boolean? = null,

//    @SerializedName("base")
    var baseCurrency: String? = null,

//    @SerializedName("date")
    @PrimaryKey
    var date: String? = null,

//    @SerializedName("rates")
    // JSON string to store rates values, since Maps are not yet supported in Realm
    var rates: String? = null
) : RealmObject()