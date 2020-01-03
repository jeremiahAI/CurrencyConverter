package com.jeremiahVaris.currencyconverter

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.jeremiahVaris.currencyconverter.di.ViewModelFactory
import com.jeremiahVaris.currencyconverter.viewmodel.ConverterViewModel
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var userDefaultFromCurrencyFlagPair = CurrencyFlagPair("NGN")
    private var userDefaultToCurrencyFlagPair = CurrencyFlagPair("USD")

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

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
        AndroidInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ConverterViewModel::class.java)

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
            updateConversionRate(formatAmount(value))
        })

        viewModel.networkError.observe(this, Observer {
            // Todo: Handle offline mode
            showSnackBar("Please check your internet connection", true)
        })

    }

    private fun showSnackBar(message: String, static: Boolean) {
        var snackBarLength = Snackbar.LENGTH_SHORT

        if (static) snackBarLength = Snackbar.LENGTH_INDEFINITE

        val mySnackBar = Snackbar.make(
            from_currency_ET,
            message, snackBarLength
        )


        mySnackBar.setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent))

        mySnackBar.setAction("OK") {
            mySnackBar.dismiss()
            viewModel.getSupportedCurrencies()
        }

        mySnackBar.show()
    }

    private fun updateConversionRate(convertedAmount: String) {
        viewModel.firstCurrencyFullName?.let {
            firstCurrencyRateTv.text = "1 $it equals"
        }
        viewModel.secondCurrencyFullName?.let {
            secondCurrencyRateTv.text = convertedAmount + " " + it
        }

    }

    private fun formatAmount(value: Double): String {
        val decimalPart: String = when {
            value.toString().contains("E-") -> { // When value is represented in exponent form
                convertExponentToDecimalPartRepresentation(value)
            }
            else -> value.toString().substringAfter(".", "")
        }


        var nonZeroDecimalIndex = 0
        for (char in decimalPart.iterator()) {
            if (char == '0') nonZeroDecimalIndex++
            else break
        }

        val stringFormat =
            when {
                value >= 1 -> "%.2f" // If value isn't purely decimal, round to two decimal places
                nonZeroDecimalIndex == 2 -> "%." + "4" + "f"
                nonZeroDecimalIndex < 2 -> "%." + "3" + "f"
                else -> "%." + (nonZeroDecimalIndex + 2) + "f"
            }

        return String.format(stringFormat, value)
    }

    private fun convertExponentToDecimalPartRepresentation(value: Double): String {
        val exponentString = value.toString()[value.toString().length - 1]
        val exponent = Character.getNumericValue(exponentString)
        var result = ""
        repeat(exponent - 1) {
            result += "0"
        }
        return result + value.toString().replace(".", "").replace("""E-\d""".toRegex(), "")
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


