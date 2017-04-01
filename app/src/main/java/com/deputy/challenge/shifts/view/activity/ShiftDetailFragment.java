package com.deputy.challenge.shifts.view.activity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deputy.challenge.shifts.R;
import com.deputy.challenge.shifts.data.model.Shift;
import com.deputy.challenge.shifts.util.TimeUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.deputy.challenge.shifts.R.id.map;

/**
 * Created by akatta on 3/30/17.
 * A fragment representing a single Shift detail screen.
 * This fragment is either contained in a {@link ShiftListActivity}
 * in two-pane mode (on tablets) or a {@link ShiftDetailActivity}
 * on handsets.
 */
public class ShiftDetailFragment extends Fragment implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<ShiftDetailFragment.StartEndAddress> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final int LOADER_ID = 100;

    private Shift mShift;
    private GoogleMap mMap;
    private View mRootView;
    private Geocoder mGeocoder;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ShiftDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mShift = (Shift) getArguments().get(ARG_ITEM_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.shift_detail, container, false);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(map);
        supportMapFragment.getMapAsync(this);
        mRootView = rootView;
        return rootView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng start = new LatLng(mShift.getStartLatitude(), mShift.getStartLongitude());
        mMap.addMarker(new MarkerOptions().position(start).title("Start Shift"));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(start).zoom(14.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);
        if (mShift.getEndTimestamp() != 0) {
            LatLng end = new LatLng(mShift.getEndLatitude(), mShift.getEndLongitude());
            mMap.addMarker(new MarkerOptions().position(end).title("end Shift"));
            CameraPosition cameraPositionEnd = new CameraPosition.Builder().target(start).zoom(14.0f).build();
            CameraUpdate cameraUpdateEnd = CameraUpdateFactory.newCameraPosition(cameraPositionEnd);
            mMap.moveCamera(cameraUpdateEnd);
        }
    }


    public void setShift(Shift shift) {
        mShift = shift;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFragmentUI();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private void updateFragmentUI() {
        //TODO extract to member variables
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Shift Id: " + mShift.getId());
        ((TextView) mRootView.findViewById(R.id.shift_starttime)).setText(TimeUtil.convertTimeStampToGenericString(mShift.getStartTimeStamp()));
        ((TextView) mRootView.findViewById(R.id.shift_startaddress)).setText((mShift.getStartLatitude() + " , " + mShift.getStartLongitude()));
        if (mShift.getEndTimestamp() == 0) {
            ((TextView) mRootView.findViewById(R.id.shift_endtime)).setText(R.string.shift_in_progress);
            ((TextView) mRootView.findViewById(R.id.shift_endaddress)).setText(R.string.shift_in_progress);
        } else {
            ((TextView) mRootView.findViewById(R.id.shift_endtime)).setText(TimeUtil.convertTimeStampToGenericString(mShift.getEndTimestamp()));
            ((TextView) mRootView.findViewById(R.id.shift_endaddress)).setText((mShift.getEndLatitude() + " , " + mShift.getEndLongitude()));
        }
    }

    @Override
    public Loader<StartEndAddress> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<StartEndAddress>(getContext()) {

            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public StartEndAddress loadInBackground() {

                double startLatitude = mShift.getStartLatitude();
                double startLongitude = mShift.getStartLongitude();
                double endLatitude = mShift.getEndLatitude();
                double endLongitude = mShift.getEndLongitude();

                StartEndAddress startEndAddress = new StartEndAddress();

                startEndAddress.startAddress = (startLatitude != 0) ? getAddress(startLatitude, startLongitude) : startLatitude + " , " + startLongitude;
                startEndAddress.endAddress = (endLatitude != 0) ? getAddress(endLatitude, endLongitude) : getString(R.string.shift_in_progress);
                return startEndAddress;
            }
        };
    }

    static final class StartEndAddress {
        String startAddress;
        String endAddress;
    }

    private String getAddress(double latitude, double longitude) {
        if (mGeocoder == null) {
            mGeocoder = new Geocoder(getContext(), Locale.getDefault());
        }
        try {
            List<Address> addresses = mGeocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            return address + " " + city;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<StartEndAddress> loader, StartEndAddress data) {
        if (data.startAddress != null)
            ((TextView) mRootView.findViewById(R.id.shift_startaddress)).setText(data.startAddress);
        if (data.endAddress != null)
            ((TextView) mRootView.findViewById(R.id.shift_endaddress)).setText(data.endAddress);
    }


    @Override
    public void onLoaderReset(Loader loader) {

    }


}
