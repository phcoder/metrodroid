/*
 * Timestamp.kt
 *
 * Copyright 2019 Google
 *
 * This file is open to relicensing, if you need it under another
 * license, contact phcoder
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.id.micolous.metrodroid.time

import au.id.micolous.metrodroid.multi.FormattedString
import au.id.micolous.metrodroid.multi.Parcelable
import au.id.micolous.metrodroid.multi.Parcelize
import au.id.micolous.metrodroid.util.NumberUtils
import au.id.micolous.metrodroid.util.Preferences
import au.id.micolous.metrodroid.util.TripObfuscator
import kotlinx.datetime.*
import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.native.concurrent.SharedImmutable

@Parcelize
@Serializable(with = MetroTimeZone.Companion::class)
data class MetroTimeZone(val olson: String): Parcelable {
    override fun toString(): String = olson

    val libTimeZone: TimeZone get() = when (this) {
        UNKNOWN -> UTC.libTimeZone
        LOCAL -> TimeZone.currentSystemDefault()
        else -> TimeZone.of(olson)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Serializer(forClass = MetroTimeZone::class)
    companion object : KSerializer<MetroTimeZone> {
        override fun serialize(encoder: Encoder, value: MetroTimeZone) {
            encoder.encodeString(value.olson)
        }

        override fun deserialize(decoder: Decoder) = MetroTimeZone(decoder.decodeString())

        // Time zone not specified
        val UNKNOWN = MetroTimeZone(olson = "UNKNOWN")
        // Local device time zone
        val LOCAL = MetroTimeZone(olson = "LOCAL")
        // UTC
        val UTC = MetroTimeZone(olson = "Etc/UTC")
        val ADELAIDE = MetroTimeZone(olson = "Australia/Adelaide")
        val AMSTERDAM = MetroTimeZone(olson = "Europe/Amsterdam")
        val AUCKLAND = MetroTimeZone(olson = "Pacific/Auckland")
        val BEIJING = MetroTimeZone(olson = "Asia/Shanghai")
        val BRISBANE = MetroTimeZone(olson = "Australia/Brisbane")
        val BRUXELLES = MetroTimeZone(olson = "Europe/Brussels")
        val CHICAGO = MetroTimeZone(olson = "America/Chicago")
        val COPENHAGEN = MetroTimeZone(olson = "Europe/Copenhagen")
        val DUBAI = MetroTimeZone(olson = "Asia/Dubai")
        val DUBLIN = MetroTimeZone(olson = "Europe/Dublin")
        val HELSINKI = MetroTimeZone(olson = "Europe/Helsinki")
        val HOUSTON = MetroTimeZone(olson = "America/Chicago")
        val JAKARTA = MetroTimeZone(olson = "Asia/Jakarta")
        val JERUSALEM = MetroTimeZone(olson = "Asia/Jerusalem")
        val JOHANNESBURG = MetroTimeZone(olson ="Africa/Johannesburg")
        val KAMCHATKA = MetroTimeZone(olson = "Asia/Kamchatka")
        val KIEV = MetroTimeZone(olson = "Europe/Kiev")
        val KIROV = MetroTimeZone(olson = "Europe/Kirov")
        val KRASNOYARSK = MetroTimeZone(olson = "Asia/Krasnoyarsk")
        val KUALA_LUMPUR = MetroTimeZone(olson = "Asia/Kuala_Lumpur")
        val LISBON = MetroTimeZone(olson = "Europe/Lisbon")
        val LONDON = MetroTimeZone(olson = "Europe/London")
        val LOS_ANGELES = MetroTimeZone(olson = "America/Los_Angeles")
        val MADRID = MetroTimeZone(olson = "Europe/Madrid")
        val MONTREAL = MetroTimeZone(olson = "America/Montreal")
        val MOSCOW = MetroTimeZone(olson = "Europe/Moscow")
        val NEW_YORK = MetroTimeZone(olson = "America/New_York")
        val OSLO = MetroTimeZone(olson = "Europe/Oslo")
        val PARIS = MetroTimeZone(olson = "Europe/Paris")
        val PERTH = MetroTimeZone(olson = "Australia/Perth")
        val ROME = MetroTimeZone(olson = "Europe/Rome")
        val SAKHALIN = MetroTimeZone(olson = "Asia/Sakhalin")
        val SANTIAGO_CHILE = MetroTimeZone("America/Santiago")
        val SAO_PAULO = MetroTimeZone(olson = "America/Sao_Paulo")
        val SEOUL = MetroTimeZone(olson = "Asia/Seoul")
        val SIMFEROPOL = MetroTimeZone(olson = "Europe/Simferopol")
        val SINGAPORE = MetroTimeZone(olson = "Asia/Singapore")
        val STOCKHOLM = MetroTimeZone(olson = "Europe/Stockholm")
        val SYDNEY = MetroTimeZone(olson = "Australia/Sydney")
        val TAIPEI = MetroTimeZone(olson = "Asia/Taipei")
        val TBILISI = MetroTimeZone(olson = "Asia/Tbilisi")
        val TOKYO = MetroTimeZone(olson = "Asia/Tokyo")
        val VANCOUVER = MetroTimeZone(olson = "America/Vancouver")
        val VLADIVOSTOK = MetroTimeZone(olson = "Asia/Vladivostok")
        val YEKATERINBURG = MetroTimeZone(olson = "Asia/Yekaterinburg")
        val SAMARA = MetroTimeZone(olson = "Europe/Samara")
        val YAKUTSK = MetroTimeZone(olson = "Asia/Yakutsk")
        val OMSK = MetroTimeZone(olson = "Asia/Omsk")
        val NOVOSIBIRSK = MetroTimeZone(olson = "Asia/Novosibirsk")
        val NOVOKUZNETSK = MetroTimeZone(olson = "Asia/Novokuznetsk")
        val WARSAW = MetroTimeZone(olson = "Europe/Warsaw")
    }
}

internal const val SEC = 1000L
internal const val MIN = 60L * SEC
internal const val HOUR = 60L * MIN
internal const val DAY = 24L * HOUR

@SharedImmutable
val epochLocalDate = LocalDate(1970, kotlinx.datetime.Month.JANUARY, 1)

internal fun makeNow(): TimestampFull =
    TimestampFull(
        timeInMillis = Clock.System.now().toEpochMilliseconds(),
        tz = MetroTimeZone(TimeZone.currentSystemDefault().id))

fun getYMD(daysSinceEpoch: Int): YMD {
    return YMD(epochLocalDate + DatePeriod(0, 0, daysSinceEpoch))
}

fun yearToDays(year: Int): Int {
    val offYear = year - 1
    var days = offYear * 365
    days += offYear / 4
    days -= offYear / 100
    days += offYear / 400
    return days - 719162
}

fun epochDayHourMinToMillis(tz: MetroTimeZone, daysSinceEpoch: Int, hour: Int, min: Int): Long {
    val ld = (epochLocalDate + DatePeriod(0, 0, daysSinceEpoch))
    return ld.atTime(hour, min).toInstant(tz.libTimeZone).toEpochMilliseconds()
}

/**
 * Enum of 0-indexed months in the Gregorian calendar
 */
