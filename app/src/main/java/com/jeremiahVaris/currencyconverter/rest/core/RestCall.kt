package com.jeremiahVaris.currencyconverter.rest.core

import retrofit2.Call

/**
 * Handler for make HTTP request and response.
 * * @param call instance of a Retrofit [Call] that sends a request to a server.
 * @param callback instance of a [RestCallback] that holds a response form a server.
 */
class RestCall(private val call: Call<*>?, private val callback: RestCallback?) {

    /**
     * Check if call has been executed.
     *
     * @return true/false if executed.
     */
    val isExecuted: Boolean
        get() = call != null && call.isExecuted

    /**
     * Cancel call if has not been executed yet.
     */
    fun cancel() {
        if (isExecuted) {
            call!!.cancel()

            if (callback != null) {
                callback.cancel()
            }
        }
    }
}
