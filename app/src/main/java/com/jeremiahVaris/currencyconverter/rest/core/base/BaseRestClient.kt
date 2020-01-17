package com.jeremiahVaris.currencyconverter.rest.core.base

import com.google.gson.JsonObject
import com.jeremiahVaris.currencyconverter.rest.core.RestCall
import com.jeremiahVaris.currencyconverter.rest.core.RestCallback
import com.jeremiahVaris.currencyconverter.rest.core.RestRequest


/**
 * Client for perform HTTP request.
 */
abstract class BaseRestClient {

    /**
     * Perform HTTP request based on [RestRequest]. Response is propagated by an event.
     *
     * @param restRequest instance of [RestRequest] which holds request parameters.
     * @param T: Response model type.
     * @return instance of [RestCall] which represents HTTP request.
     */
    protected fun <T> call(restRequest: RestRequest<JsonObject>): RestCall {
        checkArgument(restRequest, "RestRequest argument cannot be null")
        checkArgument(restRequest.call, "RestRequest->call argument cannot be null")

        val callback = RestCallback(restRequest)
        restRequest.call!!.enqueue(callback)

        return RestCall(restRequest.call, callback)
    }

    /**
     * Cancel HTTP request call if has been executed.
     *
     * @param restCall instance of [RestCall] which represents HTTP request.
     */
    protected fun <T> cancelCall(restCall: RestCall?) {
        restCall?.cancel()
    }

    /**
     * Method for checking if required parameter has been provided.
     *
     * @param reference of Object to check.
     * @param message thrown via [IllegalArgumentException].
     */
    private fun checkArgument(reference: Any?, message: String) {
        if (reference == null) {
            throw IllegalArgumentException(message)
        }
    }
}