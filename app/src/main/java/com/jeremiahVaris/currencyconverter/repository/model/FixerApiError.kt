package com.jeremiahVaris.currencyconverter.repository.model

data class FixerApiError(val success: Boolean, val error: FixerErrorBody)

data class FixerErrorBody(val code: Int, val type: String, val info: String)