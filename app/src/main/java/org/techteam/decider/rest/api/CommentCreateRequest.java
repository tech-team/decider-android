package org.techteam.decider.rest.api;

import android.os.Bundle;

import org.techteam.decider.content.question.CommentData;

public class CommentCreateRequest extends ApiRequest {
    private final CommentData commentData;

    public static final String URL = "comments";

    public class IntentExtras {
        public static final String COMMENT_DATA = "COMMENT_DATA";
    }

    public CommentCreateRequest(CommentData commentData) {
        this.commentData = commentData;
    }

    public static CommentCreateRequest fromBundle(Bundle bundle) {
        CommentData commentData = bundle.getParcelable(IntentExtras.COMMENT_DATA);
        return new CommentCreateRequest(commentData);
    }

    public CommentData getCommentData() {
        return commentData;
    }

    @Override
    public String getPath() {
        return URL;
    }
}
