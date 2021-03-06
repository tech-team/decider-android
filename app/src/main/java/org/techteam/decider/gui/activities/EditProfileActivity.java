package org.techteam.decider.gui.activities;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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

import org.techteam.decider.R;
import org.techteam.decider.content.ImageData;
import org.techteam.decider.content.UserData;
import org.techteam.decider.content.entities.UserEntry;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.ImageLoaderInitializer;
import org.techteam.decider.util.Keyboard;
import org.techteam.decider.util.Toaster;
import org.techteam.decider.util.image_selector.ActivityStarter;
import org.techteam.decider.util.image_selector.ImageSelector;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

import static android.support.v7.app.AlertDialog.*;
import static org.techteam.decider.content.entities.UserEntry.byUId;

public class EditProfileActivity extends ToolbarActivity implements ActivityStarter {
    public static final String TAG = EditProfileActivity.class.getName();
    private static final long WAIT_DIALOG_DISMISS_DELAY = 600;

    private UserEntry entry;
    private String uid;

    private RetrieveUserTask retrieveUserTask;

    // children
    private ImageView profileImage;
    private ImageSelector imageSelector;

    private EditText nickNameText;
    private EditText nameText;
    private EditText surnameText;
    private Spinner genderSpinner;
    private ArrayAdapter<GenderInfo> genderAdapter;
    private EditText countryText;
    private EditText cityText;
    private Button birthdayText;
    private EditText aboutText;

    private Button saveButton;

    private ServiceHelper serviceHelper;
    private ProgressDialog waitDialog;
    private ProgressDialog savingDialog;
    private AlertDialog dataLooseWarn;

    private ImageLoader imageLoader;

    private Date birthday;
    private String avatarUrl;

    private EventBus eventBus = EventBus.getDefault();

    private boolean dataLooseWarnShowing = false;
    private boolean loadingDialogShowing = false;
    private boolean savingDialogShowing = false;

    public static final class BundleKeys {
        public final static String USER_DATA = "USER_DATA";
        public final static String IMAGE_URI = "IMAGE_URI";
        public static final String DATA_LOOSE_WARN = "DATA_LOOSE_WARN";
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
        public static final String LOADING_DIALOG = "LOADING_DIALOG";
        public static final String SAVING_DIALOG = "LOADING_DIALOG";
    }

    public static final class IntentExtras {
        public final static String USER_ID = "USER_ID";
        public final static String REGISTRATION_DATA = "REGISTRATION_DATA";
    }

    @Override
    public AccountManagerFuture<Bundle> getAuthTokenAndCheck(AccountManagerCallback<Bundle> cb) {
        return super.getAuthTokenAndCheck(new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                getUserInfoWithWaitDialog();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_profile_edit);

        uid = getIntent().getStringExtra(IntentExtras.USER_ID);
//        Assert.assertNotSame("UID is null", uid, null);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        imageLoader = ImageLoaderInitializer.getImageLoader(this);

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
        birthdayText = (Button) findViewById(R.id.birthday);
        aboutText = (EditText) findViewById(R.id.about);

        List<GenderInfo> genders = new ArrayList<>();
        genders.add(new GenderInfo(this, UserEntry.Gender.None));
        genders.add(new GenderInfo(this, UserEntry.Gender.Female));
        genders.add(new GenderInfo(this, UserEntry.Gender.Male));
        genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genders);
        genderSpinner.setAdapter(genderAdapter);

        saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Keyboard.hideSoftKeyboard(EditProfileActivity.this, getWindow().getDecorView());
                showSavingDialog();
                saveData();
            }
        });

        birthdayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();

                if (birthday != null)
                    calendar.setTime(birthday);

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
        CallbacksKeeper callbacksKeeper = CallbacksKeeper.getInstance();
        callbacksKeeper.addCallback(TAG, OperationType.USER_EDIT, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                hideSavingDialog();

                Toaster.toast(EditProfileActivity.this, R.string.profile_saved);

                String username = data.getString(EditUserExtras.USERNAME);

                Intent intent = new Intent();
                if (getIntent().hasExtra(IntentExtras.REGISTRATION_DATA)) {
                    intent.putExtras(getIntent().getBundleExtra(IntentExtras.REGISTRATION_DATA));
                }
                intent.putExtra(LoginRegisterExtras.USERNAME, username);

                eventBus.post(new ProfileEditedEvent());

                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                hideSavingDialog();
                int genericError = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (genericError) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        getAuthTokenAndCheck(null);
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

                int serverErrorCode = data.getInt(ErrorsExtras.SERVER_ERROR_CODE, -1);
                switch (serverErrorCode) {
                    case EditUserExtras.ErrorCodes.USERNAME_TAKEN:
                        String username = data.getString(EditUserExtras.USERNAME);
                        Toaster.toastLong(getApplicationContext(), String.format(getString(R.string.username_taken), username));
                        return;
                    case EditUserExtras.ErrorCodes.USERNAME_REQUIRED:
                        Toaster.toast(getApplicationContext(), getString(R.string.username_required));
                        return;
                    default:
                        break;
                }
                Toaster.toastLong(EditProfileActivity.this, "Profile not saved: " + message);
            }
        });
        callbacksKeeper.addCallback(TAG, OperationType.USER_GET, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                retrieveUserTask = new RetrieveUserTask();
                retrieveUserTask.execute();
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        getAuthTokenAndCheck(null);
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

        if (savedInstanceState == null) {
            showLoadingDialog();

            Bundle registrationData = getIntent().getBundleExtra(IntentExtras.REGISTRATION_DATA);
            if (registrationData != null) {
                // we are at the last phase of registration
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle(R.string.end_registration_alert)
                        .setMessage(R.string.end_registration_alert_message)
                        .setIcon(R.drawable.logo)
                        .setCancelable(false)
                        .setNegativeButton(R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();

                String token = registrationData.getString(ServiceCallback.LoginRegisterExtras.TOKEN);
                // pass token explicitly
                serviceHelper.getUser(TAG, uid, token, CallbacksKeeper.getInstance().getCallback(TAG, OperationType.USER_GET));
            } else {
                getUserInfo();
            }
            new RetrieveUserTask().execute();
        } else {
            serviceHelper.restoreOperationsState(savedInstanceState, BundleKeys.PENDING_OPERATIONS, CallbacksKeeper.getInstance(), TAG);
            UserData userData = savedInstanceState.getParcelable(BundleKeys.USER_DATA);
            if (userData != null) {
                deserialize(userData);
                if (userData.getAvatar() == null) {
                    avatarUrl = savedInstanceState.getString(BundleKeys.IMAGE_URI);
                    if (avatarUrl != null) {
                        imageLoader.displayImage(ApiUI.resolveUrl(avatarUrl), profileImage);
                    }
                }
            }
            dataLooseWarnShowing = savedInstanceState.getBoolean(BundleKeys.DATA_LOOSE_WARN);
            loadingDialogShowing = savedInstanceState.getBoolean(BundleKeys.LOADING_DIALOG);
            savingDialogShowing = savedInstanceState.getBoolean(BundleKeys.SAVING_DIALOG);
        }

        if (dataLooseWarnShowing) {
            showDataLooseWarning();
        }

        if (loadingDialogShowing && !waitDialog.isShowing()) {
            showLoadingDialog();
        }

        if (savingDialogShowing && !savingDialog.isShowing()) {
            showSavingDialog();
        }
    }

    private void getUserInfo() {
        serviceHelper.getUser(TAG, uid, CallbacksKeeper.getInstance().getCallback(TAG, OperationType.USER_GET));
    }

    private void getUserInfoWithWaitDialog() {
        showLoadingDialog();
        getUserInfo();
    }

    private void showLoadingDialog() {
        waitDialog = ProgressDialog.show(this, getString(R.string.loading_profile), getString(R.string.please_wait),
                true, true, new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        loadingDialogShowing = false;
                        dialog.dismiss();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
        loadingDialogShowing = true;
    }

    private void hideLoadingDialog() {
        if (waitDialog != null && waitDialog.isShowing()) {
            waitDialog.dismiss();
        }
        loadingDialogShowing = false;
    }

    private void showSavingDialog() {
        savingDialog = ProgressDialog.show(EditProfileActivity.this, getString(R.string.saving_profile), getString(R.string.please_wait),
                true, true, new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // just close dialog, request is already sent anyway
                        hideSavingDialog();
                    }
                });
        savingDialogShowing = true;
    }

    private void hideSavingDialog() {
        if (savingDialog != null && savingDialog.isShowing()) {
            savingDialog.dismiss();
        }
        savingDialogShowing = false;
    }

    private void showDataLooseWarning() {
        dataLooseWarnShowing = true;
        dataLooseWarn = new Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.editing_profile))
                .setMessage(getString(R.string.exit_profile_editing))
                .setPositiveButton(getString(R.string.exit), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dataLooseWarnShowing = false;
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void saveData() {
        UserData userData = serialize();

        Bundle registrationData = getIntent().getBundleExtra(IntentExtras.REGISTRATION_DATA);
        if (registrationData != null) {
            // we are at the last phase of registration
            String token = registrationData.getString(ServiceCallback.LoginRegisterExtras.TOKEN);
            // pass token explicitly
            serviceHelper.editUser(TAG, userData, token, CallbacksKeeper.getInstance().getCallback(TAG, OperationType.USER_EDIT));
        } else {
            serviceHelper.editUser(TAG, userData, CallbacksKeeper.getInstance().getCallback(TAG, OperationType.USER_EDIT));
        }
    }

    private UserData serialize() {
        UserData userData = new UserData();

        ImageData imageData = imageSelector.getImageData();
        if (imageData != null)
            userData.setAvatar(imageData);

        userData.setGender(genderAdapter.getItem(genderSpinner.getSelectedItemPosition()).getValue());
        userData.setUsername(nickNameText.getText().toString());
        userData.setFirstName(nameText.getText().toString());
        userData.setLastName(surnameText.getText().toString());

        if (birthday != null)
            userData.setBirthday(birthday);

        userData.setCountry(countryText.getText().toString());
        userData.setCity(cityText.getText().toString());
        userData.setAbout(aboutText.getText().toString());

        return userData;
    }

    private void deserialize(UserData userData) {
        imageSelector.restoreFromImageData(userData.getAvatar());

        UserEntry.Gender genderValue = userData.getGender();
        int position = getGenderPositionByValue(genderValue);
        genderSpinner.setSelection(position);

        nickNameText.setText(userData.getUsername());
        nameText.setText(userData.getFirstName());
        surnameText.setText(userData.getLastName());

        if (userData.getBirthday() != null) {
            try {
                birthday = userData.getBirthdayDate();
            } catch (ParseException e) {
                String date = DateFormat.getDateFormat(EditProfileActivity.this).format(birthday);
                birthdayText.setText(date);
            }
        }

        if (birthday != null) {
            String date = DateFormat.getDateFormat(EditProfileActivity.this).format(birthday);
            birthdayText.setText(date);
        }

        countryText.setText(userData.getCountry());
        cityText.setText(userData.getCity());
        aboutText.setText(userData.getAbout());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        serviceHelper.saveOperationsState(outState, BundleKeys.PENDING_OPERATIONS);
        outState.putParcelable(BundleKeys.USER_DATA, serialize());
        outState.putString(BundleKeys.IMAGE_URI, avatarUrl);
        outState.putBoolean(BundleKeys.DATA_LOOSE_WARN, dataLooseWarnShowing);
        outState.putBoolean(BundleKeys.LOADING_DIALOG, loadingDialogShowing);
        outState.putBoolean(BundleKeys.SAVING_DIALOG, savingDialogShowing);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageSelector.onActivityResult(requestCode, resultCode, data);
    }

    class RetrieveUserTask extends AsyncTask<Void, Void, UserEntry> {

        @Override
        protected UserEntry doInBackground(Void... params) {
            entry = byUId(uid);

            return entry;
        }

        @Override
        protected void onPostExecute(UserEntry entry) {
            if (entry == null) return;
            avatarUrl = entry.getAvatar();
            if (avatarUrl != null) {
                imageLoader.displayImage(ApiUI.resolveUrl(avatarUrl), profileImage);
            }

            nickNameText.setText(entry.getUsername());
            nameText.setText(entry.getFirstName());
            surnameText.setText(entry.getLastName());
            countryText.setText(entry.getCountry());
            cityText.setText(entry.getCity());
            aboutText.setText(entry.getAbout());

            birthday = entry.getBirthday();
            if (birthday != null) {
                String date = DateFormat.getDateFormat(EditProfileActivity.this).format(entry.getBirthday());
                birthdayText.setText(date);
            }

            UserEntry.Gender genderValue = entry.getGender();
            int position = getGenderPositionByValue(genderValue);
            genderSpinner.setSelection(position);

            // wait for render event
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideLoadingDialog();
                }
            }, WAIT_DIALOG_DISMISS_DELAY);
        }
    }


    public int getGenderPositionByValue(UserEntry.Gender genderValue) {
        for (int i = 0; i < genderAdapter.getCount(); ++i) {
            GenderInfo gender = genderAdapter.getItem(i);
            if (gender.getValue() == genderValue)
                return i;
        }

        return 0;
    }

    @Override
    public void onBackPressed() {
        showDataLooseWarning();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (waitDialog != null && waitDialog.isShowing()) {
            waitDialog.dismiss();
        }
        waitDialog = null;

        if (savingDialog != null && savingDialog.isShowing()) {
            savingDialog.dismiss();
        }
        savingDialog = null;

        if (dataLooseWarn != null && dataLooseWarn.isShowing()) {
            dataLooseWarn.dismiss();
        }
        dataLooseWarn = null;
    }

    public static class ProfileEditedEvent {

    }
}
