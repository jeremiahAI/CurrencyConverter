package com.jeremiahVaris.currencyconverter

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jeremiahVaris.currencyconverter.viewmodel.ConverterViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var testResponseTV: TextView
    lateinit var viewModel: ConverterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testResponseTV = test_tv

        viewModel = ViewModelProviders.of(this).get(ConverterViewModel::class.java)

        viewModel.rates.observe(this, Observer {
            testResponseTV.text = it.toString()
        })
        val testButton: Button = testButton
        testButton.setOnClickListener {
            //            repository.getSupportedCurrencies()
            viewModel.getLatestRates()
        }
    }


}


