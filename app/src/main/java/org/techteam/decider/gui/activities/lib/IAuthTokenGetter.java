package org.techteam.decider.gui.activities.lib;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;

public interface IAuthTokenGetter {
    AccountManagerFuture<Bundle> getAuthToken(AccountManagerCallback<Bundle> cb);
}
