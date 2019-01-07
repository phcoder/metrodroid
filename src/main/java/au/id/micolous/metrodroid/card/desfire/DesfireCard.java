/*
 * DesfireCard.java
 *
 * Copyright 2011-2015 Eric Butler <eric@codebutler.com>
 * Copyright 2015-2018 Michael Farrell <micolous+git@gmail.com>
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

package au.id.micolous.metrodroid.card.desfire;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import au.id.micolous.farebot.R;
import au.id.micolous.metrodroid.card.Card;
import au.id.micolous.metrodroid.card.CardTransceiver;
import au.id.micolous.metrodroid.card.CardType;
import au.id.micolous.metrodroid.card.TagReaderFeedbackInterface;
import au.id.micolous.metrodroid.card.desfire.files.DesfireFile;
import au.id.micolous.metrodroid.card.desfire.files.InvalidDesfireFile;
import au.id.micolous.metrodroid.card.desfire.files.UnauthorizedDesfireFile;
import au.id.micolous.metrodroid.card.desfire.settings.DesfireFileSettings;
import au.id.micolous.metrodroid.card.desfire.settings.StandardDesfireFileSettings;
import au.id.micolous.metrodroid.card.desfire.settings.ValueDesfireFileSettings;
import au.id.micolous.metrodroid.transit.CardInfo;
import au.id.micolous.metrodroid.transit.CardTransitFactory;
import au.id.micolous.metrodroid.transit.TransitData;
import au.id.micolous.metrodroid.transit.TransitIdentity;
import au.id.micolous.metrodroid.transit.clipper.ClipperTransitData;
import au.id.micolous.metrodroid.transit.hsl.HSLTransitData;
import au.id.micolous.metrodroid.transit.serialonly.IstanbulKartTransitData;
import au.id.micolous.metrodroid.transit.serialonly.MykiTransitData;
import au.id.micolous.metrodroid.transit.opal.OpalTransitData;
import au.id.micolous.metrodroid.transit.orca.OrcaTransitData;
import au.id.micolous.metrodroid.transit.adelaide.AdelaideMetrocardTransitData;
import au.id.micolous.metrodroid.transit.serialonly.AtHopTransitData;
import au.id.micolous.metrodroid.transit.tfi_leap.LeapTransitData;
import au.id.micolous.metrodroid.transit.tfi_leap.LeapUnlocker;
import au.id.micolous.metrodroid.transit.serialonly.TrimetHopTransitData;
import au.id.micolous.metrodroid.transit.unknown.UnauthorizedDesfireTransitData;
import au.id.micolous.metrodroid.ui.ListItem;
import au.id.micolous.metrodroid.ui.ListItemRecursive;
import au.id.micolous.metrodroid.util.Utils;
import au.id.micolous.metrodroid.xml.ImmutableByteArray;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

@Root(name = "card")
public class DesfireCard extends Card {
    private static final String TAG = "DesfireCard";
    private static final DesfireCardTransitFactory[] FACTORIES = {
            OrcaTransitData.FACTORY,
            ClipperTransitData.FACTORY,
            HSLTransitData.FACTORY,
            OpalTransitData.FACTORY,
            MykiTransitData.Companion.getFACTORY(),
            IstanbulKartTransitData.Companion.getFACTORY(),
            LeapTransitData.FACTORY,
            TrimetHopTransitData.Companion.getFACTORY(),
            AdelaideMetrocardTransitData.FACTORY,
            AtHopTransitData.Companion.getFACTORY(),
            UnauthorizedDesfireTransitData.FACTORY
    };

    @Element(name = "manufacturing-data")
    private DesfireManufacturingData mManfData;
    @ElementList(name = "applications")
    private List<DesfireApplication> mApplications;

    private DesfireCard() { /* For XML Serializer */ }

    public DesfireCard(ImmutableByteArray tagId, Calendar scannedAt, DesfireManufacturingData manfData,
                       List<DesfireApplication> apps) {
        super(CardType.MifareDesfire, tagId, scannedAt);
        mManfData = manfData;
        mApplications = apps;
    }

    /**
     * Dumps a DESFire tag in the field.
     * @param tech Tag to dump.
     * @return DesfireCard of the card contents. Returns null if an unsupported card is in the
     *         field.
     * @throws Exception On communication errors.
     */
    @Nullable
    public static DesfireCard dumpTag(CardTransceiver tech, ImmutableByteArray uid,
                                      TagReaderFeedbackInterface feedbackInterface) throws Exception {
            List<DesfireApplication> apps = new ArrayList<>();

            DesfireManufacturingData manufData;

        try {
            DesfireProtocol desfireTag = new DesfireProtocol(tech);

            try {
                manufData = desfireTag.getManufacturingData();
            } catch (IllegalArgumentException e) {
                // Credit cards tend to fail at this point.
                Log.w(TAG, "Card responded with invalid response, may not be DESFire?", e);
                return null;
            }

            feedbackInterface.updateStatusText(Utils.localizeString(R.string.mfd_reading));
            feedbackInterface.updateProgressBar(0, 1);

            int[] appIds = desfireTag.getAppList();
            int maxProgress = appIds.length;
            int progress = 0;

            CardInfo i = parseEarlyCardInfo(appIds);
            if (i != null) {
                Log.d(TAG, String.format(Locale.ENGLISH, "Early Card Info: %s", i.getName()));
                feedbackInterface.updateStatusText(Utils.localizeString(R.string.card_reading_type, i.getName()));
                feedbackInterface.showCardType(i);
            }

            // Uncomment this to test the card type display.
            //Thread.sleep(5000);

            for (int appId : appIds) {
                feedbackInterface.updateProgressBar(progress, maxProgress);
                desfireTag.selectApp(appId);
                progress++;

                List<DesfireFile> files = new ArrayList<>();

                DesfireUnlocker unlocker = null;
                if(LeapTransitData.earlyCheck(appId))
                    unlocker = LeapUnlocker.createUnlocker(appId, manufData);
                int[] fileIds = desfireTag.getFileList();
                if (unlocker != null) {
                    fileIds = unlocker.getOrder(desfireTag, fileIds);
                }
                maxProgress += fileIds.length * (unlocker == null ? 1 : 2);
                List<DesfireAuthLog> authLog = new ArrayList<>();
                for (int fileId : fileIds) {
                    feedbackInterface.updateProgressBar(progress, maxProgress);
                    if (unlocker != null) {
                        if (i != null) {
                            feedbackInterface.updateStatusText(
                                    Utils.localizeString(R.string.mfd_unlocking, i.getName()));
                        }
                        unlocker.unlock(desfireTag, files, fileId, authLog);
                        feedbackInterface.updateProgressBar(++progress, maxProgress);
                    }

                    DesfireFileSettings settings = null;
                    try {
                        settings = desfireTag.getFileSettings(fileId);
                        byte[] data;
                        if (settings instanceof StandardDesfireFileSettings) {
                            data = desfireTag.readFile(fileId);
                        } else if (settings instanceof ValueDesfireFileSettings) {
                            data = desfireTag.getValue(fileId);
                        } else {
                            data = desfireTag.readRecord(fileId);
                        }
                        files.add(DesfireFile.create(fileId, settings, data));
                    } catch (AccessControlException ex) {
                        files.add(new UnauthorizedDesfireFile(fileId, ex.getMessage(), settings));
                    } catch (IOException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        files.add(new InvalidDesfireFile(fileId, ex.toString(), settings));
                    }
                    progress++;
                }

                apps.add(new DesfireApplication(appId, files, authLog));
            }
        } finally {
        }

        return new DesfireCard(uid, GregorianCalendar.getInstance(), manufData, apps);
    }

    /**
     * DESFire has well-known application IDs.  If those application IDs are sufficient to detect
     * a particular type of card (or at least have a really good guess at it), then we should send
     * back a CardInfo.
     *
     * If we have no idea, then send back "null".
     *
     * Each of these checks should be really cheap to run, because this blocks further card
     * reads.
     * @param appIds An array of DESFire application IDs that are present on the card.
     * @return A CardInfo about the card, or null if we have no idea.
     */
    private static CardInfo parseEarlyCardInfo(int[] appIds) {
        for (DesfireCardTransitFactory f : FACTORIES)
            if (f.earlyCheck(appIds))
                return f.getCardInfo(appIds);

        return null;
    }

    public static List<CardTransitFactory<DesfireCard>> getAllFactories() {
        return Arrays.asList(FACTORIES);
    }

    @Override
    public TransitIdentity parseTransitIdentity() {
        for (DesfireCardTransitFactory f : FACTORIES)
            if (f.check(this))
                return f.parseTransitIdentity(this);

        return null;
    }

    @Override
    public TransitData parseTransitData() {
        for (DesfireCardTransitFactory f : FACTORIES)
            if (f.check(this))
                return f.parseTransitData(this);
        return null;
    }

    @Override
    public List<ListItem> getManufacturingInfo() {
        DesfireManufacturingData data = getManufacturingData();
        return data.getInfo();
    }

    public List<DesfireApplication> getApplications() {
        return mApplications;
    }

    public DesfireApplication getApplication(int appId) {
        for (DesfireApplication app : mApplications) {
            if (app.getId() == appId)
                return app;
        }
        return null;
    }

    public DesfireManufacturingData getManufacturingData() {
        return mManfData;
    }

    @NonNull
    @Override
    public List<ListItem> getRawData() {
        List<ListItem> li = new ArrayList<>();
        for (DesfireApplication app : mApplications) {
            List<ListItem> ali = app.getRawData();
            li.add(new ListItemRecursive(
                    Utils.localizeString(R.string.application_title_format,
                            Utils.intToHex(app.getId())),
                    null, ali));
        }
        return li;
    }
}
