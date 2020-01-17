package com.jeremiahVaris.currencyconverter.rest.core

import com.google.gson.JsonObject
import com.jeremiahVaris.currencyconverter.rest.core.base.NetworkFailureEvent
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Handler for HTTP response and performing events.
 * @param restRequest instance of [RestRequest] that holds request and response data.
 */
class RestCallback(private val restRequest: RestRequest<JsonObject>?) : Callback<JsonObject> {

    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
        if (restRequest != null && restRequest.hasBaseResponseEvent()) {
            restRequest.baseResponseEvent!!.setCode(response.code())
            restRequest.baseResponseEvent!!.setResponseJson(response.body())
            postEvent()
        }
    }

    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
        if (restRequest != null && !call.isCanceled) {
            postNetworkFailureEvent(t)
        }
    }

    private fun postNetworkFailureEvent(throwable: Throwable) {
        EventBus.getDefault().post(NetworkFailureEvent(throwable, restRequest?.baseResponseEvent))
    }

    /**
     * Post [BaseResponseEvent] with flag TRUE that request has been canceled.
     */
    fun cancel() {
        if (restRequest != null && restRequest.hasBaseResponseEvent()) {
            restRequest.baseResponseEvent!!.setCanceled(true)
            postEvent()
        }
    }

    /**
     * Post [BaseResponseEvent] event of [RestRequest.getBaseResponseEvent]
     * via [EventBus.post]. If [RestRequest.isUseStickyIntent] is TRUE then
     * post sticky one via [EventBus.postSticky].
     */
    private fun postEvent() {
        if (restRequest!!.isUseStickyIntent) {
            EventBus.getDefault().postSticky(restRequest.baseResponseEvent)
        } else {
            EventBus.getDefault().post(restRequest.baseResponseEvent)
        }
    }
}