enum class Month(val zeroBasedIndex: Int) {
    JANUARY(0),
    FEBRUARY(1),
    MARCH(2),
    APRIL(3),
    MAY(4),
    JUNE(5),
    JULY(6),
    AUGUST(7),
    SEPTEMBER(8),
    OCTOBER(9),
    NOVEMBER(10),
    DECEMBER(11);

    val oneBasedIndex: Int get() = zeroBasedIndex + 1

    companion object {
        fun zeroBased(idx: Int): Month = values()[idx]
    }
}

/**
 * Represents a year, month and day in the Gregorian calendar.
 *
 * @property month Month, where January = Month.JANUARY.
 * @property day Day of the month, where the first day of the month = 1.
 */
data class YMD(val ld: LocalDate) {
    constructor(year: Int, month: Int, day: Int) : this(
        LocalDate(1600, kotlinx.datetime.Month.JANUARY, 1)
                + DatePeriod(year - 1600, month, day - 1))

    val day: Int get() = ld.dayOfMonth
    val month: Month get() = Month.zeroBased(ld.month.number - 1)
    val year: Int get() = ld.year
    val daysSinceEpoch: Int get() = epochLocalDate.daysUntil(ld)
}

internal fun yearToMillis(year: Int) = yearToDays(year) * DAY

