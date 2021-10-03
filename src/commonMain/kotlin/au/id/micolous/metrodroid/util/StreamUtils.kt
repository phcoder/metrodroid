/*
 * StreamUtils.kt
 *
 * Copyright (C) 2019 Google
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

import kotlinx.io.core.Input
import kotlinx.io.charsets.Charsets
import kotlinx.io.core.String
import kotlinx.io.core.readBytes

fun Input.readToString(maxSize: Int? = null) : String = String(
            bytes = this.readBytes(),
            charset = Charsets.UTF_8)

fun Input.forEachLine(maxSize: Int? = null, function: (String) -> Unit) {
    this.readToString(maxSize=maxSize).split('\n', '\r').filter { it.isNotEmpty() }.forEach(function)
}