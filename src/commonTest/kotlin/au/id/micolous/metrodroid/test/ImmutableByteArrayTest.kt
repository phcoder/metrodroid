/*
 * ImmutableByteArrayTest.kt
 *
 * Copyright 2019 Michael Farrell <micolous+git@gmail.com>
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
package au.id.micolous.metrodroid.test

import au.id.micolous.metrodroid.util.ImmutableByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ImmutableByteArrayTest {
    @Test
    fun testIndexOfAtStart() {
        // Check single byte
        val s = ImmutableByteArray.fromHex("0102030405")
        val n = ImmutableByteArray.fromHex("01")

        assertEquals(0, s.indexOf(n))
        assertEquals(-1, s.indexOf(n, 1))
        assertEquals(-1, s.indexOf(n, end = 0))

        // Check multiple bytes
        val n2 = ImmutableByteArray.fromHex("010203")
        assertEquals(0, s.indexOf(n2))
        assertEquals(-1, s.indexOf(n2, 1))

        // Check whole bytes match
        assertEquals(0, s.indexOf(s))
        assertEquals(-1, s.indexOf(s, 1))
        assertEquals(-1, s.indexOf(s, end = 1))
        assertEquals(-1, s.indexOf(s, end = 2))
    }

    @Test
    fun testIndexOfAtMiddle() {
        // Check single byte
        val s = ImmutableByteArray.fromHex("0102030405")
        val n = ImmutableByteArray.fromHex("03")

        assertEquals(2, s.indexOf(n))
        assertEquals(2, s.indexOf(n, 1))
        assertEquals(2, s.indexOf(n, 2))
        assertEquals(-1, s.indexOf(n, 3))
        assertEquals(-1, s.indexOf(n, end = 0))
        assertEquals(-1, s.indexOf(n, end = 1))
        assertEquals(-1, s.indexOf(n, end = 2))

        // Check multiple bytes
        val n2 = ImmutableByteArray.fromHex("0304")
        assertEquals(2, s.indexOf(n2))
        assertEquals(2, s.indexOf(n2, 1))
        assertEquals(2, s.indexOf(n2, 2))
        assertEquals(-1, s.indexOf(n2, 3))

        assertEquals(-1, s.indexOf(n2, end = 0))
        assertEquals(-1, s.indexOf(n2, end = 1))
        assertEquals(-1, s.indexOf(n2, end = 2))

        assertEquals(-1, s.indexOf(n2, 1, 1))
        assertEquals(-1, s.indexOf(n2, 1, 2))
        assertEquals(-1, s.indexOf(n2, 1, 3))
        assertEquals(2, s.indexOf(n2, 1, 4))
        assertEquals(2, s.indexOf(n2, 1, 5))

        assertEquals(-1, s.indexOf(n2, 2, 2))
        assertEquals(-1, s.indexOf(n2, 2, 3))
        assertEquals(2, s.indexOf(n2, 2, 4))
        assertEquals(2, s.indexOf(n2, 2, 5))

        assertEquals(-1, s.indexOf(n2, 3, 4))
        assertEquals(-1, s.indexOf(n2, 3, 5))
        assertEquals(-1, s.indexOf(n2, 3, 6))
    }

    @Test
    fun testBase64() {
        assertEquals(ImmutableByteArray.fromASCII("Metrodroid"),
            ImmutableByteArray.fromBase64("TWV0cm9kcm9pZA=="))
        assertEquals(ImmutableByteArray.fromASCII("Metrodroid"),
            ImmutableByteArray.fromBase64("TWV-0cm9k-cm9p----ZA==--"))
        assertFails { ImmutableByteArray.fromBase64("Metrodroid") } // Wrong padding
        assertFails { ImmutableByteArray.fromBase64("====") } // Wrong padding
    }

    @Test
    fun testUTF() {
        val STR = "Metrodroid\uD83D\uDE00\uD83C\uDDE6\uD83C\uDDFAМетродроид"
        val u16be = ImmutableByteArray.fromHex("004d006500740072006f00640072006f00690064d83dde00d83cdde6d83cddfa041c043504420440043e04340440043e04380434")
        val u16le = ImmutableByteArray.fromHex("4d006500740072006f00640072006f00690064003dd800de3cd8e6dd3cd8fadd1c043504420440043e04340440043e0438043404")
        val u8 = ImmutableByteArray.fromHex("4d6574726f64726f6964f09f9880f09f87a6f09f87bad09cd0b5d182d180d0bed0b4d180d0bed0b8d0b4")
        val invChar = "\uFFFD"
        assertEquals(STR, u16be.readUTF16(isLittleEndian = false))
        assertEquals(STR, u16le.readUTF16(isLittleEndian = true))
        assertEquals(STR, u16be.readUTF16BOM(isLittleEndianDefault = false))
        assertEquals(STR, u16le.readUTF16BOM(isLittleEndianDefault = true))
        val u16lebom = ImmutableByteArray.fromHex("fffe") + u16le
        assertEquals(STR, u16lebom.readUTF16BOM(isLittleEndianDefault = false))
        assertEquals(STR, u16lebom.readUTF16BOM(isLittleEndianDefault = true))
        val u16bebom = ImmutableByteArray.fromHex("feff") + u16be
        assertEquals(STR, u16bebom.readUTF16BOM(isLittleEndianDefault = false))
        assertEquals(STR, u16bebom.readUTF16BOM(isLittleEndianDefault = true))
        assertEquals(STR, u8.readUTF8())
        assertEquals(STR+invChar, (u16le + ImmutableByteArray.fromHex("41")).readUTF16(isLittleEndian = true))
        assertEquals(u8, ImmutableByteArray.fromUTF8(STR))
        assertEquals(invChar, ImmutableByteArray.fromHex("41").readUTF16BOM(isLittleEndianDefault = true))
    }
}
