package org.techteam.decider.rest.api;

import android.os.Bundle;

public class UserGetRequest extends ApiRequest {
    private final String userId;
    private final String accessToken;

    public static final String URL = "user";

    public class IntentExtras {
        public static final String USER_ID = "USER_ID";
        public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    }

    public UserGetRequest(String userId) {
        this.userId = userId;
        this.accessToken = null;
    }

    public UserGetRequest(String userId, String accessToken) {
        this.userId = userId;
        this.accessToken = accessToken;
    }

    public static UserGetRequest fromBundle(Bundle bundle) {
        String userId = bundle.getString(IntentExtras.USER_ID);
        String accessToken = bundle.getString(IntentExtras.ACCESS_TOKEN);
        return new UserGetRequest(userId, accessToken);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String getPath() {
        return URL;
    }
}
