package org.techteam.decider.gui.activities;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.techteam.decider.auth.AccountAuthenticator;
import org.techteam.decider.auth.CanceledAccountManagerFuture;
import org.techteam.decider.gui.activities.lib.AuthTokenGetter;

public class ToolbarActivity extends AppCompatActivity implements
        AuthTokenGetter {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public AccountManagerFuture<Bundle> getAuthToken(final AccountManagerCallback<Bundle> cb) {
        final SharedPreferences prefs = getSharedPreferences(AccountAuthenticator.AUTH_PREFS, MODE_PRIVATE);
        boolean inProgress = prefs.getBoolean(AccountAuthenticator.AUTH_IN_PROGRESS_KEY, false);
        if (inProgress) {
            return new CanceledAccountManagerFuture<>();
        } else {
            prefs.edit().putBoolean(AccountAuthenticator.AUTH_IN_PROGRESS_KEY, false).apply();
            return AuthTokenGetHelper.getAuthTokenByFeatures(this, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    if (cb != null) {
                        cb.run(future);
                    }
                    prefs.edit().putBoolean(AccountAuthenticator.AUTH_IN_PROGRESS_KEY, false).apply();
                }
            });
        }
    }

    @Override
    public AccountManagerFuture<Bundle> getAuthTokenOrExit(final AccountManagerCallback<Bundle> cb) {
        final SharedPreferences prefs = getSharedPreferences(AccountAuthenticator.AUTH_PREFS, MODE_PRIVATE);
        boolean inProgress = prefs.getBoolean(AccountAuthenticator.AUTH_IN_PROGRESS_KEY, false);
        if (inProgress) {
            return new CanceledAccountManagerFuture<>();
        } else {
            prefs.edit().putBoolean(AccountAuthenticator.AUTH_IN_PROGRESS_KEY, true).apply();
            AccountManagerCallback<Bundle> actualCb = new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    if (!future.isCancelled()) {
                        if (cb != null) {
                            cb.run(future);
                        }
                    }
                    prefs.edit().putBoolean(AccountAuthenticator.AUTH_IN_PROGRESS_KEY, false).apply();
                }
            };
            return AuthTokenGetHelper.getAuthTokenByFeatures(this, actualCb);
        }
    }
}
