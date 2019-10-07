package com.jeremiahVaris.currencyconverter

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jeremiahVaris.currencyconverter.repository.model.Currencies
import com.jeremiahVaris.currencyconverter.viewmodel.ConverterViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var mCurrencyFlagPairList: ArrayList<CurrencyFlagPair>? = null
    private var mAdapter: CurrencyAdapter? = null
    private var userDefaultFromCurrencyFlagPair = CurrencyFlagPair("NGA")
    private var userDefaultToCurrencyFlagPair = CurrencyFlagPair("USD")

    private var realtimeConversionIsEnabled: Boolean = false
    private lateinit var testResponseTV: TextView
    private lateinit var viewModel: ConverterViewModel
    private lateinit var fromCurrencySpinner: AppCompatSpinner
    private lateinit var toCurrencySpinner: AppCompatSpinner
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var fromCurrencyET: EditText
    private lateinit var fromCurrencyTV: TextView
    private lateinit var toCurrencyTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initList()

        setSpinnerAdapters(mCurrencyFlagPairList)
        setListeners()

        viewModel = ViewModelProviders.of(this).get(ConverterViewModel::class.java)

        setViewModelObservers()

        val convertButton: Button = testButton
        convertButton.setOnClickListener {
            viewModel.setAmountToBeConverted(1.0)
            viewModel.convert()
        }


    }

    private fun setViewModelObservers() {
        viewModel.currencyList.observe(this, Observer { currencies ->
            mCurrencyFlagPairList = ArrayList<CurrencyFlagPair>().apply {
                for (currency in currencies.currencyList.keys) this.add(CurrencyFlagPair(currency))
            }
            setSpinnerAdapters(mCurrencyFlagPairList)
        })

        viewModel.convertedValue.observe(this, Observer {
            testResponseTV.text = it.toString()
        })
    }

    private fun setListeners() {
        fromCurrencySpinner.onItemSelectedListener = this
        toCurrencySpinner.onItemSelectedListener = this
    }

    private fun initViews() {

        testResponseTV = test_tv
        fromCurrencySpinner = from_currency_spinner
        toCurrencySpinner = to_currency_spinner
        fromCurrencyTV = from_currency
        toCurrencyTV = to_currency
    }

    private fun setSpinnerAdapters(mCurrencyFlagPairList: java.util.ArrayList<CurrencyFlagPair>?) {
        mAdapter = CurrencyAdapter(this, currencyFlagPairList = mCurrencyFlagPairList!!)

        fromCurrencySpinner.adapter = mAdapter
        fromCurrencySpinner.setSelection(mAdapter!!.getPosition(userDefaultFromCurrencyFlagPair))
        toCurrencySpinner.adapter = mAdapter
        toCurrencySpinner.setSelection(
            (toCurrencySpinner.adapter as CurrencyAdapter).getPosition(
                userDefaultToCurrencyFlagPair
            )
        )
    }

    private fun initList() {
        mCurrencyFlagPairList = ArrayList()
        mCurrencyFlagPairList!!.add(userDefaultFromCurrencyFlagPair)
        mCurrencyFlagPairList!!.add(userDefaultToCurrencyFlagPair)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
//         An item was selected. You can retrieve the selected item using
//         parent.getItemAtPosition(pos)
        val selectedCurrency = (parent.getItemAtPosition(pos) as CurrencyFlagPair).currencyName

        if (parent.id == R.id.from_currency_spinner) {
            viewModel.setFromCurrency(selectedCurrency)
            fromCurrencyTV.text = selectedCurrency
        } else if (parent.id == R.id.to_currency_spinner) {
            viewModel.setToCurrency(selectedCurrency)
            toCurrencyTV.text = selectedCurrency
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.enable_realtime_conversion) {
            enableRealtimeConversion(item.isChecked)

        }
        return super.onOptionsItemSelected(item)
    }

    private fun enableRealtimeConversion(checked: Boolean) {
        realtimeConversionIsEnabled = checked

    }
}

private fun Currencies.convertToStringArray(): ArrayList<String> {
    return currencyList.keys.run {
        val list = ArrayList<String>()
        for (currency in this) {
            list.add(currency)
        }
        list
    }


}


