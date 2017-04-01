package com.deputy.challenge.shifts.intentservice;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.deputy.challenge.shifts.http.VolleyRequestQueue;
import com.deputy.challenge.shifts.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.android.volley.Request.Method.GET;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.CONTENT_URI;
import static com.deputy.challenge.shifts.util.ShiftAppConstants.URLConstants.SHIFTS_URL;

/**
 * Created by akatta on 3/30/17.
 *
 *
 *
 * Very basic Sync.
 *
 * the full solution to effective synching would involve changing the server APIs as well.
 * Hence chose to do only pull sync at this point of time.
 *
 *
 *
 */

public class ShiftIntentService extends IntentService implements Response.Listener, Response.ErrorListener {

    private static final String TAG = ShiftIntentService.class.getSimpleName();
    public static final String BROADCAST_FINISHED = "SYNC_COMPLETED";

    public ShiftIntentService() {
        super("ShiftIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        Cursor cursor = null;
        try {
            JSONArray shiftsArray = getShiftsFromServer();
            if(shiftsArray!=null) {
                cursor = getContentResolver().query(CONTENT_URI, null, null, null, null, null);
                Sync(cursor, shiftsArray);
            }
            sendBroadcast();
        } catch (JSONException e) {
            Log.e(TAG, "Unable to convert response to JSON", e);
            sendBroadcast();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private JSONArray getShiftsFromServer() throws JSONException {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest shiftsRequest = new StringRequest(GET, SHIFTS_URL, future, future) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return VolleyRequestQueue.getAuthHeader();
            }
        };
        VolleyRequestQueue.getInstance(getApplicationContext()).addToRequestQueue(shiftsRequest);
        try {
            String shiftsArray = future.get();
            if (shiftsArray != null) {
                return new JSONArray(shiftsArray);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, e.getLocalizedMessage());
        } catch (ExecutionException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        //probably no network
        sendBroadcast();
    }

    @Override
    public void onResponse(Object response) {
        sendBroadcast();
    }

    private void Sync(Cursor cursor, JSONArray jsonArray) throws JSONException {

        ContentValues[] shiftsContentValues = new ContentValues[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            shiftsContentValues[i] = JsonUtil.getContentValuesFromShift(JsonUtil.getShiftFromJson(jsonObject));
        }

        if (cursor.getCount() == 0) { // no DB data
            getContentResolver().bulkInsert(CONTENT_URI, shiftsContentValues);
        } else { // preexisitng data in DB. Insert missing data
            for (int i = 0; i < shiftsContentValues.length; i++) {
                if (cursor.moveToPosition(i) == false) {
                    Uri uri = getContentResolver().insert(CONTENT_URI, shiftsContentValues[i]);
                    if (uri == null) {
                        Log.e(TAG, "Unable to insert row.");
                    }
                }
            }
        }
    }

    private void sendBroadcast() {
        Intent intent = new Intent(BROADCAST_FINISHED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
