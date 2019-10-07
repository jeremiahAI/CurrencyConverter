package com.jeremiahVaris.currencyconverter

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyConversionTextWatcherTest {


    @Test
    fun addCommas_ValuePassedWithNoDecimal_AddsCommasCorrectly() {
        val commarizedValue = CurrencyConversionTextWatcher().addCommas("12345")
        assertEquals("12,345", commarizedValue)
    }

    @Test
    fun addCommas_ValuePassedWithTenthsDecimal_AddsCommasCorrectly() {
        val commarizedValue = CurrencyConversionTextWatcher().addCommas("12345678.1")
        assertEquals("12,345,678.1", commarizedValue)
    }

    @Test
    fun addCommas_ValuePassedWithHundredthsDecimal_AddsCommasCorrectly() {
        val commarizedValue = CurrencyConversionTextWatcher().addCommas("1234567.81")
        assertEquals("1,234,567.81", commarizedValue)
    }

    @Test
    fun addCommas_ValuePassedWithThousandthsDecimal_AddsCommasCorrectly() {
        val commarizedValue = CurrencyConversionTextWatcher().addCommas("12345.001")
        assertEquals("12,345.001", commarizedValue)
    }
}