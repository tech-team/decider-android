package org.techteam.decider.rest.api;

import android.os.Bundle;

public class RegisterRequest {
    public static final String URL = "/registration";
    private final String email;
    private final String password;

    public class IntentExtras {
        public static final String EMAIL = "EMAIL";
        public static final String PASSWORD = "PASSWORD";
    }

    public RegisterRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static RegisterRequest fromBundle(Bundle bundle) {
        String email = bundle.getString(IntentExtras.EMAIL, "");
        String password = bundle.getString(IntentExtras.PASSWORD, "");
        return new RegisterRequest(email, password);
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
