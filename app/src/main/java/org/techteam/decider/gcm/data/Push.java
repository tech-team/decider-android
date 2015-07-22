package org.techteam.decider.gcm.data;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.techteam.decider.R;

public abstract class Push {
    private PushCode pushCode;

    public Push(PushCode pushCode) {
        this.pushCode = pushCode;
    }

    public PushCode getPushCode() {
        return pushCode;
    }

    public String getTitle(Context context) {
        return context.getString(R.string.app_name);
    }

    public abstract String getMessage(Context context);
    public abstract PendingIntent buildContentIntent(Context context);
}
