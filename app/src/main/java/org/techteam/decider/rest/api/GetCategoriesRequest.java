package org.techteam.decider.rest.api;

import android.os.Bundle;

public class GetCategoriesRequest {
    private final String locale;

    public static final String URL = "categories";

    public class IntentExtras {
        public static final String LOCALE = "LOCALE";
    }

    public GetCategoriesRequest(String locale) {
        this.locale = locale;
    }

    public static GetCategoriesRequest fromBundle(Bundle bundle) {
        String locale = bundle.getString(IntentExtras.LOCALE, "en_US");
        return new GetCategoriesRequest(locale);
    }

    public String getLocale() {
        return locale;
    }
}
