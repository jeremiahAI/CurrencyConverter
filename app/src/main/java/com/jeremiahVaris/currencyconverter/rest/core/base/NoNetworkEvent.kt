package com.jeremiahVaris.currencyconverter.rest.core.base

class NetworkFailureEvent<T>(var throwable: Throwable?, var event: BaseResponseEvent<T>? = null)
