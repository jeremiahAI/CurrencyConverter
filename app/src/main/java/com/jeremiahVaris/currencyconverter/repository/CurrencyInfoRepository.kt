package com.jeremiahVaris.currencyconverter.repository

import com.jeremiahVaris.currencyconverter.rest.fixerIo.client.ApiFixerRestClient

class CurrencyInfoRepository {

    /**
     * Gets list of supported [Currencies] from Fixer.io API
     * Result gets returned via [Eventbus]
     */
    fun getSupportedCurrencies() {
        ApiFixerRestClient.getSupportedCurrencies()
    }
}