package com.deputy.challenge.shifts.http;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import static com.deputy.challenge.shifts.util.ShiftAppConstants.HEADER_AUTHORIZATION;
import static com.deputy.challenge.shifts.util.ShiftAppConstants.HEADER_AUTHORIZATION_VALUE;

/**
 * Created by akatta on 3/30/17.
 */
public final class VolleyRequestQueue  {



    private static volatile VolleyRequestQueue mInstance ;
    private RequestQueue mRequestQueue;
    private Context mContext;
    private static final Map<String, String> HEADERS;
    static {
        HEADERS = new HashMap<String, String>();
        HEADERS.put(HEADER_AUTHORIZATION, HEADER_AUTHORIZATION_VALUE);
    }

    private VolleyRequestQueue(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
    }


    public static VolleyRequestQueue getInstance(Context context) {
        if (mInstance == null) {
            synchronized (VolleyRequestQueue.class) {
                if (mInstance == null) {
                    mInstance = new VolleyRequestQueue(context);
                }
            }
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


    public static Map<String, String> getAuthHeader(){
        return HEADERS;
    }
}
