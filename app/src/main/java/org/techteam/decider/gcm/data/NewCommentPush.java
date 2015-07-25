package org.techteam.decider.gcm.data;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.techteam.decider.R;
import org.techteam.decider.gui.activities.QuestionDetailsActivity;

public class NewCommentPush extends Push {

    private int questionId = -1;
    private int commentId = -1;

    public NewCommentPush(Bundle data) {
        super(PushCode.NEW_COMMENT);
        String questionIdStr = data.getString("question_id");
        String commentIdStr = data.getString("comment_id");
        if (questionIdStr != null) {
            questionId = Integer.parseInt(questionIdStr);
        }
        if (commentIdStr != null) {
            commentId = Integer.parseInt(commentIdStr);
        }
    }

    @Override
    public String getTitle(Context context) {
        return super.getTitle(context);
    }

    @Override
    public String getMessage(Context context) {
        return context.getString(R.string.push_new_comment);
    }

    @Override
    public PendingIntent buildContentIntent(Context context) {
        if (questionId != -1) {
            Intent intent = new Intent(context, QuestionDetailsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(QuestionDetailsActivity.IntentExtras.Q_ID, questionId);
            if (commentId != -1) {
                intent.putExtra(QuestionDetailsActivity.IntentExtras.COMMENT_ID, commentId);
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            return pendingIntent;
        } else {
            return null;
        }
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getCommentId() {
        return commentId;
    }
}
