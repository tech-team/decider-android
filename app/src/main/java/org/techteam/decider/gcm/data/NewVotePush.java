package org.techteam.decider.gcm.data;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.techteam.decider.gui.activities.QuestionDetailsActivity;

public class NewVotePush extends Push {

    private int questionId;

    public NewVotePush(Bundle data) {
        super(PushCode.NEW_VOTE);
        questionId = Integer.parseInt(data.getString("question_id"));
    }

    @Override
    public String getTitle(Context context) {
        return super.getTitle(context);
    }

    @Override
    public String getMessage(Context context) {
        return "New votes on your question!";
    }

    @Override
    public PendingIntent buildContentIntent(Context context) {
        Intent intent = new Intent(context, QuestionDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        return pendingIntent;
    }

    public int getQuestionId() {
        return questionId;
    }
}
