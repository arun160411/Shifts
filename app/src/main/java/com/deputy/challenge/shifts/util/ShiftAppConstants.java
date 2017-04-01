package com.deputy.challenge.shifts.util;

/**
 * Created by akatta on 4/1/17.
 */
public class ShiftAppConstants {
    public static final String POST_TIME_KEY = "time";
    public static final String POST_LATITUDE_KEY = "latitude";
    public static final String POST_LONGITUDE_KEY = "longitude";

    public static final String HEADER_AUTHORIZATION_VALUE = "Deputy 75c60816dcee56f6eb0055ee97083f1c895c776b";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static class URLConstants{
        public static final String ROOT_URL = "https://apjoqdqpi3.execute-api.us-west-2.amazonaws.com/dmc";
        public static final String SHIFTS_URL = ROOT_URL + "/shifts";

        public static final String BUSINESS_URL = ROOT_URL + "/business";
        public static final String START_SHIFT_URL = ROOT_URL + "/shift/start";
        public static final String END_SHIFT_URL = ROOT_URL + "/shift/end";
    }
}
