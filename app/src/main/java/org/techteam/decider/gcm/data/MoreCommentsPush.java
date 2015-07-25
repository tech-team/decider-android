package org.techteam.decider.gcm.data;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.techteam.decider.R;
import org.techteam.decider.gui.activities.QuestionDetailsActivity;

public class MoreCommentsPush extends Push {

    private int questionId = -1;
    private int count = -1;

    public MoreCommentsPush(Bundle data) {
        super(PushCode.MORE_COMMENTS);
        String questionIdStr = data.getString("question_id");
        String countStr = data.getString("count");
        if (questionIdStr != null) {
            questionId = Integer.parseInt(questionIdStr);
        }
        if (countStr != null) {
            count = Integer.parseInt(countStr);
        }
    }

    @Override
    public String getTitle(Context context) {
        return super.getTitle(context);
    }

    @Override
    public String getMessage(Context context) {
        return String.format(context.getString(R.string.push_more_comments), count);
    }

    @Override
    public PendingIntent buildContentIntent(Context context) {
        if (questionId != -1) {
            Intent intent = new Intent(context, QuestionDetailsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(QuestionDetailsActivity.IntentExtras.Q_ID, questionId);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            return pendingIntent;
        }
        return null;
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getCount() {
        return count;
    }
}
