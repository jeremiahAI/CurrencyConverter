package com.jeremiahVaris.currencyconverter


import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.jeremiahVaris.currencyconverter.viewmodel.ConverterViewModel
import java.text.DecimalFormat


///**A [TextWatcher] for Currency formatting.
// * @param editText: The editTextField the watcher is to be applied to
// */
//class CurrencyTextWatcher(val editText: EditText) : TextWatcher {
//
//    var dec = DecimalFormat("0.00")
//    override fun afterTextChanged(p0: Editable?) {
//    }
//
//    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//    }
//
//    private var current = ""
//
//    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//        if (s.toString() != current) {
//            editText.removeTextChangedListener(this)
//
//            lateinit var formatted: String
//
//            if (s.isEmpty()) {
//                formatted = "0.00"
//            } else {
//                //Remove currency, decimal point and any spaces
//                val replaceable =
//                    String.format("[%s,.\\s]", NumberFormat.getCurrencyInstance().currency.symbol)
//                val cleanString = s.toString().replace(replaceable.toRegex(), "")
//
//                // Parse to decimal and shift decimal places
//                val parsed = BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR)
//                    .divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
//                formatted = "" + parsed
//            }
//
//            // Add commas
//            formatted = addCommas(formatted)
//
//            current = formatted
//            editText.setText(formatted)
//            editText.setSelection(formatted.length) //Place cursor at the end
//
//            editText.addTextChangedListener(this)
//        }
//    }
//
//    /** A function to add commas to a [string] in decimal (currency) format*/
//    fun addCommas(string: String): String {
//
//        var formatted = string.reversed()  //reverse string direction
//        val numberGroups: MutableList<String> = mutableListOf()
//        var decimalPart = formatted.substringBefore(".", "")
//        if (decimalPart.isNotEmpty()) decimalPart += formatted[decimalPart.length] // add decimal point
//
//        numberGroups.add(decimalPart) // Take decimal part
//
//        // Group Integer part in threes and affix commas
//        for (x in numberGroups[0].length..formatted.length step 3) {
//            if ((formatted.length - x) > 3) numberGroups.add(formatted.substring(x, x + 3) + ",")
//            else numberGroups.add(formatted.substring(x))
//        }
//
//        //combine all groups
//        formatted = ""
//        for (numberGroup in numberGroups) {
//            formatted += numberGroup
//        }
//
//        formatted = formatted.reversed()
//        return formatted
//    }
//}


/**A [TextWatcher] for Currency formatting.
 * @param editText: The editTextField the watcher is to be applied to
 */
class CurrencyConversionTextWatcher(val editText: EditText) :
    TextWatcher {

    val viewModel = ViewModelProviders.of(editText.context as FragmentActivity)
        .get(ConverterViewModel::class.java)

    var dec = DecimalFormat("0.00")

    override fun afterTextChanged(p0: Editable?) {
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    private var current = ""

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.toString() != current) {
            editText.removeTextChangedListener(this)

            lateinit var formatted: String

            if (s.isEmpty()) {
                formatted = ""
            } else {
//                //Remove currency, decimal point and any spaces
//                val replaceable =
//                    String.format("[%s,.\\s]", NumberFormat.getCurrencyInstance().currency.symbol)
//                val cleanString = s.toString().replace(replaceable.toRegex(), "")

//                // Parse to decimal and shift decimal places
//                val parsed = BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR)
//                    .divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
//                formatted = "" + parsed

                //Remove commas
                formatted = s.toString().replace(",", "")
            }

            // Set viewModel amount value
            if (editText.id == viewModel.firstEtID) {
                if (!formatted.isBlank()) viewModel.setFirstEtAmountAndConvert(formatted.toDouble())
                else viewModel.setFirstEtAmountAndConvert(0.0)

            } else if (editText.id == viewModel.secondEtID) {
                if (!formatted.isBlank()) viewModel.setSecondEtAmountAndConvert(formatted.toDouble())
                else viewModel.setSecondEtAmountAndConvert(0.0)

            }

            // Add commas
//            formatted = addCommas(formatted)



            current = formatted
            editText.setText(formatted)
            editText.setSelection(start + count + formatted.length - s.length)

            editText.addTextChangedListener(this)
        }
    }


}

/** A function to add commas to a [string] in decimal (currency) format*/
fun addCommas(string: String): String {

    var formatted = string.reversed()  //reverse string direction
    val numberGroups: MutableList<String> = mutableListOf()
    var decimalPart = formatted.substringBefore(".", "")

    // add decimal point
    if (decimalPart.isNotEmpty()) decimalPart += formatted[decimalPart.length]
    else if (formatted.contains(".")) decimalPart += "."
    numberGroups.add(decimalPart) // Take decimal part

    // Group Integer part in threes and affix commas
    for (x in numberGroups[0].length..formatted.length step 3) {
        if ((formatted.length - x) > 3) numberGroups.add(
            formatted.substring(
                x,
                x + 3
            ) + ","
        )
        else numberGroups.add(formatted.substring(x))
    }

    //combine all groups
    formatted = ""
    for (numberGroup in numberGroups) {
        formatted += numberGroup
    }

    formatted = formatted.reversed()
    return formatted
}

