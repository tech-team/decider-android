package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.api.VKError;

import org.techteam.decider.R;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Keyboard;
import org.techteam.decider.util.Toaster;

public class AuthFragment
        extends Fragment {

    public static final String TAG = AuthFragment.class.toString();
    private MainActivity activity;

    // VK stuff
    private static final String VK_APP_ID = "4855698";
    private static final String VK_AUTH_SCOPE = "";

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_auth, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // find controls
        emailText = (EditText) view.findViewById(R.id.email_text);
        passwordText = (EditText) view.findViewById(R.id.password_text);

        registerButton = (Button) view.findViewById(R.id.register_button);
        loginButton = (Button) view.findViewById(R.id.login_button);
        loginViaVKButton = (ImageButton) view.findViewById(R.id.login_via_vk_button);

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
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        this.activity = (MainActivity) activity;

        serviceHelper = new ServiceHelper(activity);
        callbacksKeeper.addCallback(OperationType.LOGIN_REGISTER, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toast(activity.getBaseContext(), "LoginRegister: ok");
                openMainFragment();
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                Toaster.toast(activity.getBaseContext(), "LoginRegister: failed");
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toolbar toolbar = (Toolbar) this.activity.findViewById(R.id.auth_toolbar);
        this.activity.setSupportActionBar(toolbar);

        ActionBar actionBar = this.activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(this.activity.getString(R.string.auth_title));
            //actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeButtonEnabled(true);
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

    @Override
    public void onResume() {
        super.onResume();
        activity.lockDrawer();
        serviceHelper.init();
    }

    @Override
    public void onPause() {
        super.onPause();
        activity.unlockDrawer();
        serviceHelper.release();
    }

    private void register() {
        Keyboard.hideSoftKeyboard(getActivity(), getView());

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();


        if (email.equals("") || password.equals("")) {
            Toaster.toast(activity.getBaseContext(), "email or password are empty");
            return;
        }

        Toaster.toast(activity.getBaseContext(), email + " : " + password);
        serviceHelper.loginRegister(email, password, callbacksKeeper.getCallback(OperationType.LOGIN_REGISTER));
    }

    private void login() {
        Keyboard.hideSoftKeyboard(getActivity(), getView());

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.equals("") || password.equals("")) {
            Toaster.toast(activity.getBaseContext(), "email or password are empty");
            return;
        }

        Toaster.toast(activity.getBaseContext(), email + " : " + password);
        serviceHelper.loginRegister(email, password, callbacksKeeper.getCallback(OperationType.LOGIN_REGISTER));
    }

    private void loginViaVK() {
        Keyboard.hideSoftKeyboard(getActivity(), getView());

        final Activity activity = this.activity;

        VKSdk.initialize(new VKSdkListener() {
            @Override
            public void onCaptchaError(VKError vkError) {
                Toaster.toast(activity, vkError.errorMessage);
            }

            @Override
            public void onTokenExpired(VKAccessToken vkAccessToken) {
                Toaster.toast(activity, "Token expired");
            }

            @Override
            public void onAccessDenied(VKError vkError) {
                Toaster.toast(activity, vkError.errorMessage);
            }

            @Override
            public void onReceiveNewToken(VKAccessToken newToken) {
                super.onReceiveNewToken(newToken);

                // auth was successful
                Toaster.toast(activity, newToken.userId);

                // open main fragment
                openMainFragment();
            }
        }, VK_APP_ID);

        VKSdk.authorize(VK_AUTH_SCOPE);
    }

    private void openMainFragment() {
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new MainFragment()).commit();
    }
}
