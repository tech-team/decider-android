package org.techteam.decider.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import de.greenrobot.event.EventBus;

public class NetworkStateReceiver extends BroadcastReceiver {
    public static final String TAG = NetworkStateReceiver.class.getName();
    private static boolean firstConnect = true;

    private EventBus eventBus = EventBus.getDefault();

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Network connectivity change");
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null) {
            if(firstConnect) {
                Log.i(TAG, "Network " + activeNetInfo.getTypeName() + " connected");
                firstConnect = false;
                eventBus.post(new NetworkIsUpEvent());
            }
        }
        else {
            firstConnect = true;
            Log.d(TAG, "There's no network connectivity");
        }
    }

    public static class NetworkIsUpEvent {

    }
}