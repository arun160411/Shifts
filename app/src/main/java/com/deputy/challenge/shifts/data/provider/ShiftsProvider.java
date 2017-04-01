package com.deputy.challenge.shifts.data.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.deputy.challenge.shifts.data.db.ShiftsOpenHelper;

import static com.deputy.challenge.shifts.data.contract.ShiftsContract.CONTENT_AUTHORITY;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.PATH_SHIFT;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.CONTENT_URI;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.TABLE_NAME;

/**
 * Created by akatta on 3/30/17.
 */
public final class ShiftsProvider extends ContentProvider {
    //any number would do. But good to have numbers divisible by 100 for base paths
    private static final int MATCHER_SHIFT = 100;

    private static final UriMatcher sShiftsUriMatcher;
    private ShiftsOpenHelper mOpenHelper;

    static {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        // to match base URIs
        matcher.addURI(authority, PATH_SHIFT, MATCHER_SHIFT);

        sShiftsUriMatcher = matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ShiftsOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        // filter and match URIs
        switch (sShiftsUriMatcher.match(uri)) {
            case MATCHER_SHIFT: {
                cursor = mOpenHelper.getReadableDatabase().query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] contentValues) {
        int rowsInserted = 0;

        int match = sShiftsUriMatcher.match(uri);

        switch (match) {
            case MATCHER_SHIFT:
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                db.beginTransaction();
                try {
                    for (ContentValues value : contentValues) {
                        long id = db.insertOrThrow(TABLE_NAME, null, value);
                        if (id <= 0) {
                            throw new SQLException("Failed to insert row into " + uri);
                        }
                    }
                    getContext().getContentResolver().notifyChange(uri, null);
                    db.setTransactionSuccessful();
                    rowsInserted = contentValues.length;
                } finally {
                    db.endTransaction();
                }
                return rowsInserted;
            default:
                return super.bulkInsert(uri, contentValues);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // for the scope of this challenge, not required.
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = sShiftsUriMatcher.match(uri);
        switch (match) {
            case MATCHER_SHIFT:
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                int rowsUpdated = db.update(TABLE_NAME, values, selection, selectionArgs);
                if (rowsUpdated > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return rowsUpdated;
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            default:
                throw new UnsupportedOperationException("uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri insertedUri;
        int match = sShiftsUriMatcher.match(uri);
        switch (match) {
            case MATCHER_SHIFT:
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                long id = db.insert(TABLE_NAME, null, values);
                if (id > 0) {
                    insertedUri = ContentUris.withAppendedId(CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("uri: " + uri);
        }

        // notify listeners
        getContext().getContentResolver().notifyChange(uri, null);
        return insertedUri;
    }

    @Override
    public String getType(Uri uri) {
        // for this challenge, not required.
        return null;
    }

}
