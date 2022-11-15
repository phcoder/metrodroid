package au.id.micolous.metrodroid.transit.pulapromet

import au.id.micolous.metrodroid.card.CardType
import au.id.micolous.metrodroid.card.classic.ClassicCard
import au.id.micolous.metrodroid.card.classic.ClassicCardTransitFactory
import au.id.micolous.metrodroid.card.classic.ClassicSector
import au.id.micolous.metrodroid.multi.Parcelize
import au.id.micolous.metrodroid.multi.R
import au.id.micolous.metrodroid.time.*
import au.id.micolous.metrodroid.transit.*
import au.id.micolous.metrodroid.util.HashUtils
import au.id.micolous.metrodroid.util.ImmutableByteArray

@Parcelize
class PulaPrometTransitData(private val mSerial: ImmutableByteArray,
                            private val mBalance: Int,
                            private val mLastTrip: PulaTrip?) : TransitData() {
    @Parcelize
    class PulaTrip(private val mPrice: Int, override val startTimestamp: TimestampFull) : Trip() {
        override val fare
            get() = TransitCurrency.HRK(mPrice)
        override val mode: Mode
            get() = Mode.BUS
    }

    override val serialNumber: String
        get() = mSerial.toHexString().uppercase()
    override val cardName: String
        get() = NAME
    override val balance get() = TransitCurrency.HRK(mBalance)
    override val trips get() = listOfNotNull(mLastTrip)

    companion object {
        private fun parseTimestamp(data: ImmutableByteArray, off: Int): TimestampFull? {
            val y: Int = data.getBitsFromBuffer(off, 4)
            val doy: Int = data.getBitsFromBuffer(off + 4, 9)
            val hm: Int = data.getBitsFromBuffer(off + 13, 11)
            if (y == 0) {
                return null
            }
            return Daystamp(yearToDays(y + 2011) + doy)
                    .promote(tz = TZ, hour = hm / 60, min = hm % 60, second = 0)
        }

        private fun parse(card: ClassicCard): PulaPrometTransitData {
            val balanceSector: ClassicSector = card.getSector(2)
            val curBlock: ImmutableByteArray = balanceSector.getBlock(0).data
            val curBalance: Int = curBlock.getBitsFromBuffer(7, 20)
            val tripTime: TimestampFull? = parseTimestamp(curBlock, 96)
            val oldBalance: Int = balanceSector.getBlock(2).data.getBitsFromBuffer(7, 20)
            val trip = tripTime?.let { PulaTrip(oldBalance - curBalance, it) }
            return PulaPrometTransitData(mSerial = card.tagId, mBalance = curBalance, mLastTrip = trip)
        }

        private const val NAME = "PulaPromet"
        private val TZ = MetroTimeZone.ZAGREB

        private val CARD_INFO = CardInfo(
                name = NAME, cardType = CardType.MifareClassic, region = TransitRegion.CROATIA,
                locationId = R.string.location_pula, keysRequired = true, preview = true)

        val FACTORY = object : ClassicCardTransitFactory {
            override val earlySectors get() = 1
            override val allCards get() = listOf(CARD_INFO)

            override fun earlyCheck(sectors: List<ClassicSector>): Boolean =
                    HashUtils.checkKeyHash(sectors[0], "pula",
                        "65fe8499ff02d990b8b284ea0e8c56b5", "ca51e67307d06ff529d2b755c71e51c6") >= 0

            override fun parseTransitIdentity(card: ClassicCard) =
                    TransitIdentity("PulaPromet", card.tagId.toHexString().uppercase())

            override fun parseTransitData(card: ClassicCard): PulaPrometTransitData = parse(card)
        }
    }
}