interface Duration {
    fun addFull (ts: TimestampFull): TimestampFull
    companion object {
        fun daysLocal(d: Int) = DurationDaysLocal(d)
        fun mins(m: Int) = DurationSec(m * 60)
        fun yearsLocal(y: Int) = DurationYearsLocal(y)
        fun monthsLocal(m: Int) = DurationMonthsLocal(m)
    }
}

interface DayDuration : Duration {
    fun addDays(ts: Daystamp): Daystamp
    fun addAny (ts: Timestamp): Timestamp = when (ts) {
        is Daystamp -> addDays(ts)
        is TimestampFull -> addFull(ts)
    }
}

class DurationDaysLocal(private val d: Int) : DayDuration {
    override fun addFull(ts: TimestampFull) = ts + DatePeriod(0, 0, d)

    override fun addDays(ts: Daystamp) = ts + DatePeriod(0, 0, d)
}

class DurationMonthsLocal(private val m: Int) : DayDuration {
    override fun addFull(ts: TimestampFull) = ts + DatePeriod(0, m, 0)

    override fun addDays(ts: Daystamp) = ts + DatePeriod(0, m, 0)
}

class DurationYearsLocal(private val y: Int) : DayDuration {
    override fun addFull(ts: TimestampFull) = ts + DatePeriod(y, 0, 0)

    override fun addDays(ts: Daystamp) = ts + DatePeriod(y, 0, 0)
}

class DurationSec(private val s: Int) : Duration {
    override fun addFull(ts: TimestampFull) = TimestampFull(
            timeInMillis = ts.timeInMillis + s * SEC,
            tz = ts.tz)
}

interface Epoch : Parcelable {
    companion object {
        fun utc(year: Int, tz: MetroTimeZone, minOffset: Int = 0) = EpochUTC(
                baseDays = yearToDays(year),
                baseMillis = yearToMillis(year) + minOffset * MIN, outputTz = tz)
        fun local(year: Int, tz: MetroTimeZone) = EpochLocal(yearToDays(year), tz)
    }
}

// This is used when timestamps observe regular progression without any DST
@Parcelize
class EpochUTC internal constructor(private val baseMillis: Long,
                                    private val baseDays: Int,
                                    private val outputTz: MetroTimeZone) : Epoch {
    fun mins(offset: Int) =
            TimestampFull(timeInMillis = baseMillis + offset * MIN, tz = outputTz)
    fun days(offset: Int) =
            Daystamp(daysSinceEpoch = baseDays + offset)

    fun seconds(offset: Long) =
            TimestampFull(timeInMillis = baseMillis + offset * SEC, tz = outputTz)
    fun dayMinute(d: Int, m: Int) = TimestampFull(
            timeInMillis = baseMillis + d * DAY + m * MIN, tz = outputTz)
    fun daySecond(d: Int, s: Int) = TimestampFull(
            timeInMillis = baseMillis + d * DAY + s * SEC, tz = outputTz)

    fun dayHourMinuteSecond(d: Int, h: Int, m: Int, s: Int) = TimestampFull(
            timeInMillis = baseMillis + d * DAY + h * HOUR + m * MIN + s * SEC, tz = outputTz)
}

//The nasty timestamps: day is equal to calendar day
@Parcelize
class EpochLocal internal constructor(private val baseDays: Int,
                                      private val tz: MetroTimeZone) : Epoch {
    fun days(d: Int) =
            Daystamp(daysSinceEpoch = baseDays + d)

    fun dayMinute(d: Int, m: Int) = TimestampFull(
            timeInMillis = epochDayHourMinToMillis(tz, baseDays + d,
                    m / 60, m % 60), tz = tz)
    fun daySecond(d: Int, s: Int) = TimestampFull(
            timeInMillis = epochDayHourMinToMillis(tz, baseDays + d,
                    s / 3600, (s / 60) % 60) + (s%60) * SEC, tz = tz)
}

