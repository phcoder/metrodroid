package au.id.micolous.metrodroid.transit.torino

import au.id.micolous.metrodroid.multi.FormattedString
import au.id.micolous.metrodroid.multi.Parcelize
import au.id.micolous.metrodroid.time.Timestamp
import au.id.micolous.metrodroid.transit.Subscription
import au.id.micolous.metrodroid.transit.TransitData.RawLevel
import au.id.micolous.metrodroid.ui.ListItem
import au.id.micolous.metrodroid.ui.ListItemInterface
import au.id.micolous.metrodroid.util.ImmutableByteArray
import au.id.micolous.metrodroid.util.hexString
import kotlin.collections.plus

@Parcelize
data class TorinoSubscription(override val id: Int,
                         private val contractProvider: Int,
                         private val contractTariff: Int,
                         private val mStart: Int,
                         private val mEnd: Int,
                         private val mFormat: Int,
                         private val mB: ImmutableByteArray,
                         private val mC: ImmutableByteArray) : Subscription() {

    override fun getRawFields(level: RawLevel): List<ListItemInterface> = if (level === RawLevel.ALL) {
            listOf(ListItem("Format", mFormat.hexString), ListItem("Start", mStart.hexString),
                    ListItem("End", mEnd.hexString),
                    ListItem("Provider", contractProvider.hexString),
                    ListItem("Tariff", contractTariff.hexString),
                    ListItem("ID", id.hexString))
        } else {
            emptyList()
        } + listOf(ListItem("B", mB.toHexString()), ListItem("C", mC.toHexString()))

    override val subscriptionName: String?
        get() = TorinoLookup.getSubscriptionName(contractProvider, contractTariff)

    override fun getAgencyName(isShort: Boolean): FormattedString? =
            TorinoLookup.getAgencyName(contractProvider, isShort)

    override val validFrom: Timestamp?
        get() = TorinoTransitData.parseDateTime(mStart)
    override val validTo: Timestamp?
        get() = TorinoTransitData.parseDateTime(mEnd)

    companion object {
        fun parse(data: ImmutableByteArray): TorinoSubscription? {
            if (data.isAllZero() || data.isAllFF()) {
                return null
            }

            return TorinoSubscription(
                        contractProvider = data.byteArrayToInt(0, 1),
                        mFormat = data.byteArrayToInt(1, 1), mB = data.sliceOffLen(2, 2),
                        contractTariff = data.byteArrayToInt(4, 2),
                        id = data.byteArrayToInt(6, 3),
                        mStart = data.byteArrayToInt(9, 3), mEnd = data.byteArrayToInt(12, 3),
                        mC = data.sliceOffLen(15, 14))
        }
    }
}