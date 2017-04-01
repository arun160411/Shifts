package com.deputy.challenge.shifts.data.contract;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by akatta on 3/30/17.
 */

//Class not to be extended.
public final class ShiftsContract {


    public static final String CONTENT_SCHEME = "content://";
    public static final String CONTENT_AUTHORITY = "com.deputy.challenge.shifts";
    public static final String PATH_SHIFT = "shifts";
    public static final Uri CORE_CONTENT_URI = Uri.parse(CONTENT_SCHEME + CONTENT_AUTHORITY);


    // We don't want this class to be instantiated, even by chance.
    private ShiftsContract() {

    }

    /**
     * Class to represent Shifts table.
     * <p>
     * start and end time of shifts are represented as TIMESTAMPS to avoid TIMEZONE issues.
     */
    public static final class ShiftsTable implements BaseColumns {
        public static final Uri CONTENT_URI =
                CORE_CONTENT_URI.buildUpon().appendPath(PATH_SHIFT).build();

        //table name for shifts
        public static final String TABLE_NAME = "shift";

        //columns of the table - primary key column _ID coming from BaseColumns interface
        public static final String COLUMN_START_TIMESTAMP = "start_timestamp";
        public static final String COLUMN_END_TIMESTAMP = "end_timestamp";
        public static final String COLUMN_START_LATITUDE = "start_latitude";
        public static final String COLUMN_START_LONGITUDE = "start_longitude";
        public static final String COLUMN_END_LATITUDE = "end_latitude";
        public static final String COLUMN_END_LONGITUDE = "end_longitude";
        public static final String COLUMN_IMAGE_URL = "image_url";

        public static final String SQL_STATEMENT_CREATE_SHIFT_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_START_TIMESTAMP + " INTEGER NOT NULL," +
                        COLUMN_END_TIMESTAMP + " INTEGER ," +
                        COLUMN_START_LATITUDE + " TEXT NOT NULL," +
                        COLUMN_START_LONGITUDE + " TEXT NOT NULL," +
                        COLUMN_END_LATITUDE + " TEXT ," +
                        COLUMN_END_LONGITUDE + " TEXT ," +
                        COLUMN_IMAGE_URL + " TEXT);";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }


}
