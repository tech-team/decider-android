package org.techteam.decider.gui.activities.lib;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;

public interface AuthTokenGetter {
    AccountManagerFuture<Bundle> getAuthToken(AccountManagerCallback<Bundle> cb);
    AccountManagerFuture<Bundle> getAuthTokenOrExit(AccountManagerCallback<Bundle> cb);
}
