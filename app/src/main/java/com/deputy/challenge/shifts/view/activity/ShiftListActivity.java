package com.deputy.challenge.shifts.view.activity;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.deputy.challenge.shifts.R;
import com.deputy.challenge.shifts.adapter.ShiftsDataAdapter;
import com.deputy.challenge.shifts.application.ShiftsApplication;
import com.deputy.challenge.shifts.data.contract.ShiftsContract;
import com.deputy.challenge.shifts.data.model.Shift;
import com.deputy.challenge.shifts.intentservice.ShiftIntentService;
import com.deputy.challenge.shifts.util.NetworkUtils;
import com.deputy.challenge.shifts.util.PermissionsUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static com.deputy.challenge.shifts.R.id.fab;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_END_LATITUDE;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_END_LONGITUDE;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_END_TIMESTAMP;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_IMAGE_URL;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_START_LATITUDE;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_START_LONGITUDE;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.COLUMN_START_TIMESTAMP;
import static com.deputy.challenge.shifts.data.contract.ShiftsContract.ShiftsTable.CONTENT_URI;
import static com.deputy.challenge.shifts.util.PermissionsUtil.REQUEST_CODE_LOCATION_ENDSHIFT;
import static com.deputy.challenge.shifts.util.PermissionsUtil.REQUEST_CODE_LOCATION_STARTSHIFT;
import static com.deputy.challenge.shifts.util.PermissionsUtil.Status.NEVERASK;
import static com.deputy.challenge.shifts.util.PermissionsUtil.Status.REQUESTED;
import static com.deputy.challenge.shifts.util.ShiftAppConstants.URLConstants.END_SHIFT_URL;
import static com.deputy.challenge.shifts.util.ShiftAppConstants.URLConstants.START_SHIFT_URL;

