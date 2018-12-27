/*
 * BetterContentProvider.java
 *
 * Copyright (C) 2012 Eric Butler
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
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.jetbrains.annotations.NonNls;

public abstract class BetterContentProvider extends ContentProvider {
    protected static final int CODE_COLLECTION = 100;
    protected static final int CODE_SINGLE = 101;
    private SQLiteOpenHelper mHelper;
    private final Class<? extends SQLiteOpenHelper> mHelperClass;
    private final String mItemType;
    private final Uri mContentUri;
    private final String mDirType;
    private final String mTableName;
    private final UriMatcher mUriMatcher;

    public BetterContentProvider(Class<? extends SQLiteOpenHelper> helperClass, String dirType, String itemType,
                                 String tableName, Uri contentUri) {
        mHelperClass = helperClass;
        mDirType = dirType;
        mItemType = itemType;
        mTableName = tableName;
        mContentUri = contentUri;

        String basePath = contentUri.getPath().substring(1);

        mUriMatcher = createUriMatcher(contentUri, basePath);
    }

    @Override
    public boolean onCreate() {
        try {
            mHelper = mHelperClass.getConstructor(Context.class).newInstance(getContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(mTableName);
        appendWheres(builder, mUriMatcher, uri);

        SQLiteDatabase db = mHelper.getReadableDatabase();

        Cursor cursor = builder.query(db, null, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(@NonNls @NonNull Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case CODE_COLLECTION:
                return mDirType;
            case CODE_SINGLE:
                return mItemType;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNls @NonNull Uri uri, ContentValues values) {
        if (mUriMatcher.match(uri) != CODE_COLLECTION) {
            throw new IllegalArgumentException("Incorrect URI: " + uri);
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long rowId = db.insertOrThrow(mTableName, null, values);

        Uri itemUri = ContentUris.withAppendedId(mContentUri, rowId);
        getContext().getContentResolver().notifyChange(itemUri, null);

        return itemUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        @NonNls SQLiteDatabase db = mHelper.getWritableDatabase();
        int count = 0;
        switch (mUriMatcher.match(uri)) {
            case CODE_SINGLE:
                String rowId = uri.getPathSegments().get(1);
                if (TextUtils.isEmpty(selection)) {
                    count = db.delete(mTableName, BaseColumns._ID + "=?", new String[]{rowId});
                } else {
                    count = db.delete(mTableName,
                            selection + " AND " + BaseColumns._ID + "=" + rowId,
                            selectionArgs);
                }
                break;
            case CODE_COLLECTION:
                count = db.delete(mTableName, selection, selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNls @NonNull Uri uri, ContentValues values, @NonNls String selection, String[] selectionArgs) {
        @NonNls SQLiteDatabase db = mHelper.getWritableDatabase();
        int count;
        switch (mUriMatcher.match(uri)) {
            case CODE_COLLECTION:
                count = db.update(mTableName, values, selection, selectionArgs);
                break;
            case CODE_SINGLE:
                String rowId = uri.getPathSegments().get(1);
                if (TextUtils.isEmpty(selection)) {
                    count = db.update(mTableName, values, BaseColumns._ID + "=" + rowId, null);
                } else {
                    count = db.update(mTableName, values,
                            selection + " AND " + BaseColumns._ID + "=" + rowId,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    protected UriMatcher createUriMatcher(Uri contentUri, @NonNls String basePath) {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(contentUri.getAuthority(), basePath, CODE_COLLECTION);
        matcher.addURI(contentUri.getAuthority(), basePath + "/#", CODE_SINGLE);
        return matcher;
    }

    protected void appendWheres(SQLiteQueryBuilder builder, UriMatcher matcher, Uri uri) {
        switch (matcher.match(uri)) {
            case CODE_COLLECTION:
                // Nothing needed here
                break;
            case CODE_SINGLE:
                builder.appendWhere(BaseColumns._ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}
