package com.service.discountcalculator

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class CurrencyFormat {

    private val currencyLocaleMap: Map<String, Locale> = mapOf(
        "INR" to Locale("en", "IN"),
        "USD" to Locale("en", "US"),
        "GBP" to Locale("en", "GB"),
        "RUB" to Locale("ru", "RU"),
        "EUR" to Locale("fr", "FR"),
        "JPY" to Locale("ja", "JP")
    )

    fun formatAccordingToCurrency(
        number: BigDecimal,
        selectedCurrencyCode: String,
        showCurrencySymbol: Boolean,
        showCommaSeparator: Boolean
    ): String {

        val locale = currencyLocaleMap[selectedCurrencyCode] ?: Locale.getDefault()

        return when {
            showCurrencySymbol && showCommaSeparator -> formatNumberWithCurrency(number, locale)
            showCurrencySymbol -> formatNumberWithCurrency(number, locale).replace(",", "")
            showCommaSeparator -> formatNumber(number, locale)
            else -> number.toString()
        }
    }

    private fun formatNumberWithCurrency(number: BigDecimal, locale: Locale): String {
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)
        return currencyFormat.format(number)
    }

    private fun formatNumber(number: BigDecimal, locale: Locale): String {
        val numberFormat = NumberFormat.getNumberInstance(locale)
        return numberFormat.format(number)
    }
}
