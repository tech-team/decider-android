package org.techteam.decider.rest.api;

import android.os.Bundle;

public class LoginRegisterRequest {
    public static final String URL = "registration";
    private final String email;
    private final String password;

    public class IntentExtras {
        public static final String EMAIL = "EMAIL";
        public static final String PASSWORD = "PASSWORD";
    }

    public LoginRegisterRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static LoginRegisterRequest fromBundle(Bundle bundle) {
        String email = bundle.getString(IntentExtras.EMAIL, "");
        String password = bundle.getString(IntentExtras.PASSWORD, "");
        return new LoginRegisterRequest(email, password);
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
