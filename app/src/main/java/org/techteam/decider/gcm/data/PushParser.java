package org.techteam.decider.gcm.data;

import android.os.Bundle;

public class PushParser {
    public static Push parse(Bundle data) {
        int code = data.getInt("code", -1);
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
        }

        return push;
    }
}
