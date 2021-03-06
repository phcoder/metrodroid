/*
 * IstanbulKartTransitData.java
 *
 * Copyright 2015-2016 Michael Farrell <micolous+git@gmail.com>
 * Copyright 2018 Google Inc.
 *
 * Authors: Vladimir Serbinenko, Michael Farrell
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

package au.id.micolous.metrodroid.transit.serialonly

import au.id.micolous.farebot.R
import au.id.micolous.metrodroid.card.CardType
import au.id.micolous.metrodroid.card.desfire.DesfireCard
import au.id.micolous.metrodroid.card.desfire.DesfireCardTransitFactory
import au.id.micolous.metrodroid.transit.CardInfo
import au.id.micolous.metrodroid.transit.TransitIdentity
import au.id.micolous.metrodroid.ui.ListItem
import au.id.micolous.metrodroid.util.Utils
import au.id.micolous.metrodroid.xml.ImmutableByteArray
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Transit data type for IstanbulKart.
 *
 *
 * This is a very limited implementation of reading IstanbulKart, because most of the data is stored in
 * locked files.
 *
 *
 * Documentation of format: https://github.com/micolous/metrodroid/wiki/IstanbulKart
 */
@Parcelize
class IstanbulKartTransitData (private val mSerial: String,
                               private val mSerial2: String): SerialOnlyTransitData() {

    public override val extraInfo: List<ListItem>?
        get() = listOf(ListItem(R.string.istanbulkart_2nd_card_number, mSerial2))

    override val reason: SerialOnlyTransitData.Reason
        get() = SerialOnlyTransitData.Reason.LOCKED

    override fun getCardName() = NAME

    override fun getSerialNumber() = formatSerial(mSerial)

    companion object {
        private const val NAME = "IstanbulKart"
        private const val APP_ID = 0x422201

        private fun parse(card: DesfireCard): IstanbulKartTransitData? {
            val metadata = card.getApplication(APP_ID)?.getFile(2)?.data

            try {
                val serial = parseSerial(metadata) ?: return null
                return IstanbulKartTransitData(
                        mSerial = serial,
                        mSerial2 = card.tagId.toHexString().toUpperCase(Locale.ENGLISH))
            } catch (ex: Exception) {
                throw RuntimeException("Error parsing IstanbulKart data", ex)
            }
        }

        private val CARD_INFO = CardInfo.Builder()
                .setName(NAME)
                .setCardType(CardType.MifareDesfire)
                .setLocation(R.string.location_istanbul)
                .setExtraNote(R.string.card_note_card_number_only)
                .setImageId(R.drawable.istanbulkart_card, R.drawable.iso7810_id1_alpha)
                .build()

        /**
         * Parses a serial number in 0x42201 file 0x2
         * @param file content of the serial file
         * @return String with the complete serial number, or null on error
         */
        private fun parseSerial(file: ImmutableByteArray?) =
                file?.getHexString(0, 8)

        val FACTORY: DesfireCardTransitFactory = object : DesfireCardTransitFactory {
            override fun earlyCheck(appIds: IntArray) = (APP_ID in appIds)

            override fun getAllCards() = listOf(CARD_INFO)

            override fun parseTransitData(desfireCard: DesfireCard) = parse(desfireCard)

            override fun parseTransitIdentity(card: DesfireCard) =
                    TransitIdentity(NAME,
                            formatSerial(parseSerial(card.getApplication(APP_ID)!!
                                    .getFile(2)!!.data)))
        }

        private fun formatSerial(serial: String?) =
                serial?.let { Utils.groupString(it, " ", 4, 4, 4) }
    }
}
