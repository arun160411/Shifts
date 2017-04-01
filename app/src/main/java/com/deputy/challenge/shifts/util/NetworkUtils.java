/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.deputy.challenge.shifts.util;


import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.deputy.challenge.shifts.R;
import com.deputy.challenge.shifts.data.model.Shift;
import com.deputy.challenge.shifts.http.VolleyRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static com.deputy.challenge.shifts.util.ShiftAppConstants.POST_LATITUDE_KEY;
import static com.deputy.challenge.shifts.util.ShiftAppConstants.POST_LONGITUDE_KEY;
import static com.deputy.challenge.shifts.util.ShiftAppConstants.POST_TIME_KEY;

/**
 * Created by akatta on 3/30/17.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();


    public static void postToApi(Context context, String url, Shift shift){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(POST_TIME_KEY, TimeUtil.convertTimeStampToISO_8601(shift.getStartTimeStamp()));
            jsonObject.put(POST_LATITUDE_KEY, shift.getStartLatitude());
            jsonObject.put(POST_LONGITUDE_KEY, shift.getStartLongitude());
        }catch(JSONException e){
            Log.e(TAG, context.getString(R.string.detail_activity_json_error), e);
        }
        //TODO not handling errors for now. need to handle.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, null, null){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return VolleyRequestQueue.getAuthHeader();
            }
        };
        jsonObjectRequest.setTag(TAG);
        VolleyRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }


}