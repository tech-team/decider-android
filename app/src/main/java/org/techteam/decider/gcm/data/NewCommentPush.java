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
        questionId = data.getInt("question_id", -1);
        commentId = data.getInt("comment_id", -1);
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