/**
 * Created by akatta on 3/30/17.
 * <p>
 * An activity representing a list of Shifts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ShiftDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ShiftListActivity extends AppCompatActivity implements ShiftsDataAdapter.ShiftsItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final String TAG = ShiftListActivity.class.getSimpleName();
    private static final int ID_SHIFTS_LOADER = 1;
    private static final String LOCATION_KEY = "LOCATION";
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "REQUEST_LOCATION_UPDATES";
    public static final String WHERE = ShiftsContract.ShiftsTable._ID + "= ?";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private ProgressBar mLoadingIndicator;
    private RecyclerView mRecyclerView;

    private int mRVPosition = RecyclerView.NO_POSITION;

    private ShiftsDataAdapter mShiftsDataAdapter;

    // Sort order:  Descending, by date.
    private static final String SORT_ORDER = COLUMN_START_TIMESTAMP + " DESC";

    private static final String LOADED_FROM_NETWORK = "LOADED_FROM_NETWORK";
    private static final String RECYCLER_VIEW_CURRENT_POSITION = "RV_CURRENT_POSITION";

    private boolean mRequestingLocationUpdates = false;
    private boolean mLoadedDataFromNetwork;

    private LocationRequest mLocationRequest;
    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;

    private boolean mClickHandled;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractBundle(savedInstanceState);
        loadPreferences(savedInstanceState);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter(ShiftIntentService.BROADCAST_FINISHED));
        setContentView(R.layout.activity_shift_list);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);


        mRecyclerView = (RecyclerView) findViewById(R.id.shift_list);

        mShiftsDataAdapter = new ShiftsDataAdapter(getApplicationContext(), this);
        setupRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mShiftsDataAdapter);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        mFab = (FloatingActionButton) findViewById(fab);

        // Basic start and stop shift. looking for tag of FAB to decide on whether to stop or start a shift
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer tag = (Integer) mFab.getTag();
                switch (tag.intValue()) {
                    case android.R.drawable.checkbox_on_background:
                        handleFabButtonStartEndShift(false);
                        break;
                    case android.R.drawable.ic_input_add:
                        handleFabButtonStartEndShift(true);
                        break;
                }
            }
        });
        showLoading();

        if (findViewById(R.id.shift_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }


    }

    /**
     * checks for permissions to show user a proper snackBar.
     */
    private void handleFabButtonStartEndShift(boolean startShift) {
        PermissionsUtil.Status cameraStatus = PermissionsUtil.hasPermission(this, PermissionsUtil.Permission.LOCATION,startShift);
        if (cameraStatus == PermissionsUtil.Status.ALLOWED) {
            handlePermissionResult(PermissionsUtil.Permission.LOCATION.ordinal(), PermissionsUtil.Status.ALLOWED,startShift);
        } else if (cameraStatus == REQUESTED) {
            handlePermissionResult(PermissionsUtil.Permission.LOCATION.ordinal(), REQUESTED,startShift);
        } else if (cameraStatus == NEVERASK) {
            handlePermissionResult(PermissionsUtil.Permission.LOCATION.ordinal(), NEVERASK,startShift);
        }
    }

    /**
     * register for location updates
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mLoadedDataFromNetwork && ((ShiftsApplication)getApplication()).isServiceStopped()) {
            getSupportLoaderManager().initLoader(ID_SHIFTS_LOADER, null, ShiftListActivity.this);
        }
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void extractBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLoadedDataFromNetwork = savedInstanceState.getBoolean(LOADED_FROM_NETWORK, false);
            mRVPosition = savedInstanceState.getInt(RECYCLER_VIEW_CURRENT_POSITION, 0);
            mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY, false);
            mLocation = savedInstanceState.getParcelable(LOCATION_KEY);
        }
    }

    @Override
    public Loader onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case ID_SHIFTS_LOADER:
                return new CursorLoader(this, ShiftsContract.ShiftsTable.CONTENT_URI, null, null, null, SORT_ORDER);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mShiftsDataAdapter.swapCursor(cursor);
        if (mRVPosition == RecyclerView.NO_POSITION) mRVPosition = 0;
        mRecyclerView.smoothScrollToPosition(mRVPosition);
        showShiftsDataView();
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "Welcome to Shifts. Please start adding your shifts", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mShiftsDataAdapter.swapCursor(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(LOADED_FROM_NETWORK, mLoadedDataFromNetwork);
        int currentVisiblePosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        outState.putInt(RECYCLER_VIEW_CURRENT_POSITION, currentVisiblePosition);
        super.onSaveInstanceState(outState);
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void showShiftsDataView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mFab.setVisibility(View.VISIBLE);
        setupFABstatus();
    }

    private void setupFABstatus() {
        // see if the first item is in progress
        Shift firstShift = ((ShiftsDataAdapter) mRecyclerView.getAdapter()).getItemAt(0);

        if (firstShift != null) {
            if (firstShift.getEndTimestamp() == 0) {
                // there is a Shift in progress. Show "done" shift icon
                mFab.setImageResource(android.R.drawable.checkbox_on_background);
                mFab.setTag(android.R.drawable.checkbox_on_background);
            } else {
                // show start icon
                mFab.setImageResource(android.R.drawable.ic_input_add);
                mFab.setTag(android.R.drawable.ic_input_add);
            }
        } else {
            mFab.setImageResource(android.R.drawable.ic_input_add);
            mFab.setTag(android.R.drawable.ic_input_add);
        }
    }

    private void showLoading() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mFab.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onListItemClick(Shift shift) {
        handleShiftDetails(shift);
    }

    private void handleShiftDetails(Shift shift) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(ShiftDetailFragment.ARG_ITEM_ID, shift);
            Fragment detailsFragment = getSupportFragmentManager().findFragmentByTag("SHIFT_DETAILS");
            ShiftDetailFragment shiftDetailFragment;
            if (detailsFragment != null) {
                shiftDetailFragment = (ShiftDetailFragment) detailsFragment;
                shiftDetailFragment.setShift(shift);
            } else {
                shiftDetailFragment = new ShiftDetailFragment();
                shiftDetailFragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.shift_detail_container, shiftDetailFragment)
                        .commit();
            }

        } else {
            Intent intent = new Intent(this, ShiftDetailActivity.class);
            intent.putExtra(ShiftDetailFragment.ARG_ITEM_ID, shift);
            startActivity(intent);
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mLoadedDataFromNetwork = true;
            getSupportLoaderManager().initLoader(ID_SHIFTS_LOADER, null, ShiftListActivity.this);
        }
    };


    private void savePreferences(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOADED_FROM_NETWORK, mLoadedDataFromNetwork);
        editor.commit();
    }

    private void loadPreferences(Bundle bundle){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        mLoadedDataFromNetwork = sharedPreferences.getBoolean(LOADED_FROM_NETWORK, false);
    }



    private void handlePermissionResult(int permissionCode, PermissionsUtil.Status permissionStatus, boolean start) {

        switch (permissionStatus) {
            case ALLOWED:
                if (mGoogleApiClient == null) {
                    setupLocationService();
                }
                if (mLocation != null) {
                    if(start) {
                        Shift shift = createShiftAndUpdateServer(mLocation);
                    }else{
                        Shift latestShift = ((ShiftsDataAdapter) mRecyclerView.getAdapter()).getItemAt(0);
                        endShiftAndUpdateServer(latestShift);
                    }
                }
                break;

            case NEVERASK:
                attachSnackBar(R.string.location_permission_rational_message, R.string.location_permission_mimic, true);
                break;

            case DENIED:
                attachSnackBar(R.string.location_permission_deny_message, -1, false);

                break;
            default:
                break;
        }
    }

    private Shift createShiftAndUpdateServer(Location location) {
        long currentTimeMillis = System.currentTimeMillis();
        Shift shift = new Shift();
        shift.setStartLatitude(location.getLatitude());
        shift.setStartLongitude(location.getLongitude());
        shift.setStartTimeStamp(currentTimeMillis);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_START_TIMESTAMP, currentTimeMillis);
        cv.put(COLUMN_START_LATITUDE, location.getLatitude());
        cv.put(COLUMN_START_LONGITUDE, location.getLongitude());
        cv.put(COLUMN_IMAGE_URL, "https://unsplash.it/500/500/?random");
        // Doing it Sync as we want the UI to be updated before user can end the shift.
        Uri contentUri = getContentResolver().insert(CONTENT_URI, cv);
        shift.setId(Integer.valueOf(contentUri.getPathSegments().get(1)));
        NetworkUtils.postToApi(this, START_SHIFT_URL, shift);
        Toast.makeText(getApplicationContext(),"Shift started and is at the top of the list",Toast.LENGTH_SHORT).show();
        return shift;
    }

    private Shift endShiftAndUpdateServer(Shift shift) {
        long currentTimeMillis = System.currentTimeMillis();
        shift.setEndLatitude(mLocation.getLatitude());
        shift.setEndLongitude(mLocation.getLongitude());
        shift.setEndTimestamp(currentTimeMillis);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_END_TIMESTAMP, shift.getEndTimestamp());
        cv.put(COLUMN_END_LATITUDE, shift.getEndLatitude());
        cv.put(COLUMN_END_LONGITUDE, shift.getEndLongitude());
        // Doing it Sync as we want the UI to be updated before user can start a new shift.
        getContentResolver().update(CONTENT_URI, cv, WHERE, new String[]{String.valueOf(shift.getId())});
        NetworkUtils.postToApi(this, END_SHIFT_URL, shift);
        Toast.makeText(getApplicationContext(),"Shift Ended",Toast.LENGTH_SHORT).show();
        return shift;
    }


    private void attachSnackBar(int snackBarMsgResId, final int mimicTextResId, boolean isDisplaySettingsBar) {
        showPermissionSnackBar(snackBarMsgResId, mimicTextResId, isDisplaySettingsBar,
                3000);
    }

    public void showPermissionSnackBar(int snackBarMsgResId, final int mimicTextResId,
                                       boolean isDisplaySettingsBar, int duration) {
        PermissionsUtil.showSnakbar(this, R.id.list, snackBarMsgResId, mimicTextResId, isDisplaySettingsBar, duration);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_STARTSHIFT:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    handlePermissionResult(PermissionsUtil.Permission.LOCATION.ordinal(), PermissionsUtil.Status.ALLOWED,true);
                } else {
                    attachSnackBar(R.string.location_permission_deny_message, -1, false);
                }
                return;

            case REQUEST_CODE_LOCATION_ENDSHIFT:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    handlePermissionResult(PermissionsUtil.Permission.LOCATION.ordinal(), PermissionsUtil.Status.ALLOWED,false);
                } else {
                    attachSnackBar(R.string.location_permission_deny_message, -1, false);
                }
                return;

        }
    }

    // log off from location updates.

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }


    @Override
    protected void onStart() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        savePreferences();
        super.onStop();
    }


    private void setupLocationService() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        if (googleAPI.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();

        } else {
            Log.e(TAG, "google play services not connected");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mRequestingLocationUpdates = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection to play service suspended error code: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Connection Failed.
        Log.e(TAG, "Connection to play service failed error: " + connectionResult.getErrorMessage());

    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        if (!mClickHandled) {
            mClickHandled = true;
            Shift firstShift = ((ShiftsDataAdapter) mRecyclerView.getAdapter()).getItemAt(0);
            if (firstShift != null) {
                if (firstShift.getEndTimestamp() != 0) {
                    createShiftAndUpdateServer(mLocation);
                } else {
                    endShiftAndUpdateServer(firstShift);
                }
            } else {
                createShiftAndUpdateServer(mLocation);
            }
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }
}
