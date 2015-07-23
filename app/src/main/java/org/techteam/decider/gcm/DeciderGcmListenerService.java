package org.techteam.decider.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.techteam.decider.R;
import org.techteam.decider.gcm.data.Push;
import org.techteam.decider.gcm.data.PushParser;
import org.techteam.decider.gui.activities.MainActivity;

public class DeciderGcmListenerService extends GcmListenerService {
    private static final String TAG = DeciderGcmListenerService.class.getName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.i(TAG, "From: " + from);
        Log.i(TAG, "Message: " + data.toString());

        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        Push push = PushParser.parse(data);
        sendNotification(push);
    }

    private void sendNotification(Push push) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(push.getTitle(this))
                .setContentText(push.getMessage(this))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(push.buildContentIntent(this));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
