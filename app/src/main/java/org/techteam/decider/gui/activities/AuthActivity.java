package org.techteam.decider.gui.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.techteam.decider.R;
import org.techteam.decider.auth.AccountGeneral;
import org.techteam.decider.gui.activities.lib.AccountAuthenticatorActivity;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Keyboard;
import org.techteam.decider.util.Toaster;


public class AuthActivity extends AccountAuthenticatorActivity {
    public static final String TAG = AuthActivity.class.getName();

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final int REQ_SIGNUP = 1;

    private final static String PACKAGE_NAME = "org.techteam.decider";

    private AccountManager mAccountManager;

    // controls
    private EditText emailText;
    private EditText passwordText;

    private Button registerButton;
    private Button loginButton;
    private ImageButton loginViaVKButton;

    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();
    private ServiceHelper serviceHelper;

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.fragment_auth);

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
                loginViaVK();
            }
        });

        serviceHelper = new ServiceHelper(this);
        callbacksKeeper.addCallback(OperationType.LOGIN, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toast(AuthActivity.this, "Login: ok");
                finishLogin(data);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                Toaster.toast(AuthActivity.this, "Login: failed. " + message);
            }
        });
        callbacksKeeper.addCallback(OperationType.REGISTER, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toast(AuthActivity.this, "Register: ok");
                finishLogin(data);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                Toaster.toast(AuthActivity.this, "Register: failed. " + message);
            }
        });

        // toolbar stuff
        Toolbar toolbar = (Toolbar) findViewById(R.id.auth_toolbar);
        setSupportActionBar(toolbar);

        //TODO: is this needed?
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.auth_title));
        }

        if (savedInstanceState != null) {
            serviceHelper.restoreOperationsState(savedInstanceState,
                    BundleKeys.PENDING_OPERATIONS,
                    callbacksKeeper);
        }
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
            Toaster.toast(AuthActivity.this, "email or password are empty");
            return;
        }

        serviceHelper.login(email, password, callbacksKeeper.getCallback(OperationType.LOGIN));
    }

    private void register() {
        Keyboard.hideSoftKeyboard(this, getWindow().getDecorView());

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();


        if (email.equals("") || password.equals("")) {
            Toaster.toast(AuthActivity.this, "email or password are empty");
            return;
        }

        Toaster.toast(this, email + " : " + password);
        serviceHelper.register(email, password, callbacksKeeper.getCallback(OperationType.REGISTER));
    }

    private void loginViaVK() {
        Keyboard.hideSoftKeyboard(this, getWindow().getDecorView());

        // TODO: vk
    }

    private void finishLogin(Bundle data) {
        Log.d(TAG, "> finishLogin");

        String login = data.getString(ServiceCallback.LoginRegisterExtras.LOGIN);
        String password = data.getString(ServiceCallback.LoginRegisterExtras.PASSWORD);
        String token = data.getString(ServiceCallback.LoginRegisterExtras.TOKEN);
        String refreshToken = data.getString(ServiceCallback.LoginRegisterExtras.REFRESH_TOKEN);
        String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        final Account account = new Account(login, PACKAGE_NAME);

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d(TAG, "> finishLogin > addAccountExplicitly");

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, password, null);
            mAccountManager.setAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, token);
            mAccountManager.setUserData(account, ServiceCallback.LoginRegisterExtras.REFRESH_TOKEN, refreshToken);
        } else {
            Log.d(TAG, "> finishLogin > setPassword");
            mAccountManager.setPassword(account, password);
        }

        data.putString(AccountManager.KEY_ACCOUNT_NAME, login);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        data.putString(AccountManager.KEY_AUTHTOKEN, token);
        data.putString(PARAM_USER_PASS, password);

        setAccountAuthenticatorResult(data);

        setResult(Activity.RESULT_OK);
        finish();
    }


    @Override
    public void onResume() {
        super.onResume();
        serviceHelper.init();
    }

    @Override
    public void onPause() {
        super.onPause();
        serviceHelper.release();
    }
}
