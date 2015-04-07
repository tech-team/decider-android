package org.techteam.decider.content.entities;

import org.json.JSONObject;
import org.techteam.decider.content.Entry;

public class CommentEntry implements Entry {

    protected CommentEntry() {
    }

    public static CommentEntry fromJson(JSONObject obj) {

        return new CommentEntry();
    }
}
