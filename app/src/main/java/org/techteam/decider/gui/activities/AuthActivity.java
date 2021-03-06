package org.techteam.decider.gui.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.techteam.decider.R;
import org.techteam.decider.auth.AccountGeneral;
import org.techteam.decider.gcm.GcmPreferences;
import org.techteam.decider.gcm.GcmRegistrationIntentService;
import org.techteam.decider.gui.activities.lib.AccountAuthenticatorActivity;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.SocialProviders;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Keyboard;
import org.techteam.decider.util.ServicesChecker;
import org.techteam.decider.util.Toaster;


public class AuthActivity extends AccountAuthenticatorActivity {
    public static final String TAG = AuthActivity.class.getName();

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final static String PACKAGE_NAME = "org.techteam.decider";

    private AccountManager mAccountManager;

    // controls
    private EditText emailText;
    private EditText passwordText;

    private Button registerButton;
    private Button loginButton;
    private ImageButton loginViaVKButton;

    private BroadcastReceiver gcmRegistrationBroadcastReceiver;

    private ServiceHelper serviceHelper;

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
    }

    private static final class ActivityCodes {
        public static final int SOCIAL_LOGIN = 1001;
        public static final int FINISH_REGISTRATION = 1002;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_auth);

        mAccountManager = AccountManager.get(getBaseContext());

        // find controls
        emailText = (EditText) findViewById(R.id.email_text);
        passwordText = (EditText) findViewById(R.id.password_text);

        registerButton = (Button) findViewById(R.id.register_button);
        loginButton = (Button) findViewById(R.id.login_button);
        loginViaVKButton = (ImageButton) findViewById(R.id.login_via_vk_button);

        // attach callbacks
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        loginViaVKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socialLogin(SocialProviders.Provider.VK);
            }
        });

        serviceHelper = new ServiceHelper(this);
        CallbacksKeeper callbacksKeeper = CallbacksKeeper.getInstance();
        callbacksKeeper.addCallback(TAG, OperationType.LOGIN, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                String username = data.getString(LoginRegisterExtras.USERNAME);
                String password = data.getString(LoginRegisterExtras.PASSWORD);
                saveToken(data, username, password);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int genericError = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (genericError) {
                    case ErrorsExtras.GenericErrors.SERVER_ERROR:
                        Toaster.toastLong(getApplicationContext(), R.string.server_problem);
                        return;
                    case ErrorsExtras.GenericErrors.NO_INTERNET:
                        Toaster.toastLong(getApplicationContext(), R.string.no_internet);
                        return;
                    case ErrorsExtras.GenericErrors.INTERNAL_PROBLEMS:
                        Toaster.toastLong(getApplicationContext(), R.string.internal_problems);
                        return;
                }

                int serverErrorCode = data.getInt(ErrorsExtras.SERVER_ERROR_CODE, -1);
                switch (serverErrorCode) {
                    case ErrorsExtras.ErrorCodes.REGISTRATION_UNFINISHED:
                        requestUsername(data);
                        return;
                    case LoginRegisterExtras.ErrorCodes.INVALID_CREDENTIALS:
                        Toaster.toast(getApplicationContext(), R.string.invalid_credentials);
                        return;
                    default:
                        break;
                }
                Toaster.toast(getApplicationContext(), "Login: failed. " + message);
            }
        });
        callbacksKeeper.addCallback(TAG, OperationType.REGISTER, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {

            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int genericError = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (genericError) {
                    case ErrorsExtras.GenericErrors.SERVER_ERROR:
                        Toaster.toastLong(getApplicationContext(), R.string.server_problem);
                        return;
                    case ErrorsExtras.GenericErrors.NO_INTERNET:
                        Toaster.toastLong(getApplicationContext(), R.string.no_internet);
                        return;
                    case ErrorsExtras.GenericErrors.INTERNAL_PROBLEMS:
                        Toaster.toastLong(getApplicationContext(), R.string.internal_problems);
                        return;
                }

                int serverErrorCode = data.getInt(ErrorsExtras.SERVER_ERROR_CODE, -1);
                switch (serverErrorCode) {
                    case ErrorsExtras.ErrorCodes.REGISTRATION_UNFINISHED:
                        requestUsername(data);
                        return;
                    case LoginRegisterExtras.ErrorCodes.INVALID_CREDENTIALS:
                        Toaster.toast(getApplicationContext(), R.string.email_taken);
                        return;
                    default:
                        break;
                }
                Toaster.toast(AuthActivity.this, "Register: failed. " + message);
            }
        });

        // toolbar stuff
        Toolbar toolbar = (Toolbar) findViewById(R.id.auth_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            serviceHelper.restoreOperationsState(savedInstanceState,
                    BundleKeys.PENDING_OPERATIONS,
                    callbacksKeeper, TAG);
        }
    }

    private void requestUsername(Bundle registrationData) {
        Intent finishRegistrationIntent = new Intent(AuthActivity.this, EditProfileActivity.class);
        finishRegistrationIntent.putExtra(EditProfileActivity.IntentExtras.REGISTRATION_DATA, registrationData);
        String uid = registrationData.getString(ServiceCallback.LoginRegisterExtras.USER_ID);
        finishRegistrationIntent.putExtra(EditProfileActivity.IntentExtras.USER_ID, uid);
        startActivityForResult(finishRegistrationIntent, ActivityCodes.FINISH_REGISTRATION);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        serviceHelper.saveOperationsState(outState, BundleKeys.PENDING_OPERATIONS);
    }

    private void login() {
        Keyboard.hideSoftKeyboard(this, getWindow().getDecorView());

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.equals("") || password.equals("")) {
            Toaster.toast(AuthActivity.this, getString(R.string.empty_credentials));
            return;
        }

        serviceHelper.login(TAG, email, password, CallbacksKeeper.getInstance().getCallback(TAG, OperationType.LOGIN));
    }

    private void register() {
        Keyboard.hideSoftKeyboard(this, getWindow().getDecorView());

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();


        if (email.equals("") || password.equals("")) {
            Toaster.toast(AuthActivity.this, getString(R.string.empty_credentials));
            return;
        }

        // nice article about email regexes:
        // http://www.webmonkey.com/2008/08/four_regular_expressions_to_check_email_addresses/
        // so let's just use dirty and simple one
        if (!email.matches(".+\\@.+\\..+")) {
            Toaster.toast(AuthActivity.this, getString(R.string.invalid_email));
            return;
        }

        serviceHelper.register(TAG, email, password, CallbacksKeeper.getInstance().getCallback(TAG, OperationType.REGISTER));
    }

    private void socialLogin(SocialProviders.Provider provider) {
        Keyboard.hideSoftKeyboard(this, getWindow().getDecorView());
        String url = SocialProviders.getProviderPath(provider);

        Intent intent = new Intent(AuthActivity.this, SocialLoginActivity.class);
        Bundle options = new Bundle();
        options.putString(SocialLoginActivity.IntentKeys.URL, url);
        intent.putExtras(options);
        startActivityForResult(intent, ActivityCodes.SOCIAL_LOGIN);
    }

    private void saveToken(Bundle data, String login, String password) {
        String token = data.getString(ServiceCallback.LoginRegisterExtras.TOKEN);
        long expires = data.getLong(ServiceCallback.LoginRegisterExtras.EXPIRES);
        String refreshToken = data.getString(ServiceCallback.LoginRegisterExtras.REFRESH_TOKEN);
        String userId = data.getString(ServiceCallback.LoginRegisterExtras.USER_ID);
        String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        final Account account = new Account(login, PACKAGE_NAME);

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d(TAG, "> finishLogin > addAccountExplicitly");

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, password, null);
            mAccountManager.setAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, token);
        } else {
            Log.d(TAG, "> finishLogin > setPassword");
            mAccountManager.setPassword(account, password);
        }
        mAccountManager.setUserData(account, ServiceCallback.LoginRegisterExtras.EXPIRES, Long.toString(expires));
        mAccountManager.setUserData(account, ServiceCallback.LoginRegisterExtras.REFRESH_TOKEN, refreshToken);
        mAccountManager.setUserData(account, ServiceCallback.LoginRegisterExtras.USER_ID, userId);


        data.putString(AccountManager.KEY_ACCOUNT_NAME, login);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        data.putString(AccountManager.KEY_AUTHTOKEN, token);
        data.putString(PARAM_USER_PASS, password);

        setAccountAuthenticatorResult(data);

        subscribeGcm();

        setResult(Activity.RESULT_OK);
        finish();
    }

    private void subscribeGcm() {

        ServicesChecker.CheckerResult res = ServicesChecker.checkPlayServices(AuthActivity.this);

        switch (res) {
            case OK:
                Intent intent = new Intent(this, GcmRegistrationIntentService.class);
                startService(intent);
                break;
            case NOT_OK:
                break;
            case NOT_SUPPORTED:
                Log.i(TAG, "GCM is not supported on this device.");
                finishAffinity();
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        serviceHelper.init();
        LocalBroadcastManager.getInstance(this).registerReceiver(gcmRegistrationBroadcastReceiver,
                new IntentFilter(GcmPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    public void onPause() {
        super.onPause();
        serviceHelper.release();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gcmRegistrationBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToolbarActivity.setAuthInProgress(false);
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }

    private void loginRegisterComplete(Intent data) {
        Bundle registrationData = data.getExtras();
        String username = registrationData.getString(ServiceCallback.LoginRegisterExtras.USERNAME);
        String password = registrationData.getString(ServiceCallback.LoginRegisterExtras.PASSWORD);
        if (password == null) {
            password = "dummy";
        }
        saveToken(registrationData, username, password);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == ActivityCodes.SOCIAL_LOGIN) {
            if (data.getBooleanExtra(ServiceCallback.LoginRegisterExtras.REGISTRATION_UNFINISHED, false)) {
                requestUsername(data.getExtras());
            } else {
                loginRegisterComplete(data);
            }
        } else if (requestCode == ActivityCodes.FINISH_REGISTRATION) {
            loginRegisterComplete(data);
        }
    }
}