sealed class Timestamp: Parcelable {
    abstract val localDate: LocalDate
    abstract fun format(): FormattedString
    open operator fun plus(duration: DayDuration): Timestamp = duration.addAny(this)
    abstract fun toDaystamp(): Daystamp
    abstract fun obfuscateDelta(delta: Long): Timestamp
    abstract fun plus(duration: DatePeriod): Timestamp

    fun isSameDay(other: Timestamp): Boolean = this.toDaystamp() == other.toDaystamp()
    abstract fun getMonth(): Month
    abstract fun getYear(): Int
    abstract val day: Int
}

@Parcelize
@Serializable
// Only date is known
data class Daystamp internal constructor(val daysSinceEpoch: Int): Timestamp(), Comparable<Daystamp> {
    override fun getMonth(): Month = Month.zeroBased(localDate.month.number-1)

    override fun getYear(): Int = localDate.year
    override val day: Int get() = localDate.dayOfMonth

    override fun toDaystamp(): Daystamp = this

    override fun compareTo(other: Daystamp): Int = daysSinceEpoch.compareTo(other.daysSinceEpoch)

    override fun obfuscateDelta(delta: Long) = Daystamp(daysSinceEpoch = daysSinceEpoch + ((delta + DAY/2) / DAY).toInt())

    override fun format(): FormattedString =
                TimestampFormatter.longDateFormat(this)

    val dayOfYear: Int get() = localDate.dayOfYear
    override val localDate by lazy {
        epochLocalDate + DatePeriod(0, 0, daysSinceEpoch)
    }

    override operator fun plus(duration: DatePeriod) = Daystamp(
        localDate + duration)

    fun adjust() : Daystamp = this
    fun promote(tz: MetroTimeZone, hour: Int, min: Int): TimestampFull = TimestampFull(
            tz = tz, timeInMillis = epochDayHourMinToMillis(tz, daysSinceEpoch, hour, min))

    /**
     * Formats a GregorianCalendar in to ISO8601 date format in local time (ie: without any timezone
     * conversion).  This is designed for [Daystamp] values which only have a valid date
     * component.
     *
     * This should only be used for debugging logs, in order to ensure consistent
     * information.
     *
     * @receiver Date to format
     * @return String representing the date in ISO8601 format.
     */
    private fun isoDateFormat(): String {
        // ISO_DATE_FORMAT = SimpleDateFormat ("yyyy-MM-dd", Locale.US)
        return NumberUtils.zeroPad(localDate.year, 4) + "-" +
                NumberUtils.zeroPad(localDate.month.number, 2) + "-" +
                NumberUtils.zeroPad(localDate.dayOfMonth, 2)
    }

    override fun toString(): String = isoDateFormat()

    /**
     * Represents a year, month and day in the Gregorian calendar.
     *
     * @param month Month, where January = 0.
     * @param day Day of the month, where the first day of the month = 1.
     */
    constructor(year: Int, month: Int, day: Int) : this(YMD(year, month, day))

    constructor(year: Int, month: Month, day: Int) : this(YMD(year, month.zeroBasedIndex, day))

    constructor(ymd: YMD) : this(
        daysSinceEpoch = ymd.daysSinceEpoch
    )

    constructor(localDate: LocalDate) : this(
        daysSinceEpoch = epochLocalDate.daysUntil(localDate)
    )

    companion object {
        fun fromDayOfYear(year: Int, dayOfYear: Int) =
            Daystamp(LocalDate(year, kotlinx.datetime.Month.JANUARY, 1) + DatePeriod(0, 0, dayOfYear))
    }
}

