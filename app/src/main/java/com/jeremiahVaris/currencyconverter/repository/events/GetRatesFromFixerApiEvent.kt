package com.jeremiahVaris.currencyconverter.repository.events

import com.google.gson.JsonObject
import com.jeremiahVaris.currencyconverter.rest.core.base.BaseResponseEvent

class GetRatesFromFixerApiEvent : BaseResponseEvent<JsonObject>()
