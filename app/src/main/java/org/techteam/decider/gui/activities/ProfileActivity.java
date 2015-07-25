package org.techteam.decider.gui.activities;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.UserEntry;
import org.techteam.decider.gui.activities.lib.AuthTokenGetter;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.ImageLoaderInitializer;
import org.techteam.decider.util.Toaster;

import java.util.Date;

public class ProfileActivity extends ToolbarActivity implements AuthTokenGetter {
    private static final String TAG = ProfileActivity.class.getName();

    public final static String USER_ID = "USER_ID";

    public final static int EDIT_PROFILE = 0;
    private static final long WAIT_DIALOG_DISMISS_DELAY = 600;

    private UserEntry entry;
    private String uid;

    private RetrieveEntryTask retrieveEntryTask;

    // children
    private Toolbar toolbar;
    private ImageView profileImage;

    private TextView fullNameText;
    private TextView countryText;
    private TextView cityText;
    private TextView birthdayText;
    private TextView genderText;
    private TextView aboutText;

    private Button editButton;

    private ImageLoader imageLoader;

    private ServiceHelper serviceHelper;
    private ProgressDialog waitDialog;

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
    }

    @Override
    public AccountManagerFuture<Bundle> getAuthToken(AccountManagerCallback<Bundle> cb) {
        return AuthTokenGetHelper.getAuthTokenByFeatures(this, cb);
    }

    @Override
    public AccountManagerFuture<Bundle> getAuthTokenOrExit(final AccountManagerCallback<Bundle> cb) {
        AccountManagerCallback<Bundle> actualCb = new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                if (!future.isCancelled()) {
                    if (cb != null) {
                        cb.run(future);
                    }
                }
            }
        };
        return AuthTokenGetHelper.getAuthTokenByFeatures(this, actualCb);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_profile);

        uid = getIntent().getStringExtra(USER_ID);
//        Assert.assertNotSame("UID is null", uid, null);

        // setup toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        imageLoader = ImageLoaderInitializer.getImageLoader(this);

        // find children
        profileImage = (ImageView) findViewById(R.id.profile_image);

        fullNameText = (TextView) findViewById(R.id.nick_name_text);
        countryText = (TextView) findViewById(R.id.country_text);
        cityText = (TextView) findViewById(R.id.city_text);
        birthdayText = (TextView) findViewById(R.id.birthday_text);
        genderText = (TextView) findViewById(R.id.gender_text);
        aboutText = (TextView) findViewById(R.id.about_text);

        editButton = (Button) findViewById(R.id.edit_button);

        serviceHelper = new ServiceHelper(this);
        CallbacksKeeper callbacksKeeper = CallbacksKeeper.getInstance();
        callbacksKeeper.addCallback(TAG, OperationType.USER_GET, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                retrieveEntryTask = new RetrieveEntryTask();
                retrieveEntryTask.execute();
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                waitDialog.dismiss();
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        getAuthToken(null);
                        return;
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
                Toaster.toastLong(getApplicationContext(), "GetUser: failed. " + message);
            }
        });

        if (savedInstanceState != null) {
            serviceHelper.restoreOperationsState(savedInstanceState, BundleKeys.PENDING_OPERATIONS, CallbacksKeeper.getInstance(), TAG);
        }

        waitDialog = ProgressDialog.show(this, getString(R.string.loading_profile), getString(R.string.please_wait),
                true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ProfileActivity.this.finish();
            }
        });
        serviceHelper.getUser(TAG, uid, CallbacksKeeper.getInstance().getCallback(TAG, OperationType.USER_GET));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_PROFILE && resultCode == Activity.RESULT_OK) {
            // update data
            retrieveEntryTask = new RetrieveEntryTask();
            retrieveEntryTask.execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceHelper.init();
    }

    @Override
    protected void onPause() {
        super.onResume();
        serviceHelper.release();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        serviceHelper.saveOperationsState(outState, BundleKeys.PENDING_OPERATIONS);
    }

    class RetrieveEntryTask extends AsyncTask<Void, Void, UserEntry> {

        @Override
        protected UserEntry doInBackground(Void... params) {
            entry = UserEntry.byUId(uid);

            return entry;
        }

        @Override
        protected void onPostExecute(UserEntry entry) {
            String avatarUrl = entry.getAvatar();
            if (avatarUrl != null) {
                imageLoader.displayImage(ApiUI.resolveUrl(avatarUrl), profileImage);
            }

            toolbar.setTitle(getString(R.string.profile_toolbar_title) + ": " + entry.getUsername());
            fullNameText.setText(entry.getFirstName() + " " + entry.getLastName());
            countryText.setText(entry.getCountry());
            cityText.setText(entry.getCity());

            Date birthday = entry.getBirthday();
            if (birthday != null) {
                String date = DateFormat.getDateFormat(ProfileActivity.this).format(birthday);
                birthdayText.setText(date);
            }

            genderText.setText(new GenderInfo(ProfileActivity.this, entry.getGender()).toString());
            aboutText.setText(entry.getAbout());

            // edit button available only for current profile
            if (!entry.getUid().equals(ApiUI.getCurrentUserId(ProfileActivity.this))) {
                editButton.setVisibility(View.GONE);
            } else {
                editButton.setVisibility(View.VISIBLE);
                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                        intent.putExtra(EditProfileActivity.IntentExtras.USER_ID, getIntent().getStringExtra(USER_ID));
                        startActivityForResult(intent, EDIT_PROFILE);
                    }
                });
            }

            // wait for render event
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    waitDialog.dismiss();
                }
            }, WAIT_DIALOG_DISMISS_DELAY);
        }
    }
}
