package com.deputy.challenge.shifts.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;

/**
 * Created by akatta on 3/30/17.
 */
public class PermissionsUtil {

    public static final int REQUEST_CODE_LOCATION = 10;
    private static final String SHIFTS_PREFS = "Shifts_Pref";
    private static final String HAS_LOCATION_PERMISSION = "location_permission_granted";

    /**
     * Different libraries needs runtime permissions to handle
     */
    public enum Permission {
        LOCATION
    }


    public enum Status {
        ALLOWED,
        REQUESTED,
        NEVERASK,
        DENIED
    }


    public static Status hasPermission(final Activity context, Permission type) {
        switch (type) {

            case LOCATION:
                return hasPermissionForLocation(context);

            default:
                return Status.ALLOWED;
        }
    }


    private static Status hasPermissionForLocation(Activity context) {

        // When the requested feature has granted permission. The calling method will handle the positive path.
        if (hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return Status.ALLOWED;
        }

        //Gets whether you should show UI with Never ask again for requesting a permission.
        boolean isRationaleFlow = ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_COARSE_LOCATION);

        // If the user has denied the permission for very first time or consecutive times without
        // opting for "Never ask again".
        // This block has the responsibility to request the permission and return the REQUESTED status.
        if (isRationaleFlow) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION);

            return Status.REQUESTED;

        } else {
            // Display the permission dialog for very first time
            // This block has the responsibility to request the permission and return the REQUESTED status.
            if (!hasPermissionBeenRequested(context, Permission.LOCATION.name())) {
                setPermissionBeenRequested(context, Permission.LOCATION.name(), true);

                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION);

                return Status.REQUESTED;
            }
        }

        // If the user has denied the permission with opting for "Never ask again".
        // The calling method should handle this flow.
        return Status.NEVERASK;
    }

    private static void setPermissionBeenRequested(Activity context, String name, boolean b) {
        SharedPreferences settings = context.getSharedPreferences(SHIFTS_PREFS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(HAS_LOCATION_PERMISSION, b);
        editor.commit();
    }

    private static boolean hasPermissionBeenRequested(Activity context, String name) {
        SharedPreferences settings = context.getSharedPreferences(SHIFTS_PREFS,
                Context.MODE_PRIVATE);
        return settings.getBoolean(HAS_LOCATION_PERMISSION, false);
    }


    /**
     * Check for the API level > 23(Android M) and then check whether the application has the requested permission or not.
     *
     * @param context    : Activity reference to check permission
     * @param permission : Requested permission for example {@link Manifest.permission#CAMERA},
     *                   {@link Manifest.permission#READ_PHONE_STATE}, {@link Manifest.permission#MODIFY_AUDIO_SETTINGS}
     * @return True if has permission or API < 23 else return false
     */
    private static boolean hasPermission(Activity context, String permission) {

        boolean apiAboveMaarshMellow = !isAPIAboveMarshmallow();

        if (context != null && !context.isFinishing()) {
            return apiAboveMaarshMellow || (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
        } else {
            return apiAboveMaarshMellow;
        }

    }

    /**
     * @return True if the API level > 23(Android M) else return false
     */
    public static boolean isAPIAboveMarshmallow() {
        return (Build.VERSION.SDK_INT >= 23);
    }




    public static void showSnakbar(Activity context, int rootId, int snackBarMsgResId,
                                   final int mimicTextResId, boolean isDisplaySettingsBar, int duration) {
        if (rootId > 0) {
            final View root = context.findViewById(rootId);
            String snackBarMsg = context.getString(snackBarMsgResId);
                showInfoSnakbar(root, snackBarMsg, duration);
            }

    }

    private static void showInfoSnakbar(View view, String snackBarMsg, int duration) {
        Snackbar snackbar = Snackbar.make(view, snackBarMsg, Snackbar.LENGTH_LONG);

        // The snackbar will dismiss automatically after 10 seconds
        snackbar.setDuration(duration);

        View snackbarView = snackbar.getView();
        // Default line support is 2 for snackbar. Overriding it to show the full text by making it 3 line.
        if (snackbarView != null) {
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            //Set the max lines for textview to show multiple lines
            textView.setMaxLines(3);
        }
        snackbar.show();
    }


}