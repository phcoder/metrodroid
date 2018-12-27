/*
 * ISO7816TLV.java
 *
 * Copyright 2018 Michael Farrell <micolous+git@gmail.com>
 * Copyright 2018 Google
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

package au.id.micolous.metrodroid.card.iso7816

import au.id.micolous.metrodroid.ui.ListItem
import au.id.micolous.metrodroid.ui.ListItemRecursive
import au.id.micolous.metrodroid.util.Utils
import java.lang.Exception

object ISO7816TLV {
    // return: <leadBits, id, idlen>
    private fun getTLVIDLen(buf: ByteArray, p: Int): Int {
        if (buf[p].toInt() and 0x1f != 0x1f)
            return 1
        var len = 1
        while (buf[p + len++].toInt() and 0x80 != 0);
        return len
    }

    // return lenlen, lenvalue
    private fun decodeTLVLen(buf: ByteArray, p: Int): IntArray {
        val headByte = buf[p].toInt() and 0xff
        if (headByte shr 7 == 0)
            return intArrayOf(1, headByte and 0x7f)
        val numfollowingbytes = headByte and 0x7f
        return intArrayOf(1 + numfollowingbytes,
                Utils.byteArrayToInt(buf, p + 1, numfollowingbytes))
    }

    fun berTlvIterate(buf: ByteArray,
                              iterator: (id: ByteArray,
                                         header: ByteArray,
                                         data: ByteArray) -> Unit) {
        // Skip ID
        var p = getTLVIDLen(buf, 0)
        val (startoffset, fulllen) = decodeTLVLen(buf, p)
        p += startoffset

        while (p < fulllen) {
            val idlen = getTLVIDLen(buf, p)
            val (lenlen, datalen) = decodeTLVLen(buf, p + idlen)
            iterator(Utils.byteArraySlice(buf, p, idlen),
                    Utils.byteArraySlice(buf, p, idlen + lenlen),
                    Utils.byteArraySlice(buf, p + idlen + lenlen, datalen))

            p += idlen + lenlen + datalen
        }
    }

    fun pdolIterate(buf: ByteArray,
                    iterator: (id: ByteArray,
                               len: Int) -> Unit) {
        var p = 0

        while (p < buf.size) {
            val idlen = getTLVIDLen(buf, p)
            val (lenlen, datalen) = decodeTLVLen(buf, p + idlen)
            iterator(Utils.byteArraySlice(buf, p, idlen), datalen)

            p += idlen + lenlen
        }
    }

    fun findBERTLV(buf: ByteArray, target: String, keepHeader: Boolean): ByteArray? {
        var result: ByteArray? = null
        berTlvIterate(buf) { id, header, data ->
            if (Utils.getHexString(id) == target) {
                result = if (keepHeader) header + data else data
            }
        }
        return result
    }

    fun infoBerTLV(buf: ByteArray): List<ListItem> {
        val result = mutableListOf<ListItem>()
        berTlvIterate(buf) { id, header, data ->
            if (id[0].toInt() and 0xe0 == 0xa0)
                try {
                    result.add(ListItemRecursive(Utils.getHexString(id),
                            null, infoBerTLV(header + data)))
                } catch (e: Exception) {
                    result.add(ListItem(Utils.getHexDump(id), Utils.getHexDump(data)))
                }
            else
                result.add(ListItem(Utils.getHexDump(id), Utils.getHexDump(data)))

        }
        return result
    }

    fun infoWithRaw(buf: ByteArray) = listOfNotNull(
            ListItemRecursive.collapsedValue("RAW", Utils.getHexDump(buf)),
            try {
                ListItemRecursive("TLV", null, infoBerTLV(buf))
            } catch (e: Exception) {
                null
            })
}
