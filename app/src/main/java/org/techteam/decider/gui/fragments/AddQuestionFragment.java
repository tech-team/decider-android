package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.question.ImageQuestionData;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.loaders.CategoriesLoader;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;
import org.techteam.decider.util.image_selector.ActivityStarter;
import org.techteam.decider.util.image_selector.ImageHolder;
import org.techteam.decider.util.image_selector.ImageSelector;

public class AddQuestionFragment extends Fragment implements ActivityStarter {
    private static final String TAG = AddQuestionFragment.class.getName();
    private MainActivity activity;

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
    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();
    private ServiceHelper serviceHelper;

    private ProgressDialog waitDialog;

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
        public static final String QUESTION_DATA = "QUESTION_DATA";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_question, container, false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = (MainActivity) activity;

        //TODO: is this needed? i though posting now is only 1 request?
        callbacksKeeper.addCallback(OperationType.UPLOAD_IMAGE, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                String uid = data.getString(ImageUploadExtras.UID);
                int imageOrdinalId = data.getInt(ImageUploadExtras.IMAGE_ORDINAL_ID);
                getImageHolderById(imageOrdinalId).setUid(uid);

                Toaster.toastLong(AddQuestionFragment.this.activity.getBaseContext(), "Upload ok. Image uid = " + uid + ". OrdinalId = " + imageOrdinalId);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int imageOrdinalId = data.getInt(ImageUploadExtras.IMAGE_ORDINAL_ID);
                Toaster.toastLong(AddQuestionFragment.this.activity.getBaseContext(), "Upload failed: " + message + ". OrdinalId = " + imageOrdinalId);
            }
        });

        callbacksKeeper.addCallback(OperationType.CREATE_QUESTION, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                waitDialog.dismiss();
                Toaster.toast(AddQuestionFragment.this.activity.getBaseContext(), "Create question ok");
                int qid = data.getInt(CreateQuestionExtras.QID, -1);
                if (qid == -1) {
                    // do something in this shitty situation
                } else {

                }
                getActivity().onBackPressed();
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                waitDialog.dismiss();
                Toaster.toast(AddQuestionFragment.this.activity.getBaseContext(), "Create question failed: " + message);
            }
        });
    }

    private ImageHolder getImageHolderById(int id) {
        if (leftImageSelector.getImageHolder().getOrdinal() == id)
            return leftImageSelector.getImageHolder();
        else
            return rightImageSelector.getImageHolder();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();
        assert v != null;

        // setup toolbar
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.post_add_toolbar);
        this.activity.setSupportActionBar(toolbar);

        ActionBar actionBar = this.activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // find controls
        postText = (EditText) v.findViewById(R.id.add_post_text);
        categoriesSpinner = (Spinner) v.findViewById(R.id.add_post_category_spinner);
        
        anonymityCheckBox = (CheckBox) v.findViewById(R.id.add_post_anonymity_checkbox);

        imageChoice1 = (ImageView) v.findViewById(R.id.add_post_image_choice1);
        imageChoice2 = (ImageView) v.findViewById(R.id.add_post_image_choice2);


        //TODO: fix ordinals, they maybe wrong
        leftImageSelector = new ImageSelector(activity, this, imageChoice1, (short) 0);
        rightImageSelector = new ImageSelector(activity, this, imageChoice2, (short) 1);

        createButton = (Button) v.findViewById(R.id.add_post_send_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPost();
            }
        });

        // setup categories list
        Context context = v.getContext();
        serviceHelper = new ServiceHelper(context);
        getLoaderManager().restartLoader(LoaderIds.CATEGORIES_LOADER, null, categoriesLoaderCallbacks);

        categoriesSpinnerAdapter = new SimpleCursorAdapter(
                context,
                R.layout.categories_spinner_item,
                null,
                new String[] {CategoryEntry.LOCALIZED_LABEL_FIELD},
                new int[] {R.id.category_title},
                0);

        categoriesSpinner.setAdapter(categoriesSpinnerAdapter);

        if (savedInstanceState != null) {
            currentQuestionData = savedInstanceState.getParcelable(BundleKeys.QUESTION_DATA);
            serviceHelper.restoreOperationsState(savedInstanceState,
                    BundleKeys.PENDING_OPERATIONS,
                    callbacksKeeper);
            restoreQuestion();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        currentQuestionData = gatherQuestionData();
        outState.putParcelable(BundleKeys.QUESTION_DATA, currentQuestionData);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // call both, they will compare ordinals
        leftImageSelector.onActivityResult(requestCode, resultCode, data);
        rightImageSelector.onActivityResult(requestCode, resultCode, data);
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
        currentQuestionData = gatherQuestionData();

        // validate data
        if (currentQuestionData.getText().isEmpty()) {
            Toaster.toast(getActivity(), R.string.fill_all_fields);
            return false;
        }
        if (currentQuestionData.getPicture1() == null || currentQuestionData.getPicture2() == null) {
            Toaster.toast(getActivity(), R.string.pictures_fil);
            return false;
        }

        waitDialog = ProgressDialog.show(activity, getString(R.string.creating_post), getString(R.string.please_wait), true);
        serviceHelper.createQuestion(currentQuestionData, callbacksKeeper.getCallback(OperationType.CREATE_QUESTION));

        return true;
    }


    private class LoaderCallbacksImpl implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if  (id == LoaderIds.CATEGORIES_LOADER) {
                return new CategoriesLoader(getActivity());
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
