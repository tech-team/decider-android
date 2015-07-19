package org.techteam.decider.gui.activities;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import junit.framework.Assert;

import org.techteam.decider.R;
import org.techteam.decider.content.ImageData;
import org.techteam.decider.content.UserData;
import org.techteam.decider.content.entities.UserEntry;
import org.techteam.decider.gui.activities.lib.IAuthTokenGetter;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Keyboard;
import org.techteam.decider.util.Toaster;
import org.techteam.decider.util.image_selector.ActivityStarter;
import org.techteam.decider.util.image_selector.ImageSelector;

public class EditProfileActivity extends AppCompatActivity implements ActivityStarter, IAuthTokenGetter {
    public final static String USER_ID = "USER_ID";

    private UserEntry entry;
    private String uid;

    private RetrieveEntryTask retrieveEntryTask;

    // children
    private ImageView profileImage;
    private ImageSelector imageSelector;

    private EditText nickNameText;
    private EditText nameText;
    private EditText surnameText;
    private EditText countryText;
    private EditText cityText;
    private EditText birthdayText;

    private Button saveButton;

    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();
    private ServiceHelper serviceHelper;
    private ProgressDialog waitDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.fragment_profile_edit);

        uid = getIntent().getStringExtra(USER_ID);
        Assert.assertNotSame("UID is null", uid, null);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // find children
        profileImage = (ImageView) findViewById(R.id.profile_image);

        final int aspectWidth = 1;
        final int aspectHeight = 1;
        final int previewWidth = 1280;
        final int previewHeight = 1280;
        imageSelector = new ImageSelector(this, this, profileImage);
        imageSelector.setParams(aspectWidth, aspectHeight, previewWidth, previewHeight);

        nickNameText = (EditText) findViewById(R.id.nick);
        nameText = (EditText) findViewById(R.id.name);
        surnameText = (EditText) findViewById(R.id.surname);
        countryText = (EditText) findViewById(R.id.country);
        cityText = (EditText) findViewById(R.id.city);
        birthdayText = (EditText) findViewById(R.id.birthday);

        saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Keyboard.hideSoftKeyboard(EditProfileActivity.this, getWindow().getDecorView());
                waitDialog = ProgressDialog.show(EditProfileActivity.this, getString(R.string.saving_profile), getString(R.string.please_wait), true);
                saveData();
            }
        });


        serviceHelper = new ServiceHelper(this);
        callbacksKeeper.addCallback(OperationType.USER_EDIT, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                waitDialog.dismiss();
                Toaster.toast(EditProfileActivity.this, "Profile saved");
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                waitDialog.dismiss();
                int code = data.getInt(ErrorsExtras.ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.Codes.INVALID_TOKEN:
                        getAuthToken(null);
                        return;
                    case ErrorsExtras.Codes.SERVER_ERROR:
                        Toaster.toastLong(getApplicationContext(), R.string.server_problem);
                        return;
                }
                Toaster.toastLong(EditProfileActivity.this, "Profile not saved: " + message);
            }
        });
        callbacksKeeper.addCallback(OperationType.USER_GET, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toast(getApplicationContext(), "GetUser: ok");
                retrieveEntryTask = new RetrieveEntryTask();
                retrieveEntryTask.execute();
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                waitDialog.dismiss();
                int code = data.getInt(ErrorsExtras.ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.Codes.INVALID_TOKEN:
                        getAuthToken(null);
                        return;
                    case ErrorsExtras.Codes.SERVER_ERROR:
                        Toaster.toastLong(getApplicationContext(), R.string.server_problem);
                        return;
                }
                Toaster.toastLong(getApplicationContext(), "GetUser: failed. " + message);
            }
        });

        waitDialog = ProgressDialog.show(this, getString(R.string.loading_profile), getString(R.string.please_wait), true);
        serviceHelper.getUser(uid, callbacksKeeper.getCallback(OperationType.USER_GET));
    }

    private void saveData() {
        UserData userData = new UserData();

        ImageData imageData = imageSelector.getImageData();
        if (imageData != null)
            userData.setUsername(nickNameText.getText().toString());

        //TODO: userData.setMuzhik();
        userData.setFirstName(nickNameText.getText().toString());
        userData.setLastName(nameText.getText().toString());
        userData.setBirthday(surnameText.getText().toString());
        userData.setCountry(countryText.getText().toString());
        userData.setCity(cityText.getText().toString());

        serviceHelper.editUser(userData, callbacksKeeper.getCallback(OperationType.USER_EDIT));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageSelector.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public AccountManagerFuture<Bundle> getAuthToken(AccountManagerCallback<Bundle> cb) {
        return AuthTokenGetter.getAuthTokenByFeatures(this, cb);
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
            //TODO: such "null", very string!
            if (avatarUrl != null && !avatarUrl.equals("null"))
                ImageLoader.getInstance().displayImage(entry.getAvatar(), profileImage);

            nameText.setText(entry.getFirstName());
            surnameText.setText(entry.getLastName());
            countryText.setText(entry.getCountry());
            cityText.setText(entry.getCity());
            birthdayText.setText(entry.getBirthday().toString());

            waitDialog.dismiss();
        }
    }
}
