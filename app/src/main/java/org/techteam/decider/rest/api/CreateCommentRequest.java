package org.techteam.decider.rest.api;

import android.os.Bundle;

import org.techteam.decider.content.question.CommentData;
import org.techteam.decider.content.question.QuestionData;

public class CreateCommentRequest {
    private final CommentData commentData;

    public static final String URL = "comments";

    public class IntentExtras {
        public static final String COMMENT_DATA = "COMMENT_DATA";
    }

    public CreateCommentRequest(CommentData commentData) {
        this.commentData = commentData;
    }

    public static CreateCommentRequest fromBundle(Bundle bundle) {
        CommentData commentData = bundle.getParcelable(IntentExtras.COMMENT_DATA);
        return new CreateCommentRequest(commentData);
    }

    public CommentData getCommentData() {
        return commentData;
    }
}
