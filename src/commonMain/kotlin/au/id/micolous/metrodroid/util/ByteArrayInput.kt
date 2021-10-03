/*
 * ByteArrayInput.kt
 *
 * Copyright (C) 2021 Google
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

package au.id.micolous.metrodroid.util

import kotlinx.io.core.AbstractInput
import kotlinx.io.core.ExperimentalIoApi
import kotlinx.io.core.IoBuffer
import kotlin.math.min

@ExperimentalIoApi
class ByteArrayInput (val ba: ByteArray, var offset: Int = 0,
                      val bufSize: Int = 8192): AbstractInput() {
    override fun fill(): IoBuffer? {
        val off = offset
        val sz = min(bufSize, ba.size - offset)
        offset += sz
        return IoBuffer.Pool.borrow().apply {
            writeFully(ba, off, sz)
        }
    }

    override fun closeSource() {
    }
}