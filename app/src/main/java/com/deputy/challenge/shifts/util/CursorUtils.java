package com.deputy.challenge.shifts.util;

import android.database.Cursor;

import com.deputy.challenge.shifts.data.model.Shift;

import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.*;

/**
 * Created by akatta on 3/30/17.
 */

public class CursorUtils {
    public static Shift getShift(Cursor cursor) {
        Shift shift = new Shift();
        shift.setId(cursor.getInt(cursor.getColumnIndex(_ID)));
        shift.setStartTimeStamp(cursor.getLong(cursor.getColumnIndex(COLUMN_START_TIMESTAMP)));
        shift.setEndTimestamp(cursor.getLong(cursor.getColumnIndex(COLUMN_END_TIMESTAMP)));
        shift.setStartLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_START_LATITUDE)));
        shift.setStartLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_START_LONGITUDE)));
        shift.setEndLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_END_LATITUDE)));
        shift.setEndLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_END_LONGITUDE)));
        shift.setImageURL(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL)));
        return shift;
    }
}
