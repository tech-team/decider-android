package org.techteam.decider.gui.activities;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.nostra13.universalimageloader.core.ImageLoader;

import junit.framework.Assert;

import org.techteam.decider.R;
import org.techteam.decider.content.ImageData;
import org.techteam.decider.content.UserData;
import org.techteam.decider.content.entities.UserEntry;
import org.techteam.decider.gui.activities.lib.IAuthTokenGetter;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Keyboard;
import org.techteam.decider.util.Toaster;
import org.techteam.decider.util.image_selector.ActivityStarter;
import org.techteam.decider.util.image_selector.ImageSelector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.techteam.decider.content.entities.UserEntry.byUId;

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
    private Spinner genderSpinner;
    private ArrayAdapter<Gender> genderAdapter;
    private EditText countryText;
    private EditText cityText;
    private EditText birthdayText;

    private Button saveButton;

    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();
    private ServiceHelper serviceHelper;
    private ProgressDialog waitDialog;

    private Date birthday;

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
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

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
        genderSpinner = (Spinner) findViewById(R.id.gender);
        countryText = (EditText) findViewById(R.id.country);
        cityText = (EditText) findViewById(R.id.city);
        birthdayText = (EditText) findViewById(R.id.birthday);

        List<Gender> genders = new ArrayList<>();
        genders.add(new Gender(UserEntry.Gender.None));
        genders.add(new Gender(UserEntry.Gender.Female));
        genders.add(new Gender(UserEntry.Gender.Male));
        genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genders);
        genderSpinner.setAdapter(genderAdapter);

        saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Keyboard.hideSoftKeyboard(EditProfileActivity.this, getWindow().getDecorView());
                waitDialog = ProgressDialog.show(EditProfileActivity.this, getString(R.string.saving_profile), getString(R.string.please_wait), true);
                saveData();
            }
        });

        birthdayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = entry.getBirthday();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                DatePickerDialog dialog = new DatePickerDialog(EditProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        birthday = calendar.getTime();

                        String date = DateFormat.getDateFormat(EditProfileActivity.this).format(birthday);
                        birthdayText.setText(date);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
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

        userData.setGender(genderAdapter.getItem(genderSpinner.getSelectedItemPosition()).getValue());
        userData.setFirstName(nickNameText.getText().toString());
        userData.setLastName(nameText.getText().toString());

        if (birthday != null)
            userData.setBirthday(birthday);

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
            entry = byUId(uid);

            return entry;
        }

        @Override
        protected void onPostExecute(UserEntry entry) {
            String avatarUrl = entry.getAvatar();
            if (avatarUrl != null) {
                ImageLoader.getInstance().displayImage(ApiUI.resolveUrl(avatarUrl), profileImage);
            }

            nameText.setText(entry.getFirstName());
            surnameText.setText(entry.getLastName());
            countryText.setText(entry.getCountry());
            cityText.setText(entry.getCity());

            Date birthday = entry.getBirthday();
            if (birthday != null) {
                String date = DateFormat.getDateFormat(EditProfileActivity.this).format(entry.getBirthday());
                birthdayText.setText(date);
            }

            UserEntry.Gender genderValue = entry.getGender();
            int position = getGenderPositionByValue(genderValue);
            genderSpinner.setSelection(position);

            waitDialog.dismiss();
        }
    }


    public int getGenderPositionByValue(UserEntry.Gender genderValue) {
        for (int i = 0; i < genderAdapter.getCount(); ++i) {
            Gender gender = genderAdapter.getItem(i);
            if (gender.getValue() == genderValue)
                return i;
        }

        return 0;
    }

    private class Gender {
        private UserEntry.Gender value;

        public Gender(UserEntry.Gender genderValue) {
            value = genderValue;
        }

        public UserEntry.Gender getValue() {
            return value;
        }

        @Override
        public String toString() {
            switch (value) {
                case Male:
                    return getString(R.string.gender_male);
                case Female:
                    return getString(R.string.gender_female);
                case None:
                default:
                    return getString(R.string.gender_none);
            }
        }
    }
}
