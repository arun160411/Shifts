package com.deputy.challenge.shifts.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.*;

/**
 * Created by akatta on 3/30/17.
 */
public final class ShiftsOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "shifts.db";
    private static final int DATABASE_VERSION = 1;

    public ShiftsOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_STATEMENT_CREATE_SHIFT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ignoring for this challenge.
        // else we would need to do update the schema / recreate table / similar sorts.
    }
}
