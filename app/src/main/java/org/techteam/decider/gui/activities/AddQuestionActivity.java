package org.techteam.decider.gui.activities;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.question.ImageQuestionData;
import org.techteam.decider.gui.activities.lib.AuthTokenGetter;
import org.techteam.decider.gui.loaders.CategoriesLoader;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Keyboard;
import org.techteam.decider.util.Toaster;
import org.techteam.decider.util.image_selector.ActivityStarter;
import org.techteam.decider.util.image_selector.ImageHolder;
import org.techteam.decider.util.image_selector.ImageSelector;

public class AddQuestionActivity extends ToolbarActivity implements ActivityStarter, AuthTokenGetter {
    private static final String TAG = AddQuestionActivity.class.getName();

    public static final String QUESTION_ID = "QUESTION_ID";

    // child controls
    private EditText postText;
    private Spinner categoriesSpinner;
    private CheckBox anonymityCheckBox;

    // image choices
    private ImageView imageChoice1;
    private ImageView imageChoice2;

    private Button createButton;

    // categories
    private LoaderManager.LoaderCallbacks<Cursor> categoriesLoaderCallbacks = new LoaderCallbacksImpl();
    private SimpleCursorAdapter categoriesSpinnerAdapter;

    private ImageSelector leftImageSelector;
    private ImageSelector rightImageSelector;

    private ImageQuestionData currentQuestionData;
    private ServiceHelper serviceHelper;

    private ProgressDialog waitDialog;

