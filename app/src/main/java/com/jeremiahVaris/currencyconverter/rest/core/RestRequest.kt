package com.jeremiahVaris.currencyconverter.rest.core

import com.jeremiahVaris.currencyconverter.rest.core.RestRequest.Builder
import com.jeremiahVaris.currencyconverter.rest.core.base.BaseResponseEvent
import retrofit2.Call

/**
 * Wrapper for HTTP request and response.
 * @param builder instance of [Builder] for build request object.
 * @param T: Response model type
 */
class RestRequest<T>(builder: Builder<T>) {
    var call: Call<T>?

    var baseResponseEvent: BaseResponseEvent<T>?

    /**
     * Check if [org.greenrobot.eventbus.EventBus] should use [org.greenrobot.eventbus.EventBus.postSticky].
     */
    val isUseStickyIntent: Boolean

    init {
        this.call = builder.call
        this.baseResponseEvent = builder.baseResponseEvent
        this.isUseStickyIntent = builder.useStickyIntent
    }

    /**
     * Check if [BaseResponseEvent] is set.
     *
     * @return true/false if [BaseResponseEvent] is not null.
     */
    fun hasBaseResponseEvent(): Boolean {
        return baseResponseEvent != null
    }

    /**
     * Builder of [RestRequest].
     * @param T: Response model type
     */
    class Builder<T> {

        var call: Call<T>? = null
        var baseResponseEvent: BaseResponseEvent<T>? = null
        var useStickyIntent: Boolean = false

        /**
         * Add [Call] instance.
         *
         * @param call instance of [Call].
         * @return self instance of [Builder]
         */
        fun call(call: Call<T>): Builder<T> {
            this.call = call
            return this
        }

        /**
         * Add [BaseResponseEvent] instance.
         *
         * @param baseResponseEvent BaseResponseEvent of [Call].
         * @return self instance of [Builder]
         */
        fun addBaseResponseEvent(baseResponseEvent: BaseResponseEvent<T>): Builder<T> {
            this.baseResponseEvent = baseResponseEvent
            return this
        }

        /**
         * Add true/false flag if use sticky intent.
         * See [org.greenrobot.eventbus.EventBus.postSticky].
         *
         * @param useStickyIntent true/false if use sticky intent.
         * @return self instance of [Builder]
         */
        fun shouldUseStickyIntent(useStickyIntent: Boolean): Builder<T> {
            this.useStickyIntent = useStickyIntent
            return this
        }

        /**
         * Create new instance of [RestRequest]
         *
         * @return new instance of [RestRequest]
         */
        fun build(): RestRequest<T> {
            return RestRequest<T>(this)
        }
    }
}

