package org.techteam.decider.rest.api;

import android.os.Bundle;

public class CategoriesGetRequest {
    private final String locale;

    public static final String URL = "categories";

    public class IntentExtras {
        public static final String LOCALE = "LOCALE";
    }

    public CategoriesGetRequest(String locale) {
        this.locale = locale;
    }

    public static CategoriesGetRequest fromBundle(Bundle bundle) {
        String locale = bundle.getString(IntentExtras.LOCALE, "en_US");
        return new CategoriesGetRequest(locale);
    }

    public String getLocale() {
        return locale;
    }
}