    private boolean dataLooseWarnShowing = false;

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
        public static final String QUESTION_DATA = "QUESTION_DATA";
        public static final String DATA_LOOSE_WARN = "DATA_LOOSE_WARN";
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.fragment_add_question);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.post_add_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // find controls
        postText = (EditText) findViewById(R.id.add_post_text);
        categoriesSpinner = (Spinner) findViewById(R.id.add_post_category_spinner);
        
        anonymityCheckBox = (CheckBox) findViewById(R.id.add_post_anonymity_checkbox);

        imageChoice1 = (ImageView) findViewById(R.id.add_post_image_choice1);
        imageChoice2 = (ImageView) findViewById(R.id.add_post_image_choice2);


        //TODO: fix ordinals, they maybe wrong
        leftImageSelector = new ImageSelector(this, this, imageChoice1, (byte) 0);
        rightImageSelector = new ImageSelector(this, this, imageChoice2, (byte) 1);

        createButton = (Button) findViewById(R.id.add_post_send_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPost();
            }
        });

        // setup categories list
        serviceHelper = new ServiceHelper(this);
        getSupportLoaderManager().restartLoader(LoaderIds.CATEGORIES_LOADER, null, categoriesLoaderCallbacks);

        categoriesSpinnerAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                null,
                new String[] {CategoryEntry.LOCALIZED_LABEL_FIELD},
                new int[] {android.R.id.text1},
                0);

        categoriesSpinner.setAdapter(categoriesSpinnerAdapter);

        CallbacksKeeper callbacksKeeper = CallbacksKeeper.getInstance();
        callbacksKeeper.addCallback(TAG, OperationType.QUESTION_CREATE, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                waitDialog.dismiss();
                Toaster.toast(AddQuestionActivity.this, R.string.question_created);

                Intent result = new Intent();
                int qid = data.getInt(CreateQuestionExtras.QID, -1);
                result.putExtra(QUESTION_ID, qid);

                setResult(Activity.RESULT_OK, result);
                finish();
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

                Toaster.toast(AddQuestionActivity.this, "Create question failed: " + message);
            }
        });

        if (savedInstanceState != null) {
            currentQuestionData = savedInstanceState.getParcelable(BundleKeys.QUESTION_DATA);
            dataLooseWarnShowing = savedInstanceState.getBoolean(BundleKeys.DATA_LOOSE_WARN);
            serviceHelper.restoreOperationsState(savedInstanceState,
                    BundleKeys.PENDING_OPERATIONS,
                    callbacksKeeper, TAG);
            restoreQuestion();
        }

        if (dataLooseWarnShowing) {
            showDataLooseWarning();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        currentQuestionData = gatherQuestionData();
        outState.putParcelable(BundleKeys.QUESTION_DATA, currentQuestionData);
        outState.putBoolean(BundleKeys.DATA_LOOSE_WARN, dataLooseWarnShowing);
        serviceHelper.saveOperationsState(outState, BundleKeys.PENDING_OPERATIONS);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // call both, they will compare ordinals
        leftImageSelector.onActivityResult(requestCode, resultCode, data);
        rightImageSelector.onActivityResult(requestCode, resultCode, data);
    }

    private ImageHolder getImageHolderById(int id) {
        if (leftImageSelector.getImageHolder().getOrdinal() == id)
            return leftImageSelector.getImageHolder();
        else
            return rightImageSelector.getImageHolder();
    }

    private void restoreQuestion() {
        // restoring text
        postText.setText(currentQuestionData.getText());

        // restoring category
        categoriesSpinner.setSelection(currentQuestionData.getCategoryEntrySpinnerId());

        // restoring images
        leftImageSelector.restoreFromImageData(currentQuestionData.getPicture1());
        rightImageSelector.restoreFromImageData(currentQuestionData.getPicture2());

        // restore anonymous
        anonymityCheckBox.setSelected(currentQuestionData.isAnonymous());
    }

    private ImageQuestionData gatherQuestionData() {
        String message = postText.getText().toString();

        Cursor categoryCursor = (Cursor) categoriesSpinner.getSelectedItem();
        CategoryEntry categoryEntry = CategoryEntry.fromCursor(categoryCursor);
        int categoryUid = categoryEntry.getUid();

        boolean anonymity = anonymityCheckBox.isChecked();

        ImageQuestionData data = new ImageQuestionData();

        data.setPicture1(leftImageSelector.getImageData());
        data.setPicture2(rightImageSelector.getImageData());

        data.setText(message);
        data.setAnonymous(anonymity);
        data.setCategoryEntryUid(categoryUid);
        data.setCategoryEntrySpinnerId(categoriesSpinner.getSelectedItemPosition());

        return data;
    }

    private boolean createPost() {
        Keyboard.hideSoftKeyboard(this, getWindow().getDecorView());

        currentQuestionData = gatherQuestionData();

        // validate data
        if (currentQuestionData.getText().isEmpty()) {
            Toaster.toast(this, R.string.fill_all_fields);
            return false;
        }
        if (currentQuestionData.getPicture1() == null || currentQuestionData.getPicture2() == null) {
            Toaster.toast(this, R.string.pictures_fil);
            return false;
        }

        waitDialog = ProgressDialog.show(this, getString(R.string.creating_post), getString(R.string.please_wait),
                true, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // question data is probably already on server so.. just close dialog
                        dialog.dismiss();
                    }
                });
        serviceHelper.createQuestion(TAG, currentQuestionData, CallbacksKeeper.getInstance().getCallback(TAG, OperationType.QUESTION_CREATE));

        return true;
    }

    @Override
    public void onBackPressed() {
        // if fields are empty
        if (postText.getText().toString().isEmpty()
                && leftImageSelector.getImageData() == null
                && rightImageSelector.getImageData() == null) {
            dataLooseWarnShowing = false;
            super.onBackPressed();
            return;
        }

        // if no, ask user if he really wants to exit
        showDataLooseWarning();
    }

    private void showDataLooseWarning() {
        dataLooseWarnShowing = true;
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.creating_post))
                .setMessage(getString(R.string.exit_question_creation))
                .setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dataLooseWarnShowing = false;
                        finish();
                    }

                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private class LoaderCallbacksImpl implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if  (id == LoaderIds.CATEGORIES_LOADER) {
                return new CategoriesLoader(AddQuestionActivity.this);
            }
            throw new IllegalArgumentException("Loader with given id is not found");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
            categoriesSpinnerAdapter.swapCursor(newCursor);
            if (currentQuestionData != null) {
                categoriesSpinner.setSelection(currentQuestionData.getCategoryEntrySpinnerId());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            categoriesSpinnerAdapter.swapCursor(null);
        }
    }
}
