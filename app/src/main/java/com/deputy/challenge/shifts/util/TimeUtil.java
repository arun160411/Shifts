package com.deputy.challenge.shifts.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.deputy.challenge.shifts.util.TimeUtil.DATELABLE.DATE_LABEL_OLDER;
import static com.deputy.challenge.shifts.util.TimeUtil.DATELABLE.DATE_LABEL_THIS_MONTH;
import static com.deputy.challenge.shifts.util.TimeUtil.DATELABLE.DATE_LABEL_TODAY;
import static com.deputy.challenge.shifts.util.TimeUtil.DATELABLE.DATE_LABEL_YESTERDAY;

/**
 * Created by akatta on 3/30/17.
 */
public class TimeUtil {
    private static SimpleDateFormat sdf;
    private static final long One_Day_In_Milliseconds = 86400000;
    private static final String DATE_FORMAT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ";
    private static final String FORMAT_TIME_STAMP = "EEEE,  yyyy-MM-dd h:mm a";

    private static final String FORMAT_DAY = "EEEE,dd/MMM";

    public static DATELABLE getDateLabel(long timeInMillis) {
        long nowTime = System.currentTimeMillis();
        long quotient = (nowTime - timeInMillis) / One_Day_In_Milliseconds;

        if (quotient < 1) {
            return DATE_LABEL_TODAY;
        } else if (quotient < 2) {
            return DATE_LABEL_YESTERDAY;
        } else if (quotient < 30) {
            return DATE_LABEL_THIS_MONTH;
        } else {
            return DATE_LABEL_OLDER;
        }
    }

    public static String convertTimeStampToGenericString(long timestamp){


        return new SimpleDateFormat(FORMAT_TIME_STAMP).format(timestamp);
    }

    public static String convertTimeStampToDayString(long timestamp){


        return new SimpleDateFormat(FORMAT_DAY).format(timestamp);
    }

    public static String convertTimeStampToISO_8601(long timestamp){
        return new SimpleDateFormat(DATE_FORMAT_ISO_8601).format(timestamp);
    }
    public static final long getMilliseconds(String time) {
        if (time == null || time.length()==0) {
            return 0;
        }
        try {
            return new SimpleDateFormat(DATE_FORMAT_ISO_8601).parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;

    }

    public static enum DATELABLE{
        DATE_LABEL_TODAY,
        DATE_LABEL_YESTERDAY,
        DATE_LABEL_THIS_MONTH,
        DATE_LABEL_OLDER
    }





}
