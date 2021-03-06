/*
 * MTRReaderTest.java
 *
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

package au.id.micolous.metrodroid.test;

import org.junit.Test;

import au.id.micolous.metrodroid.transit.Station;
import au.id.micolous.metrodroid.transit.ezlink.EZLinkTransitData;

import static junit.framework.TestCase.assertEquals;

/**
 * Tests StationTableReader (MdST). This uses the ezlink stop database.
 */

public class MRTReaderTest extends BaseInstrumentedTest {
    @Test
    public void testGetStation() {
        setLocale("en-US");
        showRawStationIds(false);
        showLocalAndEnglish(false);

        Station s = EZLinkTransitData.getStation("CGA");
        assertEquals("Changi Airport", s.getStationName());
        assertEquals(1.3575, Float.valueOf(s.getLatitude()), 0.001);
        assertEquals(103.9885, Float.valueOf(s.getLongitude()), 0.001);
    }
}
