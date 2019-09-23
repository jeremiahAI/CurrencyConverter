package com.jeremiahVaris.currencyconverter

import com.jeremiahVaris.currencyconverter.repository.model.Currencies

object Utils {
    fun convertToString(currencies: Currencies): String {
        return currencies.currencyList.keys.run {
            var list = ""
            for (currency in this) {
                list += if (list.isBlank()) currency
                else ",$currency"
            }
            list
        }
    }


}

