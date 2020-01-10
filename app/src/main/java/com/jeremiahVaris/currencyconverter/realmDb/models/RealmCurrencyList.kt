package com.jeremiahVaris.currencyconverter.realmDb.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Model for CurrencyFlagPair symbols and their full names that is saved in Realm database
 */
open class RealmCurrencyList(
    @PrimaryKey
    var key: String = "currencies",
    @SerializedName("symbols")
    // JSON string to store currency pairs, since Maps are not yet supported in Realm
    var currencyList: String? = null
) : RealmObject()
