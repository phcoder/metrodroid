/*
 * Base64String.java
 *
 * Copyright (C) 2014 Eric Butler <eric@codebutler.com>
 * Copyright 2018 Michael Farrell <micolous+git@gmail.com>
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
package au.id.micolous.metrodroid.xml;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.util.Arrays;

import au.id.micolous.metrodroid.util.Utils;

/**
 * Wrapper for the XML serialiser, that encodes <code>byte[]</code> as a
 * Base64 string.
 */
public class Base64String {
    private final byte[] mData;

    private static final Base64String EMPTY = new Base64String();

    private Base64String() {
        mData = new byte[0];
    }

    /**
     * Builds a new Base64String, taking a <code>byte[]</code> as input.
     *
     * If {@param data} is null, then a zero-length byte array will be used.
     *
     * If {@param data} is known to be constantly null, use {@link #empty()}
     * instead.
     *
     * @param data Data to encode
     */
    public Base64String(@Nullable byte[] data) {
        if (data == null) {
            mData = new byte[0];
        } else {
            mData = data;
        }
    }

    /**
     * Builds a new Base64String, taking a Base64 encoded string as input.
     * @param data Base64 encoded data
     * @throws IllegalArgumentException On invalid Base64 data.
     */
    public Base64String(String data) throws IllegalArgumentException {
        mData = Base64.decode(data, Base64.DEFAULT);
    }

    /**
     * Gets the underlying data in this Base64String.
     */
    @NonNull
    public byte[] getData() {
        return mData;
    }

    @NonNull
    public String toBase64() {
        return Base64.encodeToString(mData, Base64.NO_WRAP);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Base64String)) {
            return false;
        }

        Base64String other = (Base64String)obj;
        return Arrays.equals(mData, other.mData);
    }

    /**
     * Returns this Base64 string encoded in base16.
     */
    @NonNull
    @Override
    public String toString() {
        return Utils.getHexString(mData);
    }

    /**
     * Returns a zero-length Base64String.
     */
    @NonNull
    public static Base64String empty() {
        return EMPTY;
    }

    public static final class Transform implements org.simpleframework.xml.transform.Transform<Base64String> {
        @Override
        public Base64String read(String value) {
            return new Base64String(value);
        }

        @Override
        public String write(Base64String value) {
            return value.toBase64();
        }
    }
}
