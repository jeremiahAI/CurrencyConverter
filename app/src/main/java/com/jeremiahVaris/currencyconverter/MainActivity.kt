package com.jeremiahVaris.currencyconverter

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jeremiahVaris.currencyconverter.viewmodel.ConverterViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var userDefaultFromCurrencyFlagPair = CurrencyFlagPair("NGN")
    private var userDefaultToCurrencyFlagPair = CurrencyFlagPair("USD")

    private var mCurrencyFlagPairList: ArrayList<CurrencyFlagPair> =
        ArrayList<CurrencyFlagPair>().apply {
            this.add(userDefaultFromCurrencyFlagPair)
            this.add(userDefaultToCurrencyFlagPair)
        }

    private var realtimeConversionIsEnabled: Boolean = false
    private lateinit var firstEtTextWatcher: CurrencyConversionTextWatcher
    private lateinit var secondEtTextWatcher: CurrencyConversionTextWatcher
    private lateinit var viewModel: ConverterViewModel
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ConverterViewModel::class.java)

        setContentView(R.layout.activity_main)

        initViews()
        initList()
        initTextWatchers()
        setTextWatchers()

        setListeners()
        setSpinnerAdapters(mCurrencyFlagPairList)



        setViewModelObservers()



    }

    private fun initTextWatchers() {
        firstEtTextWatcher = CurrencyConversionTextWatcher(from_currency_ET)
        secondEtTextWatcher = CurrencyConversionTextWatcher(to_currency_ET)
    }

    private fun setTextWatchers() {
        from_currency_ET.addTextChangedListener(firstEtTextWatcher)
        to_currency_ET.addTextChangedListener(secondEtTextWatcher)
    }

    private fun removeTextWatchers() {
        from_currency_ET.removeTextChangedListener(
            firstEtTextWatcher
        )
        to_currency_ET.removeTextChangedListener(secondEtTextWatcher)
    }

    private fun setViewModelObservers() {
        viewModel.currencyList.observe(this, Observer { currencies ->
            mCurrencyFlagPairList = ArrayList<CurrencyFlagPair>().apply {
                for (currency in currencies.currencyList.keys) this.add(CurrencyFlagPair(currency))
            }
            setSpinnerAdapters(mCurrencyFlagPairList)
        })

        viewModel.firstEtValue.observe(this, Observer { value ->
            removeTextWatchers()
            from_currency_ET.setText(formatAmount(value))
            setTextWatchers()
        })

        viewModel.secondEtValue.observe(this, Observer { value ->
            removeTextWatchers()
            to_currency_ET.setText(formatAmount(value))
            setTextWatchers()
        })

        viewModel.secondEtHint.observe(this, Observer { value ->
            to_currency_ET.hint = formatAmount(value)
        })

    }

    private fun formatAmount(value: Double): String {
//        if (value>1)
        return String.format("%.2f", value)
    }

    private fun setListeners() {
        from_currency_spinner.onItemSelectedListener = this
        to_currency_spinner.onItemSelectedListener = this
    }

    private fun initViews() {
        viewModel.firstEtID = from_currency_ET.id
        viewModel.secondEtID = to_currency_ET.id
    }

    private fun setSpinnerAdapters(mCurrencyFlagPairList: java.util.ArrayList<CurrencyFlagPair>?) {
        val mAdapter = CurrencyAdapter(this, currencyFlagPairList = mCurrencyFlagPairList!!)

        from_currency_spinner.adapter = mAdapter
        to_currency_spinner.adapter = mAdapter

        mCurrencyFlagPairList.forEachIndexed { index, currencyFlagPair ->
            if (currencyFlagPair.currencyName == userDefaultFromCurrencyFlagPair.currencyName)
                from_currency_spinner.setSelection(index)
            if (currencyFlagPair.currencyName == userDefaultToCurrencyFlagPair.currencyName)
                to_currency_spinner.setSelection(index)
        }
    }

    private fun initList() {
        mCurrencyFlagPairList = ArrayList()
        mCurrencyFlagPairList.add(userDefaultFromCurrencyFlagPair)
        mCurrencyFlagPairList.add(userDefaultToCurrencyFlagPair)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        view?.let {

            val selectedCurrency = (parent.getItemAtPosition(pos) as CurrencyFlagPair).currencyName

            if (parent.id == R.id.from_currency_spinner) {
                viewModel.setFirstCurrency(selectedCurrency)
                first_currency?.text = selectedCurrency
                viewModel.convertFirstAmount()
            } else if (parent.id == R.id.to_currency_spinner) {
                viewModel.setSecondCurrency(selectedCurrency)
                second_currency?.text = selectedCurrency
                viewModel.convertSecondAmount()
            }

            viewModel.convertHint()
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


