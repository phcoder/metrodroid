@file:JvmName("CountryKtActual")
package au.id.micolous.metrodroid.util

import android.os.Build
import java.util.*

actual fun currencyNameBySymbol(symbol: String): String? =
    Currency.getInstance(symbol)?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            it.displayName
        } else {
            it.currencyCode
        }
    }

private val specialLocales = mapOf(
    "CA" to Locale.CANADA,
    "CN" to Locale.CHINA,
    "DE" to Locale.GERMANY,
    "FR" to Locale.FRANCE,
    "GB" to Locale.UK,
    "IT" to Locale.ITALY,
    "JP" to Locale.JAPAN,
    "KR" to Locale.KOREA,
    "TW" to Locale.TAIWAN,
    "UK" to Locale.UK,
    "US" to Locale.US
)

actual fun iso3166AlphaToName(isoAlpha: String): String? {
    val locale = specialLocales[isoAlpha] ?: Locale("", isoAlpha)
    return locale.displayCountry
}
