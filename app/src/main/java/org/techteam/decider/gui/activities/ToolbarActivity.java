package org.techteam.decider.gui.activities;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.techteam.decider.auth.AccountAuthenticator;
import org.techteam.decider.auth.CanceledAccountManagerFuture;
import org.techteam.decider.gui.activities.lib.AuthTokenGetter;

import java.util.concurrent.atomic.AtomicBoolean;

public class ToolbarActivity extends AppCompatActivity implements
        AuthTokenGetter {
    private static AtomicBoolean authInProgress = new AtomicBoolean(false);
//    private static boolean authInProgress = false;

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
        if (authInProgress.get()) {
            return new CanceledAccountManagerFuture<>();
        } else {
            authInProgress.set(true);
            return AuthTokenGetHelper.getAuthTokenByFeatures(this, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    if (cb != null) {
                        cb.run(future);
                    }
                    authInProgress.set(false);
                }
            });
        }
    }

    @Override
    public AccountManagerFuture<Bundle> getAuthTokenAndCheck(final AccountManagerCallback<Bundle> cb) {
        return getAuthToken(new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                if (!future.isCancelled()) {
                    if (cb != null) {
                        cb.run(future);
                    }
                }
            }
        });
    }

    public static void setAuthInProgress(boolean authInProgress) {
        ToolbarActivity.authInProgress.set(authInProgress);
    }
}
