package org.techteam.decider.gcm.data;

import android.os.Bundle;

public class PushParser {
    public static Push parse(Bundle data) {
        int code = Integer.parseInt(data.getString("code"));
        PushCode pushCode = PushCode.fromCode(code);

        Push push = null;
        switch (pushCode) {
            case NEW_COMMENT:
                push = new NewCommentPush(data);
                break;
            case NEW_COMMENT_LIKE:
                break;
            case NEW_VOTE:
                push = new NewVotePush(data);
                break;
            case MORE_COMMENTS:
                push = new MoreCommentsPush(data);
                break;
            case MORE_VOTES:
                push = new MoreVotesPush(data);
                break;
        }

        return push;
    }
}
