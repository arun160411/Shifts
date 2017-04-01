package com.deputy.challenge.shifts.util;

import android.content.ContentValues;

import com.deputy.challenge.shifts.data.model.Shift;

import org.json.JSONException;
import org.json.JSONObject;

import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_END_LATITUDE;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_END_LONGITUDE;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_END_TIMESTAMP;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_IMAGE_URL;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_START_LATITUDE;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_START_LONGITUDE;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_START_TIMESTAMP;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable._ID;

public class JsonUtil {

    public static final String ID = "id";
    public static final String START_TIMESTAMP = "start";
    public static final String END_TIMESTAMP = "end";
    public static final String STARTLATITUDE = "startLatitude";
    public static final String STARTLONGITUDE = "startLongitude";
    public static final String ENDLATITUDE = "endLatitude";
    public static final String ENDLONGITUDE = "endLongitude";
    public static final String IMAGE_URL = "image";


    public static ContentValues getContentValuesFromShift(Shift shift){
        ContentValues cv = new ContentValues();
        cv.put(_ID, shift.getId());
        cv.put(COLUMN_START_TIMESTAMP, shift.getStartTimeStamp());
        cv.put(COLUMN_START_LATITUDE, shift.getStartLatitude());
        cv.put(COLUMN_START_LONGITUDE, shift.getStartLongitude());
        cv.put(COLUMN_END_TIMESTAMP, shift.getEndTimestamp());
        cv.put(COLUMN_END_LATITUDE, shift.getEndLatitude());
        cv.put(COLUMN_END_LONGITUDE, shift.getEndLongitude());
        cv.put(COLUMN_IMAGE_URL, shift.getImageURL());
        return cv;
    }

    public static Shift getShiftFromJson(JSONObject jsonObject)  throws JSONException {
        int id = jsonObject.getInt(ID);
        long start = TimeUtil.getMilliseconds(jsonObject.getString(START_TIMESTAMP));
        long end = TimeUtil.getMilliseconds(jsonObject.getString(END_TIMESTAMP));
        double startLatitude = (jsonObject.getString(STARTLATITUDE).isEmpty()) ? 0 : Double.parseDouble(jsonObject.getString(STARTLATITUDE)) ;
        double startLongitude = (jsonObject.getString(STARTLONGITUDE).isEmpty()) ? 0 : Double.parseDouble(jsonObject.getString(STARTLONGITUDE));

        double endLatitude = (jsonObject.getString(ENDLATITUDE).isEmpty()) ? 0 : Double.parseDouble(jsonObject.getString(ENDLATITUDE)) ;
        double endLongitude = (jsonObject.getString(ENDLONGITUDE).isEmpty()) ? 0 : Double.parseDouble(jsonObject.getString(ENDLONGITUDE));
        String image = jsonObject.getString(IMAGE_URL);

        Shift shift = new Shift();
        shift.setId(id);
        shift.setStartTimeStamp(start);
        shift.setEndTimestamp(end);
        shift.setStartLongitude(startLongitude);
        shift.setStartLatitude(startLatitude);
        shift.setEndLongitude(endLongitude);
        shift.setEndLatitude(endLatitude);
        shift.setImageURL(image);
        return shift;
    }

}
