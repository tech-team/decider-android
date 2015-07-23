package org.techteam.decider.gcm.data;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.techteam.decider.R;
import org.techteam.decider.gui.activities.QuestionDetailsActivity;

public class NewCommentPush extends Push {

    private int questionId;
    private int commentId;

    public NewCommentPush(Bundle data) {
        super(PushCode.NEW_COMMENT);
        questionId = Integer.parseInt(data.getString("question_id"));
        commentId = Integer.parseInt(data.getString("comment_id"));
    }

    @Override
    public String getTitle(Context context) {
        return super.getTitle(context);
    }

    @Override
    public String getMessage(Context context) {
        return "New comment on your question!";
    }

    @Override
    public PendingIntent buildContentIntent(Context context) {
        Intent intent = new Intent(context, QuestionDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(QuestionDetailsActivity.IntentExtras.Q_ID, questionId);
        intent.putExtra(QuestionDetailsActivity.IntentExtras.COMMENT_ID, commentId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        return pendingIntent;
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getCommentId() {
        return commentId;
    }
}
