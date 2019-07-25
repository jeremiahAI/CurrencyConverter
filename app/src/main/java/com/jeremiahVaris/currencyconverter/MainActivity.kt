package com.jeremiahVaris.currencyconverter

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jeremiahVaris.currencyconverter.rest.fixerIo.client.ApiFixerRestClient
import com.jeremiahVaris.currencyconverter.rest.fixerIo.event.GetLatestRatesEvent
import com.jeremiahVaris.currencyconverter.rest.fixerIo.event.GetSupportedCurrenciesEvent
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : AppCompatActivity() {

    lateinit var testResponseTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testResponseTV = test_tv

        val testButton: Button = testButton
        testButton.setOnClickListener {
            ApiFixerRestClient.getSupportedCurrencies()
//            ApiFixerRestClient.getLatestRates("NGN")
        }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun showResponse(currencies: GetSupportedCurrenciesEvent) {
        testResponseTV.text = currencies.getResponse()?.currencyList.toString()
    }

    @Subscribe
    fun showResponse(latestRatesEvent: GetLatestRatesEvent) {
        testResponseTV.text = latestRatesEvent.getResponse()?.toString()
    }

}
