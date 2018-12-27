/*
 * CardProvider.java
 *
 * Copyright (C) 2011 Eric Butler
 *
 * Authors:
 * Eric Butler <eric@codebutler.com>
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

package au.id.micolous.metrodroid.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.jetbrains.annotations.NonNls;

import au.id.micolous.farebot.BuildConfig;

public class CardProvider extends ContentProvider {
    @NonNls
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".cardprovider";

    public static final Uri CONTENT_URI_CARD = Uri.parse("content://" + AUTHORITY + "/cards");

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "cards", CardDBHelper.CARD_COLLECTION_URI_INDICATOR);
        sUriMatcher.addURI(AUTHORITY, "cards/#", CardDBHelper.SINGLE_CARD_URI_INDICATOR);
    }

    private CardDBHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new CardDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNls @NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        @NonNls SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case CardDBHelper.CARD_COLLECTION_URI_INDICATOR:
                builder.setTables(CardsTableColumns.TABLE_NAME);
                //builder.setProjectionMap();
                break;
            case CardDBHelper.SINGLE_CARD_URI_INDICATOR:
                builder.setTables(CardsTableColumns.TABLE_NAME);
                builder.appendWhere(CardsTableColumns._ID + " = " + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = builder.query(db, null, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(@NonNls @NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CardDBHelper.CARD_COLLECTION_URI_INDICATOR:
                return CardDBHelper.CARD_DIR_TYPE;
            case CardDBHelper.SINGLE_CARD_URI_INDICATOR:
                return CardDBHelper.CARD_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNls @NonNull Uri uri, ContentValues values) {
        if (sUriMatcher.match(uri) != CardDBHelper.CARD_COLLECTION_URI_INDICATOR) {
            throw new IllegalArgumentException("Incorrect URI: " + uri);
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insertOrThrow(CardsTableColumns.TABLE_NAME, null, values);

        Uri cardUri = ContentUris.withAppendedId(CONTENT_URI_CARD, rowId);
        getContext().getContentResolver().notifyChange(cardUri, null);

        return cardUri;
    }

    @SuppressWarnings("MagicCharacter")
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        @NonNls SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case CardDBHelper.CARD_COLLECTION_URI_INDICATOR:
                count = db.delete(CardsTableColumns.TABLE_NAME, selection, selectionArgs);
                break;
            case CardDBHelper.SINGLE_CARD_URI_INDICATOR:
                String rowId = uri.getPathSegments().get(1);
                count = db.delete(CardsTableColumns.TABLE_NAME
                        , CardsTableColumns._ID + "=" + rowId
                                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "")
                        , selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        @NonNls SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case CardDBHelper.CARD_COLLECTION_URI_INDICATOR:
                count = db.update(CardsTableColumns.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CardDBHelper.SINGLE_CARD_URI_INDICATOR:
                String rowId = uri.getPathSegments().get(1);
                count = db.update(CardsTableColumns.TABLE_NAME
                        , values
                        , CardsTableColumns._ID + "=" + rowId
                                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "")
                        , selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
