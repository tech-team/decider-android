package org.techteam.decider.util;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class ServicesChecker {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public enum CheckerResult {
        OK,
        NOT_OK,
        NOT_SUPPORTED
    }

    public static CheckerResult checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                return CheckerResult.NOT_OK;
            } else {
                return CheckerResult.NOT_SUPPORTED;
            }
        }
        return CheckerResult.OK;
    }
}
