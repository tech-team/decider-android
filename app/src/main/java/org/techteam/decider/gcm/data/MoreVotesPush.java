package org.techteam.decider.gcm.data;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.techteam.decider.R;
import org.techteam.decider.gui.activities.QuestionDetailsActivity;

public class MoreVotesPush extends Push {

    private int questionId;
    private int count;

    public MoreVotesPush(Bundle data) {
        super(PushCode.MORE_VOTES);
        questionId = Integer.parseInt(data.getString("question_id"));
        count = Integer.parseInt(data.getString("count"));
    }

    @Override
    public String getTitle(Context context) {
        return super.getTitle(context);
    }

    @Override
    public String getMessage(Context context) {
        return String.format(context.getString(R.string.push_more_votes), count);
    }

    @Override
    public PendingIntent buildContentIntent(Context context) {
        Intent intent = new Intent(context, QuestionDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(QuestionDetailsActivity.IntentExtras.Q_ID, questionId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        return pendingIntent;
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getCount() {
        return count;
    }
}
