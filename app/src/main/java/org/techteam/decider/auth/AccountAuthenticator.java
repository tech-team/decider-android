package org.techteam.decider.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;
import org.techteam.decider.gui.activities.AuthActivity;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.rest.service_helper.ServiceCallback;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static org.techteam.decider.auth.AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;
import static org.techteam.decider.auth.AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
import static org.techteam.decider.auth.AccountGeneral.AUTHTOKEN_TYPE_READ_ONLY;
import static org.techteam.decider.auth.AccountGeneral.AUTHTOKEN_TYPE_READ_ONLY_LABEL;

public class AccountAuthenticator extends AbstractAccountAuthenticator {
    private String TAG = AccountAuthenticator.class.getName();
    private final Context mContext;

    private ApiUI apiUI;

    public AccountAuthenticator(Context context) {
        super(context);
        apiUI = new ApiUI(context);

        // I hate you! Google - set mContext as protected!
        this.mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "> addAccount");

        final Intent intent = new Intent(mContext, AuthActivity.class);

        intent.putExtra(AuthActivity.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(AuthActivity.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(AuthActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "> getAuthToken");

        // If the caller requested an authToken type we don't support, then
        // return an error
        if (!authTokenType.equals(AccountGeneral.AUTHTOKEN_TYPE_READ_ONLY) && !authTokenType.equals(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager am = AccountManager.get(mContext);

        String authToken = am.peekAuthToken(account, authTokenType);
        String expiresStr = am.getUserData(account, ServiceCallback.LoginRegisterExtras.EXPIRES);
        Long expires = expiresStr != null ? Long.parseLong(expiresStr) : null;
        String refreshToken = am.getUserData(account, ServiceCallback.LoginRegisterExtras.REFRESH_TOKEN);

        Log.d(TAG, "> peekAuthToken returned - " + authToken);
        if (TextUtils.isEmpty(authToken) || (expires != null && System.currentTimeMillis() > expires)) { // Token is expired
            if (refreshToken != null) {
                try {
                    Log.d(TAG, "> refreshing token");

                    JSONObject data = apiUI.refreshToken(refreshToken);
                    authToken = apiUI.extractToken(data);
                    expires = (long) apiUI.extractTokenExpires(data);
                    refreshToken = apiUI.extractRefreshToken(data);

                    am.setAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, authToken);
                    am.setUserData(account, ServiceCallback.LoginRegisterExtras.EXPIRES, Long.toString(System.currentTimeMillis() + expires * 1000));
                    am.setUserData(account, ServiceCallback.LoginRegisterExtras.REFRESH_TOKEN, refreshToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            result.putLong(ServiceCallback.LoginRegisterExtras.EXPIRES, expires != null ? expires : 0);
            result.putString(ServiceCallback.LoginRegisterExtras.REFRESH_TOKEN, refreshToken);
            return result;
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        final Intent intent = new Intent(mContext, AuthActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AuthActivity.ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(AuthActivity.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(AuthActivity.ARG_ACCOUNT_NAME, account.name);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }


    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
            return AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        else if (AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType))
            return AUTHTOKEN_TYPE_READ_ONLY_LABEL;
        else
            return authTokenType + " (Label)";
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }
}