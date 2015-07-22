package org.techteam.decider.rest.api;

import android.os.Bundle;

import org.techteam.decider.content.UserData;

public class UserEditRequest extends ApiRequest {
    public static final String URL = "user";

    private final UserData userData;
    private final String accessToken;

    public class IntentExtras {
        public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
        public static final String USER_DATA = "USER_DATA";
    }

    public UserEditRequest(UserData userData) {
        this.userData = userData;
        this.accessToken = null;
    }

    public UserEditRequest(UserData userData, String accessToken) {
        this.userData = userData;
        this.accessToken = accessToken;
    }

    public static UserEditRequest fromBundle(Bundle bundle) {
        UserData userData = bundle.getParcelable(IntentExtras.USER_DATA);
        String accessToken = bundle.getString(IntentExtras.ACCESS_TOKEN);
        return new UserEditRequest(userData, accessToken);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public UserData getUserData() {
        return userData;
    }

    @Override
    public String getPath() {
        return URL;
    }
}
