/*
 * IntercodeTransitData.java
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

package au.id.micolous.metrodroid.transit.intercode;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.id.micolous.farebot.R;
import au.id.micolous.metrodroid.card.CardType;
import au.id.micolous.metrodroid.card.calypso.CalypsoApplication;
import au.id.micolous.metrodroid.card.calypso.CalypsoCardTransitFactory;
import au.id.micolous.metrodroid.card.iso7816.ISO7816File;
import au.id.micolous.metrodroid.card.iso7816.ISO7816Record;
import au.id.micolous.metrodroid.transit.CardInfo;
import au.id.micolous.metrodroid.transit.TransitIdentity;
import au.id.micolous.metrodroid.transit.en1545.Calypso1545TransitData;
import au.id.micolous.metrodroid.transit.en1545.En1545Bitmap;
import au.id.micolous.metrodroid.transit.en1545.En1545Container;
import au.id.micolous.metrodroid.transit.en1545.En1545Field;
import au.id.micolous.metrodroid.transit.en1545.En1545FixedInteger;
import au.id.micolous.metrodroid.transit.en1545.En1545FixedString;
import au.id.micolous.metrodroid.transit.en1545.En1545Lookup;
import au.id.micolous.metrodroid.transit.en1545.En1545Parsed;
import au.id.micolous.metrodroid.transit.en1545.En1545Repeat;
import au.id.micolous.metrodroid.ui.ListItem;
import au.id.micolous.metrodroid.util.Utils;
import au.id.micolous.metrodroid.xml.ImmutableByteArray;

public class IntercodeTransitData extends Calypso1545TransitData {
    private static final int COUNTRY_ID_FRANCE = 0x250;

    // NOTE: Many French smart-cards don't have a brand name, and are simply referred to as a "titre
    // de transport" (ticket). Here they take the name of the transit agency.

    // https://www.tisseo.fr/les-tarifs/obtenir-une-carte-pastel
    private static final CardInfo TISSEO_CARD_INFO = new CardInfo.Builder()
            .setName("Pastel")
            .setLocation(R.string.location_toulouse)
            .setCardType(CardType.ISO7816)
            .setPreview()
            .build();

    private static final CardInfo TRANSGIRONDE_CARD_INFO = new CardInfo.Builder()
            .setName("TransGironde")
            .setLocation(R.string.location_gironde)
            .setCardType(CardType.ISO7816)
            .setPreview()
            .build();

    private static final CardInfo OURA_CARD_INFO = new CardInfo.Builder()
            .setName("OùRA")
            .setLocation(R.string.location_grenoble)
            .setCardType(CardType.ISO7816)
            .build();

    private static final CardInfo NAVIGO_CARD_INFO = new CardInfo.Builder()
            .setName("Navigo")
            .setLocation(R.string.location_paris)
            .setCardType(CardType.ISO7816)
            .build();

    private static final CardInfo ENVIBUS_CARD_INFO = new CardInfo.Builder()
            .setName("Envibus")
            .setLocation(R.string.location_sophia_antipolis)
            .setCardType(CardType.ISO7816)
            .build();

    private static final CardInfo TAM_MONTPELLIER_CARD_INFO = new CardInfo.Builder()
            .setName("TaM") // Transports de l'agglomération de Montpellier
            .setLocation(R.string.location_montpellier)
            .setCardType(CardType.ISO7816)
            .build();

    public static final Creator<IntercodeTransitData> CREATOR = new Creator<IntercodeTransitData>() {
        @NonNull
        public IntercodeTransitData createFromParcel(Parcel parcel) {
            return new IntercodeTransitData(parcel);
        }

        @NonNull
        public IntercodeTransitData[] newArray(int size) {
            return new IntercodeTransitData[size];
        }
    };
    public final static En1545Container TICKET_ENV_FIELDS = new En1545Container(
            new En1545FixedInteger(ENV_VERSION_NUMBER, 6),
            new En1545Bitmap(
                    new En1545FixedInteger(ENV_NETWORK_ID, 24),
                    new En1545FixedInteger(ENV_APPLICATION_ISSUER_ID, 8),
                    En1545FixedInteger.date(ENV_APPLICATION_VALIDITY_END),
                    new En1545FixedInteger("EnvPayMethod", 11),
                    new En1545FixedInteger(ENV_AUTHENTICATOR, 16),
                    new En1545FixedInteger("EnvSelectList", 32),
                    new En1545Container(
                            new En1545FixedInteger("EnvCardStatus", 1),
                            new En1545FixedInteger("EnvExtra", 0)
                    )
            )
    );
    private final static En1545Container HOLDER_FIELDS = new En1545Container(
            new En1545Bitmap(
                    new En1545Bitmap(
                            new En1545FixedString("HolderSurname", 85),
                            new En1545FixedString("HolderForename", 85)
                    ),
                    new En1545Bitmap(
                            new En1545FixedInteger(HOLDER_BIRTH_DATE, 32),
                            new En1545FixedString("HolderBirthPlace", 115)
                    ),
                    new En1545FixedString("HolderBirthName", 85),
                    new En1545FixedInteger(HOLDER_ID_NUMBER, 32),
                    new En1545FixedInteger("HolderCountryAlpha", 24),
                    new En1545FixedInteger("HolderCompany", 32),
                    new En1545Repeat(2,
                            new En1545Bitmap(
                                    new En1545FixedInteger("HolderProfileNetworkId", 24),
                                    new En1545FixedInteger("HolderProfileNumber", 8),
                                    En1545FixedInteger.date(HOLDER_PROFILE)
                            )
                    ),
                    new En1545Bitmap(
                            new En1545FixedInteger("HolderDataCardStatus", 4),
                            new En1545FixedInteger("HolderDataTeleReglement", 4),
                            new En1545FixedInteger("HolderDataResidence", 17),
                            new En1545FixedInteger("HolderDataCommercialID", 6),
                            new En1545FixedInteger("HolderDataWorkPlace", 17),
                            new En1545FixedInteger("HolderDataStudyPlace", 17),
                            new En1545FixedInteger("HolderDataSaleDevice", 16),
                            new En1545FixedInteger("HolderDataAuthenticator", 16),
                            En1545FixedInteger.date("HolderDataProfileStart1"),
                            En1545FixedInteger.date("HolderDataProfileStart2"),
                            En1545FixedInteger.date("HolderDataProfileStart3"),
                            En1545FixedInteger.date("HolderDataProfileStart4")
                    )
            )
    );
    private final static En1545Container TICKET_ENV_HOLDER_FIELDS = new En1545Container(
            TICKET_ENV_FIELDS, HOLDER_FIELDS);

    private static final En1545Field contractListFields = new En1545Repeat(4,
            new En1545Bitmap(
                    new En1545FixedInteger(CONTRACTS_NETWORK_ID, 24),
                    new En1545FixedInteger(CONTRACTS_TARIFF, 16),
                    new En1545FixedInteger(CONTRACTS_POINTER, 5)
            )
    );

    private IntercodeTransitData(CalypsoApplication card) {
        super(card, TICKET_ENV_HOLDER_FIELDS, contractListFields, getSerial(getNetId(card), card));
    }

    protected IntercodeTransaction createTrip(ImmutableByteArray data) {
        return new IntercodeTransaction(data, getNetworkId());
    }

    @Nullable
    @Override
    protected IntercodeTransaction createSpecialEvent(ImmutableByteArray data) {
        return new IntercodeTransaction(data, getNetworkId());
    }

    protected IntercodeSubscription createSubscription(ImmutableByteArray data, En1545Parsed contractList, Integer listNum,
                                                       int recordNum, Integer counter) {
        if (contractList == null || listNum == null)
            return null;
        Integer tariff = contractList.getInt(CONTRACTS_TARIFF, listNum);
        if (tariff == null)
            return null;
        return new IntercodeSubscription(data, (tariff >> 4) & 0xff, getNetworkId(), counter);
    }

    private static final SparseArray<Pair<CardInfo, En1545Lookup>> NETWORKS = new SparseArray<>();

    static {
        NETWORKS.put(0x250064, Pair.create(TAM_MONTPELLIER_CARD_INFO, new IntercodeLookupUnknown()));
        NETWORKS.put(0x250502, Pair.create(OURA_CARD_INFO, new IntercodeLookupSTR("oura")));
        NETWORKS.put(0x250901, Pair.create(NAVIGO_CARD_INFO, new IntercodeLookupNavigo()));
        NETWORKS.put(0x250916, Pair.create(TISSEO_CARD_INFO, new IntercodeLookupTisseo()));
        NETWORKS.put(0x250920, Pair.create(ENVIBUS_CARD_INFO, new IntercodeLookupUnknown()));
        NETWORKS.put(0x250921, Pair.create(TRANSGIRONDE_CARD_INFO, new IntercodeLookupGironde()));
    }

    public static En1545Lookup getLookup(int networkId) {
        if (NETWORKS.get(networkId) != null)
            return NETWORKS.get(networkId).second;
        return new IntercodeLookupUnknown();
    }

    private static String getCardName(int networkId) {
        if (NETWORKS.get(networkId) != null)
            return NETWORKS.get(networkId).first.getName();
        if ((networkId >> 12) == COUNTRY_ID_FRANCE)
            return "Intercode-France-" + Integer.toHexString(networkId & 0xfff);
        return "Intercode-" + Integer.toHexString(networkId);
    }

    private static int getNetId(CalypsoApplication card) {
        return card.getFile(CalypsoApplication.File.TICKETING_ENVIRONMENT).getRecord(1)
                .getData().getBitsFromBuffer(13, 24);
    }

    private static String getSerial(int netId, CalypsoApplication card) {
        ISO7816File iccFile = card.getFile(CalypsoApplication.File.ICC);
        if (iccFile == null) {
            return null;
        }

        ISO7816Record iccRecord = iccFile.getRecord(1);

        if (iccRecord == null) {
            return null;
        }
        ImmutableByteArray data = iccRecord.getData();

        if (netId == 0x250502)
            return data.getHexString(20, 6).substring(1,11);

        if (data.byteArrayToLong(16, 4) != 0) {
            return Long.toString(data.byteArrayToLong(16, 4));
        }

        if (data.byteArrayToLong(0, 4) != 0) {
            return Long.toString(data.byteArrayToLong(0, 4));
        }

        return null;
    }

    public final static CalypsoCardTransitFactory FACTORY = new CalypsoCardTransitFactory() {
        @NonNull
        @Override
        public List<CardInfo> getAllCards() {
            List<CardInfo> li = new ArrayList<>();
            for(int i = 0, nsize = NETWORKS.size(); i < nsize; i++) {
                li.add(NETWORKS.valueAt(i).first);
            }
            return li;
        }

        @NonNull
        @Override
        public TransitIdentity parseTransitIdentity(@NonNull CalypsoApplication card) {
            int netId = getNetId(card);
            return new TransitIdentity(getCardName(netId), getSerial(netId, card));
        }

        @Override
        public boolean check(ImmutableByteArray ticketEnv) {
            try {
                int netId = ticketEnv.getBitsFromBuffer(13, 24);
                return NETWORKS.get(netId) != null || COUNTRY_ID_FRANCE == (netId >> 12);
            } catch (Exception e) {
                return false;
            }
        }

        @NonNull
        @Override
        public IntercodeTransitData parseTransitData(@NonNull CalypsoApplication card) {
            return new IntercodeTransitData(card);
        }

        @Override
        public CardInfo getCardInfo(ImmutableByteArray ticketEnv) {
            int netId = ticketEnv.getBitsFromBuffer(13, 24);
            if (NETWORKS.get(netId) != null)
                return NETWORKS.get(netId).first;
            return null;
        }
    };

    @Override
    public String getCardName() {
        return getCardName(getNetworkId());
    }

    private IntercodeTransitData(Parcel parcel) {
        super(parcel);
    }

    @Override
    public List<ListItem> getInfo() {
        List <ListItem> items =  super.getInfo();
        Set<String> handled = new HashSet<>(Arrays.asList(
                ENV_NETWORK_ID,
                En1545FixedInteger.dateName(ENV_APPLICATION_ISSUE),
                ENV_APPLICATION_ISSUER_ID,
                En1545FixedInteger.dateName(ENV_APPLICATION_VALIDITY_END),
                ENV_AUTHENTICATOR,
                En1545FixedInteger.dateName(HOLDER_PROFILE),
                HOLDER_BIRTH_DATE,
                HOLDER_POSTAL_CODE));
        items.addAll(mTicketEnvParsed.getInfo(handled));
        return items;
    }

    @Override
    protected En1545Lookup getLookup() {
        return getLookup(getNetworkId());
    }
}
