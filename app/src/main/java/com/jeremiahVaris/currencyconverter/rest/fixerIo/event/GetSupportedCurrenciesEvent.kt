package com.jeremiahVaris.currencyconverter.rest.fixerIo.event

import com.jeremiahVaris.currencyconverter.rest.core.base.BaseResponseEvent
import com.jeremiahVaris.currencyconverter.rest.fixerIo.model.Currencies

class GetSupportedCurrenciesEvent : BaseResponseEvent<Currencies>()