@Parcelize
@Serializable
// Precision or minutes and higher
data class TimestampFull internal constructor(val timeInMillis: Long,
                                            val tz: MetroTimeZone): Parcelable, Comparable<TimestampFull>, Timestamp() {
    override fun getMonth(): Month = toDaystamp().getMonth()
    override fun getYear(): Int = toDaystamp().getYear()
    override val day: Int get() = ldt.dayOfMonth

    override fun toDaystamp() = Daystamp(ldt.date)

    val hour: Int get() = ldt.hour
    val minute: Int get() = ldt.minute
    val ldt by lazy {
        Instant.fromEpochMilliseconds(timeInMillis).toLocalDateTime(tz.libTimeZone)
    }
    val ldtUtc by lazy {
        Instant.fromEpochMilliseconds(timeInMillis).toLocalDateTime(MetroTimeZone.UTC.libTimeZone)
    }
    override val localDate by lazy {
        ldt.date
    }

    override fun compareTo(other: TimestampFull): Int = timeInMillis.compareTo(other = other.timeInMillis)
    fun adjust() : TimestampFull =
            TripObfuscator.maybeObfuscateTS(if (Preferences.convertTimezone)
                TimestampFull(timeInMillis, MetroTimeZone.LOCAL) else this)

    operator fun plus(duration: Duration) = duration.addFull(this)
    override operator fun plus(duration: DayDuration) = duration.addFull(this)
    override operator fun plus(duration: DatePeriod) = TimestampFull(
        (ldt.date + duration).atTime(ldt.hour, ldt.minute,
            ldt.second, ldt.nanosecond).toInstant(tz.libTimeZone).toEpochMilliseconds(),
        tz)

    override fun format(): FormattedString = TimestampFormatter.dateTimeFormat(this)

    constructor(tz : MetroTimeZone, year: Int, month: Int, day: Int, hour: Int,
                min: Int, sec: Int = 0) : this(
            timeInMillis = epochDayHourMinToMillis(
                    tz, YMD(year, month, day).daysSinceEpoch, hour, min) + sec * SEC,
            tz = tz
    )

    constructor(tz : MetroTimeZone, year: Int, month: Month, day: Int, hour: Int,
                min: Int, sec: Int = 0) : this(
        year = year, month = month.zeroBasedIndex, day = day,
        hour = hour, min = min, sec = sec,
        tz = tz
    )

    /**
     * Formats a GregorianCalendar in to ISO8601 date and time format in UTC. This should only be
     * used for debugging logs, in order to ensure consistent information.
     *
     * @receiver Date/time to format
     * @return String representing the date and time in ISO8601 format.
     */
    fun isoDateTimeFormat(): String {
        //  SimpleDateFormat ("yyyy-MM-dd HH:mm", Locale.US)
        return NumberUtils.zeroPad(ldtUtc.year, 4) + "-" +
                NumberUtils.zeroPad(ldtUtc.month.number, 2) + "-" +
                NumberUtils.zeroPad(ldtUtc.dayOfMonth, 2) + " " +
                NumberUtils.zeroPad(ldtUtc.hour, 2) + ":" +
                NumberUtils.zeroPad(ldtUtc.minute, 2)
    }

    /**
     * Formats a GregorianCalendar in to ISO8601 date and time format in UTC, but with only
     * characters that can be used in filenames on most filesystems.
     *
     * @receiver Date/time to format
     * @return String representing the date and time in ISO8601 format.
     */
    fun isoDateTimeFilenameFormat(): String {
        //  SimpleDateFormat ("yyyyMMdd-HHmmss", Locale.US)
        return NumberUtils.zeroPad(ldtUtc.year, 4) +
                NumberUtils.zeroPad(ldtUtc.month.number, 2) +
                NumberUtils.zeroPad(ldtUtc.dayOfMonth, 2) + "-" +
                NumberUtils.zeroPad(ldtUtc.hour, 2) +
                NumberUtils.zeroPad(ldtUtc.minute, 2) +
                NumberUtils.zeroPad(ldtUtc.second, 2)
    }

    override fun toString(): String = isoDateTimeFormat() + "/$tz"

    override fun obfuscateDelta(delta: Long) = TimestampFull(timeInMillis = timeInMillis + delta, tz = tz)

    companion object {
        fun now() = makeNow()
    }
}
