package com.jeremiahVaris.currencyconverter

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.jeremiahVaris.currencyconverter.di.RatesVisualizationFragment
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

    private var errorAlertDialog: AlertDialog? = null

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

        supportFragmentManager.beginTransaction().add(
            R.id.rates_graph_container,
            RatesVisualizationFragment()
        ).commit()


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
                currencies.currencyList?.keys?.forEach {
                    this.add(CurrencyFlagPair(it))
                }
            }
            setSpinnerAdapters(mCurrencyFlagPairList)
        })

        viewModel.firstEtValue.observe(this, Observer { value ->
            removeTextWatchers()
            from_currency_ET.setText(value)
            setTextWatchers()
        })

        viewModel.secondEtValue.observe(this, Observer { value ->
            removeTextWatchers()
            to_currency_ET.setText(value)
            setTextWatchers()
        })

        viewModel.secondEtHint.observe(this, Observer { value ->
            to_currency_ET.hint = value
            updateConversionRate(value)
        })

        viewModel.isRefreshing.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = it
        })

        viewModel.minorNetworkError.observe(this, Observer {
            showSnackBar("Please check your internet connection to get the latest rates", true)
        })
        viewModel.majorNetworkError.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = false
            showUnableToGetRatesDialog()
        })

        viewModel.dateOfRatesInUse.observe(this, Observer {
            it?.let { lastUpdatedTv.text = "Last Updated: $it" }

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
            viewModel.refresh()
        }

        if (!mySnackBar.isShown) mySnackBar.show()
    }

    private fun updateConversionRate(convertedAmount: String) {
        viewModel.apply {
            if (!firstCurrencyFullName.isBlank() && !secondCurrencyFullName.isBlank()) {
                firstCurrencyRateTv.text = "1 $firstCurrencyFullName equals"
                secondCurrencyRateTv.text = convertedAmount + " " + secondCurrencyFullName
            } else {
                firstCurrencyRateTv.visibility = GONE
                secondCurrencyRateTv.visibility = GONE
            }
        }

    }

    private fun setListeners() {
        from_currency_spinner.onItemSelectedListener = this
        to_currency_spinner.onItemSelectedListener = this
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
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

    private fun showUnableToGetRatesDialog(message: String? = null) {
        val builder = AlertDialog.Builder(this)
//        builder.setMessage(response.data)
        builder.setMessage(
            message
                ?: "We were unable to get the latest exchange rates. Please check your internet connection."
        )
        builder.setTitle("Oops")
        builder.setCancelable(false)

        builder.setPositiveButton("Try Again") { p0, p1 ->
            viewModel.refresh()
        }

        if (errorAlertDialog == null) errorAlertDialog = builder.create()
        if (!errorAlertDialog!!.isShowing) errorAlertDialog!!.show()
    }
}


