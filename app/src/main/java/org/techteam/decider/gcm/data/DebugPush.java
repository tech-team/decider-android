package org.techteam.decider.gcm.data;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.techteam.decider.R;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.activities.QuestionDetailsActivity;

public class DebugPush extends Push {

    private String title;
    private String msg;

    public DebugPush(Bundle data) {
        super(PushCode.DEBUG);
        title = data.getString("title");
        msg = data.getString("msg");
    }

    @Override
    public String getTitle(Context context) {
        return title;
    }

    @Override
    public String getMessage(Context context) {
        return msg;
    }

    @Override
    public PendingIntent buildContentIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        return pendingIntent;
    }


}
