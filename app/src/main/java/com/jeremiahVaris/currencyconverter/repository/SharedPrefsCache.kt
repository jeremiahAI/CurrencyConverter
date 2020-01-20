package com.jeremiahVaris.currencyconverter.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.doAsync
import javax.inject.Inject

class SharedPrefsCache @Inject constructor(val sharedPrefs: SharedPreferences, val gson: Gson) {

    private val ACCESS_KEY_KEY = "accessKey"
    private val KEY_SWITCH_STATUS = "keySwitch"
    private var editor: SharedPreferences.Editor = sharedPrefs.edit()


    private inline fun <reified T> fetch(
        key: String
    ): T? {
        val json =
            sharedPrefs.getString(
                key,
                null
            )

        return try {
            val type = object : TypeToken<T>() {}.type
            gson.fromJson<T>(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun <T> save(t: T, key: String) {
        doAsync {
            val type = object : TypeToken<T>() {}.type
            val json = Gson().toJson(t, type)


            editor.putString(key, json).apply()

        }
    }

    fun clear() {
        editor.clear().commit()
    }

    fun saveAccessKey(currentKey: String?) {
        save(currentKey, ACCESS_KEY_KEY)
    }

    fun getAccessKey(): String? {
        return fetch<String>(ACCESS_KEY_KEY)
    }

    fun getIsKeySwitchInProgress(): Boolean? {
        return fetch<Boolean>(KEY_SWITCH_STATUS)
    }


    fun saveIsKeySwitchInProgress(progressStatus: Boolean?) {
        save(progressStatus, KEY_SWITCH_STATUS)
    }


}