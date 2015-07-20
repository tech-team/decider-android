package org.techteam.decider.rest.api;

import android.os.Bundle;

import org.techteam.decider.content.UserData;

public class UserEditRequest extends ApiRequest {
    public static final String URL = "user";

    private final UserData userData;

    public class IntentExtras {
        public static final String USER_DATA = "USER_DATA";
    }

    public UserEditRequest(UserData userData) {
        this.userData = userData;
    }

    public static UserEditRequest fromBundle(Bundle bundle) {
        UserData userData = bundle.getParcelable(IntentExtras.USER_DATA);
        return new UserEditRequest(userData);
    }

    public UserData getUserData() {
        return userData;
    }

    @Override
    public String getPath() {
        return URL;
    }
}
