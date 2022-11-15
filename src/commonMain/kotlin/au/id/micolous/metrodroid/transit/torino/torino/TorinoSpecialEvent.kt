package au.id.micolous.metrodroid.transit.torino

import au.id.micolous.metrodroid.multi.Parcelize
import au.id.micolous.metrodroid.time.Timestamp
import au.id.micolous.metrodroid.transit.Transaction
import au.id.micolous.metrodroid.transit.TransitCurrency
import au.id.micolous.metrodroid.transit.TransitData.RawLevel
import au.id.micolous.metrodroid.util.ImmutableByteArray

@Parcelize
data class TorinoSpecialEvent(private val mA: ImmutableByteArray) : Transaction() {
    override val isTapOff: Boolean
        get() = false
    override val timestamp: Timestamp?
        get() = null
    override val fare: TransitCurrency?
        get() = null

    override val isTapOn: Boolean
        get() = false

    public override fun isSameTrip(other: Transaction): Boolean = false

    override fun getRawFields(level: RawLevel) = "mA=" + mA.toHexString()

    companion object {
        fun parse(data: ImmutableByteArray): TorinoSpecialEvent = TorinoSpecialEvent(data)
    }
}