package com.jeremiahVaris.currencyconverter.repository.events

import com.jeremiahVaris.currencyconverter.repository.model.Rates
import java.util.*

class GetAllRatesFromRealmEvent(val allRealmRates: TreeMap<String, Rates>)
