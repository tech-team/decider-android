package org.techteam.decider.gcm;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

public class DeciderGcmListenerService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
    }

    @Override
    public void onMessageSent(String msgId) {
        super.onMessageSent(msgId);
    }
}
