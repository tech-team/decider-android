package org.techteam.decider.gcm.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.techteam.decider.R;

public class PushParser {
    public static Push parse(Context context, Bundle data) {
        int code = Integer.parseInt(data.getString("code"));
        PushCode pushCode = PushCode.fromCode(code);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean allowComments = prefs.getBoolean(context.getString(R.string.pref_notification_comments_key), false);
        boolean allowVotes = prefs.getBoolean(context.getString(R.string.pref_notification_votes_key), false);
        Push push = null;
        switch (pushCode) {
            case DEBUG:
                push = new DebugPush(data);
                break;
            case NEW_COMMENT:
                if (allowComments) {
                    push = new NewCommentPush(data);
                }
                break;
            case NEW_COMMENT_LIKE:
                break;
            case NEW_VOTE:
                if (allowVotes) {
                    push = new NewVotePush(data);
                }
                break;
            case MORE_COMMENTS:
                if (allowComments) {
                    push = new MoreCommentsPush(data);
                }
                break;
            case MORE_VOTES:
                if (allowVotes) {
                    push = new MoreVotesPush(data);
                }
                break;
        }

        return push;
    }
}
