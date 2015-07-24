package org.techteam.decider.gui.activities;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Bundle;

import org.techteam.decider.auth.AccountGeneral;

public final class AuthTokenGetHelper {
    public static AccountManagerFuture<Bundle> getAuthTokenByFeatures(Activity activity, AccountManagerCallback<Bundle> cb) {
        AccountManager am = AccountManager.get(activity);
        return am.getAuthTokenByFeatures(activity.getApplicationContext().getPackageName(),
                AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,
                null, activity, null, null, cb, null);
    }
}
