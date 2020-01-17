package com.jeremiahVaris.currencyconverter.rest.core.base

import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * Event propagated when HTTP request accomplished.
 * @param T The type of the value.
 */
abstract class BaseResponseEvent<T> {

    private var response: T? = null
    private var responseJsonObject: JsonObject? = null
    private var responseCode = -1
    private var isCanceled = false

    /**
     * Get response of HTTP request.
     *
     * @return response data.
     */
    inline fun <reified Y> getResponse(): Y? {
        return Gson().fromJson(getResponseJSON(), Y::class.java)
    }

    /**
     * Set response of HTTP request.
     *
     * @param response data.
     */
    fun setResponse(response: T?) {
        this.response = response
    }

    /**
     * Get response of HTTP request.
     *
     * @return response data.
     */
    fun getResponseJSON(): JsonObject? {
        return responseJsonObject
    }

    /**
     * Set response of HTTP request.
     *
     * @param response data.
     */
    fun setResponseJson(response: JsonObject?) {
        this.responseJsonObject = response

    }

    /**
     * Get HTTP status code.
     *
     * @return HTTP status code.
     */
    fun getCode(): Int {
        return responseCode
    }

    /**
     * Set HTTP status responseCode.
     *
     * @param responseCode HTTP status responseCode.
     */
    fun setCode(responseCode: Int) {
        this.responseCode = responseCode
    }

    /**
     * Returns true if request has been canceled.
     *
     * @return true/false.
     */
    fun isCanceled(): Boolean {
        return isCanceled
    }

    /**
     * Sets true if request has been canceled.
     *
     * @param canceled true/false.
     */
    fun setCanceled(canceled: Boolean) {
        isCanceled = canceled
    }

    /**
     * Returns true if request contains response.
     *
     * @return true/false.
     */
    fun hasResponse(): Boolean {
        return response != null
    }


}