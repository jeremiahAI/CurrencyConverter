package com.jeremiahVaris.currencyconverter.repository.events

import com.jeremiahVaris.currencyconverter.repository.model.Rates
import com.jeremiahVaris.currencyconverter.rest.core.base.BaseResponseEvent

class GetRatesFromFixerApiEvent : BaseResponseEvent<Rates>()
