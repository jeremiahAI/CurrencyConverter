package com.jeremiahVaris.currencyconverter.rest.core

import com.jeremiahVaris.currencyconverter.rest.core.base.NetworkFailureEvent
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Handler for HTTP response and performing events.
 * @param restRequest instance of [RestRequest] that holds request and response data.
 * @param T: Response model type
 */
class RestCallback<T>(private val restRequest: RestRequest<T>?) : Callback<T> {

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (restRequest != null && restRequest.hasBaseResponseEvent()) {
            restRequest.baseResponseEvent!!.setCode(response.code())
            restRequest.baseResponseEvent!!.setResponse(response.body())
            postEvent()
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
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
