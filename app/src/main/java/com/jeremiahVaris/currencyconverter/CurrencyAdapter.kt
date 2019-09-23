package com.jeremiahVaris.currencyconverter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide


class CurrencyAdapter(
    context: Context,
    currencyFlagPairList: ArrayList<CurrencyFlagPair>,
    resourceId: Int = 0
) : ArrayAdapter<CurrencyFlagPair>(context, resourceId, currencyFlagPairList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        return super.getView(position, convertView, parent)
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initDropDownView(position, convertView, parent)
    }


    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(
                R.layout.currency_spinner, parent, false
            )
        }

        val flagImageView: ImageView = view!!.findViewById(R.id.image_view_flag)
        val textViewName: TextView = view.findViewById(R.id.text_view_name)

        val currentItem = getItem(position)



        if (currentItem != null) {
            Glide.with(flagImageView.context)
                .load(currentItem.flagImage)
//                .apply(RequestOptions.circleCropTransform())
                .into(flagImageView)

            textViewName.text = currentItem.currencyName
        }

        return view
    }

    private fun initDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(
                R.layout.currency_spinner_row, parent, false
            )
        }

        val flagImageView: ImageView = view!!.findViewById(R.id.image_view_flag)
        val textViewName: TextView = view.findViewById(R.id.text_view_name)

        val currentItem = getItem(position)



        if (currentItem != null) {
            Glide.with(flagImageView.context)
                .load(currentItem.flagImage)
//                .apply(RequestOptions.circleCropTransform())
                .into(flagImageView)

            textViewName.text = currentItem.currencyName
        }

        return view
    }

}


