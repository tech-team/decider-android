package org.techteam.decider.rest.api;

import android.os.Bundle;

public class UserGetRequest {
    private final String userId;

    public static final String URL = "user";

    public class IntentExtras {
        public static final String USER_ID = "USER_ID";
    }

    public UserGetRequest(String userId) {
        this.userId = userId;
    }

    public static UserGetRequest fromBundle(Bundle bundle) {
        String userId = bundle.getString(IntentExtras.USER_ID);
        return new UserGetRequest(userId);
    }

    public String getUserId() {
        return userId;
    }
}
