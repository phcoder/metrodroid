package au.id.micolous.metrodroid.transit.torino

import au.id.micolous.metrodroid.multi.FormattedString
import au.id.micolous.metrodroid.multi.Localizer.localizeString
import au.id.micolous.metrodroid.multi.R
import au.id.micolous.metrodroid.transit.Station
import au.id.micolous.metrodroid.util.StationTableReader.Companion.getLineName
import au.id.micolous.metrodroid.util.StationTableReader.Companion.getOperatorName
import au.id.micolous.metrodroid.util.StationTableReader.Companion.getStation

object TorinoLookup {
    fun getAgencyName(agency: Int, isShort: Boolean): FormattedString? =
        if (agency == 0) {
            null
        } else {
            getOperatorName(STR, agency, isShort)
        }

    fun getSubscriptionName(agency: Int?, contractTariff: Int?): String? {
        if (contractTariff == null) {
            return null
        }
        return subscriptionMapByAgency[Pair(agency , contractTariff)]?.let{ localizeString(it) }
                ?: localizeString(R.string.unknown_format, contractTariff.toString())
    }

    fun getStation(agency: Int, station: Int): Station =
            getStation(STR, agency shl 24 or station, "$agency/$station")

    fun getRouteName(agency: Int, id: Int): FormattedString =
            getLineName(STR, agency shl 24 or id, "$agency/$id")

    private val subscriptionMapByAgency: Map<Pair<Int?, Int>, Int> = emptyMap()

    private const val STR = "torino"
}