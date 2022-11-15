package au.id.micolous.metrodroid.transit.torino

import au.id.micolous.metrodroid.multi.FormattedString
import au.id.micolous.metrodroid.multi.Parcelize
import au.id.micolous.metrodroid.time.TimestampFull
import au.id.micolous.metrodroid.transit.*
import au.id.micolous.metrodroid.transit.TransitCurrency.Companion.EUR
import au.id.micolous.metrodroid.transit.TransitData.RawLevel
import au.id.micolous.metrodroid.util.ImmutableByteArray
import au.id.micolous.metrodroid.util.hexString

@Parcelize
data class TorinoTransaction(private val mFormat: Int, private val tripProvider: Int,
                        private val mA: ImmutableByteArray,
                        private val mOperationType: Int,
                        private val mB: Int,
                        private val mStamp: Int,
                        private val mC: ImmutableByteArray,
                        private val firstStamp: Int,
                        private val mD: ImmutableByteArray,
                        private val mAuthenticator: Int, private val mStation: Int,
                        private val mLine: Int, private val mFare: Int) : Transaction() {

    override val timestamp: TimestampFull?
        get() = TorinoTransitData.parseDateTime(mStamp)

    override fun getAgencyName(isShort: Boolean): FormattedString? =
            TorinoLookup.getAgencyName(tripProvider, isShort)

    override fun getRawFields(level: RawLevel) =
        if (level === RawLevel.ALL) {
            "mStamp=${mStamp.hexString},mAuthenticator=${mAuthenticator.hexString},mOperationType=${mOperationType.hexString},mFare=$mFare,"
        } else {
            ""
        } + "firstStamp=${firstStamp.hexString}/${TorinoTransitData.parseDateTime(firstStamp)?.format()},A=$mA,B=${mB.hexString},C=$mC"

    override val isTapOff: Boolean
        get() = mOperationType in listOf(1, 3)

    /* access modifiers changed from: protected */
    override val isTapOn: Boolean
        get() = mOperationType in listOf(0, 2)
    override val fare: TransitCurrency?
        get() {
            if (mOperationType in listOf(2, 3) && mFare == 0) {
                return null
            }
            return if (mOperationType in listOf(1, 3)) {
                EUR(-mFare)
            } else {
                EUR(mFare)
            }
        }

    /* access modifiers changed from: protected */
    public override fun isSameTrip(other: Transaction): Boolean =
            other is TorinoTransaction && firstStamp == other.firstStamp

    override val isTransparent: Boolean
        get() = mOperationType == 4
    override val mode: Trip.Mode
        get() = if (mOperationType == 4) {
            Trip.Mode.TICKET_MACHINE
        } else {
            Trip.Mode.OTHER
        }
    override val station: Station
        get() = TorinoLookup.getStation(tripProvider, mStation)
    override val humanReadableLineIDs: List<String>
        get() = listOf("$tripProvider/$mLine")
    override val routeNames: List<FormattedString>
        get() = listOf(TorinoLookup.getRouteName(tripProvider, mLine))

    companion object {
        fun parse(data: ImmutableByteArray): TorinoTransaction = TorinoTransaction(
                mFormat = data.byteArrayToInt(0, 1),
                tripProvider = data.byteArrayToInt(1, 1),
                mA = data.sliceOffLen(2, 3),
                mOperationType = data.getBitsFromBuffer(40, 4), mB = data.getBitsFromBuffer(44, 4),
                mStamp = data.byteArrayToInt(6, 3),
                mC = data.sliceOffLen(15, 3), firstStamp = data.byteArrayToInt(20, 3),
                mD = data.sliceOffLen(23, 4), mAuthenticator = data.byteArrayToInt(27, 2),
                mStation = data.byteArrayToInt(9, 3), mLine = data.byteArrayToInt(12, 3),
                mFare = data.byteArrayToInt(18, 2))
    }
}