package au.id.micolous.metrodroid.transit.torino

import au.id.micolous.metrodroid.card.CardType
import au.id.micolous.metrodroid.card.calypso.CalypsoApplication
import au.id.micolous.metrodroid.card.calypso.CalypsoCardTransitFactory
import au.id.micolous.metrodroid.multi.Localizer
import au.id.micolous.metrodroid.multi.Parcelize
import au.id.micolous.metrodroid.multi.R
import au.id.micolous.metrodroid.time.Epoch
import au.id.micolous.metrodroid.time.EpochUTC
import au.id.micolous.metrodroid.time.MetroTimeZone
import au.id.micolous.metrodroid.time.TimestampFull
import au.id.micolous.metrodroid.transit.*
import au.id.micolous.metrodroid.ui.ListItem
import au.id.micolous.metrodroid.ui.ListItemInterface
import au.id.micolous.metrodroid.util.ImmutableByteArray
import au.id.micolous.metrodroid.util.Preferences.hideCardNumbers
import au.id.micolous.metrodroid.util.Preferences.obfuscateTripDates
import au.id.micolous.metrodroid.util.hexString
import au.id.micolous.metrodroid.util.sum
import kotlin.collections.plus

@Parcelize
data class TorinoTransitData(
        private val mA: ImmutableByteArray,
        private val mIssueDate: Int,
        private val mCodiceFiscale: String,
        private val mCircuit: Int,
        override val trips: List<TransactionTripAbstract>?,
        override val subscriptions: List<TorinoSubscription>?,
        override val serialNumber: String?) : TransitData() {

    override val info: List<ListItemInterface>
        get() {
            val res = mutableListOf<ListItem>()
            res.add(ListItem("Circuit",
                    circuitoMap[mCircuit] ?: Localizer.localizeString(R.string.unknown_format, mCircuit)))
            if (!hideCardNumbers && !obfuscateTripDates) {
                res.add(ListItem(R.string.codice_fiscale, mCodiceFiscale))
            }
            val it = parseDateTime(mIssueDate)
            if (it != null) {
                res.add(ListItem(R.string.issue_date, it.format()))
            }
            return res
        }

    override fun getRawFields(level: RawLevel): List<ListItemInterface> {
        return if (level === RawLevel.ALL) {
            listOf(ListItem("Circuit", mCircuit.hexString), ListItem(R.string.issue_date, mIssueDate.hexString))
        } else {
            emptyList()
        } + listOf(ListItem("A", mA.toHexString()))
    }

    override val cardName: String
        get() = "BIP"

    companion object {
        private val circuitoMap = mapOf(0xc0 to "BIP", 0xc1 to "Pyou", 0xc2 to "E.DI.S.U",
                0xc3 to "NFC", 0xc4 to "TRENITALIA", 0xc5 to "CB")
        private val epoch: EpochUTC = Epoch.utc(2005, MetroTimeZone.ROME, -60)

        /* compiled from: TorinoTransitData.kt */
        fun parseDateTime(value: Int): TimestampFull? =
                if (value == 0) {
                    null
                } else {
                    epoch.mins(value)
                }

        val FACTORY = object : CalypsoCardTransitFactory {
            override val allCards: List<CardInfo>
                get() = listOf(CARD_INFO)

            override fun parseTransitIdentity(card: CalypsoApplication): TransitIdentity =
                    TransitIdentity("BIP", getSerial(card))

            override fun check(tenv: ImmutableByteArray): Boolean {
                return try {
                    tenv.getBitsFromBuffer(0, 32) == 83952385
                } catch (e: Exception) {
                    false
                }
            }

            override fun getCardInfo(tenv: ImmutableByteArray): CardInfo = CARD_INFO

            override fun parseTransitData(card: CalypsoApplication): TransitData {
                val tenv: ImmutableByteArray =
                        card.getFile(CalypsoApplication.File.TICKETING_ENVIRONMENT, false)?.recordList?.sum()
                                ?: ImmutableByteArray.empty()
                val tLogListParsed = card.getFile(CalypsoApplication.File.TICKETING_LOG, false)?.recordList
                        ?.filter { !it.isAllZero() }?.map { TorinoTransaction.parse(it) } ?: emptyList()
                val specialEventListParsed = card.getFile(CalypsoApplication.File.TICKETING_SPECIAL_EVENTS, false)
                        ?.recordList?.filter { !it.isAllZero() }?.map { TorinoSpecialEvent.parse(it) } ?: emptyList()
                val trips = TransactionTrip.merge(tLogListParsed + specialEventListParsed)
                val parsedSubs = listOf(CalypsoApplication.File.TICKETING_CONTRACTS_1,
                        CalypsoApplication.File.TICKETING_CONTRACTS_2).mapNotNull { card.getFile(it, false)?.recordList }
                        .flatten().mapNotNull { TorinoSubscription.parse(it) }
                return TorinoTransitData(tenv.sliceOffLen(0, 9),
                        tenv.byteArrayToInt(9, 3), tenv.sliceOffLen(12, 16).readLatin1(),
                        tenv.byteArrayToInt(28, 1), trips, parsedSubs, getSerial(card))
            }
        }

        fun getSerial(card: CalypsoApplication): String? =
                card.getFile(CalypsoApplication.File.ICC, false)
                        ?.getRecord(1)?.byteArrayToInt(16, 4)?.toString()

        val CARD_INFO = CardInfo(name = "BIP", cardType = CardType.ISO7816,
                region = TransitRegion.ITALY, keysRequired = false, preview = true,
                imageAlphaId = R.drawable.iso7810_id1_alpha)
    }
}