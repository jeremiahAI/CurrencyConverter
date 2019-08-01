package com.jeremiahVaris.currencyconverter.repository.events

import com.jeremiahVaris.currencyconverter.repository.model.Currencies
import com.jeremiahVaris.currencyconverter.rest.core.base.BaseResponseEvent

class GetSupportedCurrenciesEvent : BaseResponseEvent<Currencies>()