package au.id.micolous.metrodroid.transit.en1545;

import au.id.micolous.metrodroid.xml.ImmutableByteArray;

public interface En1545Bits {
    int getBitsFromBuffer(ImmutableByteArray buffer, int iStartBit, int iLength);
}
