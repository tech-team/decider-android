package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
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

import com.android.camera.CropImageIntentBuilder;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.adapters.ColoredAdapter;
import org.techteam.decider.gui.loaders.CategoriesLoader;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.gui.views.WrappingViewPager;
import org.techteam.decider.gui.widget.SlidingTabLayout;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.UploadImageRequest;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;

import java.io.File;

public class AddQuestionFragment extends Fragment{
    private MainActivity activity;

    // child controls
    private EditText postText;
    private Spinner categoriesSpinner;
    private CheckBox anonymityCheckBox;

    // text choices
    private EditText textChoice1;
    private EditText textChoice2;

    // image choices
    private ImageView imageChoice1;
    private ImageView imageChoice2;

    private Button createButton;

    // categories
    private LoaderManager.LoaderCallbacks<Cursor> categoriesLoaderCallbacks = new LoaderCallbacksImpl();
    private SimpleCursorAdapter categoriesSpinnerAdapter;

    // question types
    private static final int PAGES_COUNT = 2;
    private QuestionTypePagerAdapter mQuestionTypePagerAdapter;
    private SlidingTabLayout mQuestionTypeTabLayout;
    private WrappingViewPager mQuestionTypePager;

    // image selector stuff
    private static final int TAKE_PICTURE = 1;
    private static final int SELECT_IMAGE = 2;
    private static final int CROP_IMAGE = 3;

    private static final int ASPECT_WIDTH = 9;
    private static final int ASPECT_HEIGHT = 16;

    private static final int CROPPED_WIDTH = 800;
    private static final int CROPPED_HEIGHT = CROPPED_WIDTH * ASPECT_HEIGHT / ASPECT_WIDTH;

    private static final String CROPPED_FILE_EXTENSION = ".cropped.jpg";

    private ImageHolder currentImageHolder;

    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();
    private ServiceHelper serviceHelper;

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
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

        callbacksKeeper.addCallback(OperationType.UPLOAD_IMAGE, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                String uid = data.getString(ImageUploadExtras.UID); // TODO: save this
                Toaster.toast(AddQuestionFragment.this.activity.getBaseContext(), "Upload ok. Image uid = " + uid);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                Toaster.toast(AddQuestionFragment.this.activity.getBaseContext(), "Upload failed: " + message);
            }
        });

        callbacksKeeper.addCallback(OperationType.CREATE_QUESTION, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toast(AddQuestionFragment.this.activity.getBaseContext(), "Create question ok");
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                Toaster.toast(AddQuestionFragment.this.activity.getBaseContext(), "Create question failed: " + message);
            }
        });
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

        // text choices
        textChoice1 = (EditText) v.findViewById(R.id.add_post_text_choice1);
        textChoice2 = (EditText) v.findViewById(R.id.add_post_text_choice2);

        // image choices
        imageChoice1 = (ImageView) v.findViewById(R.id.add_post_image_choice1);
        imageChoice2 = (ImageView) v.findViewById(R.id.add_post_image_choice2);

        imageChoice1.setOnClickListener(new ImageChoiceClickListener(imageChoice1));
        imageChoice2.setOnClickListener(new ImageChoiceClickListener(imageChoice2));

        createButton = (Button) v.findViewById(R.id.add_post_send_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (createPost()) {
                    getActivity().onBackPressed();
                }
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

        // Set up the ViewPager with the adapter
        mQuestionTypePagerAdapter = new QuestionTypePagerAdapter();

        mQuestionTypePager = (WrappingViewPager) v.findViewById(R.id.question_type_pager);
        mQuestionTypePager.setAdapter(mQuestionTypePagerAdapter);

        mQuestionTypeTabLayout = (SlidingTabLayout) v.findViewById(R.id.question_type_pager_tabs);
        mQuestionTypeTabLayout.setDistributeEvenly(true);
        mQuestionTypeTabLayout.setViewPager(mQuestionTypePager);

        if (savedInstanceState == null) {

        } else {
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

    private boolean createPost() {
        // collect data
        String message = postText.getText().toString();
        //TODO: get category from spinner's adapter
        //categoriesSpinner
        boolean anonimity = anonymityCheckBox.isChecked();

        //TODO: check current question type
        // text choices
        String choice1 = textChoice1.getText().toString();
        String choice2 = textChoice2.getText().toString();

        // validate data
        if (message.isEmpty() || choice1.isEmpty() || choice2.isEmpty()) {
            Toaster.toast(getActivity(), R.string.fill_all_fields);
            return false;
        }

        // send if valid
        //TODO: send question

        return true;
    }


    private class LoaderCallbacksImpl implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if  (id == LoaderIds.CATEGORIES_LOADER) {

                if (args != null) {
                }

                return new CategoriesLoader(getActivity());
            }
            throw new IllegalArgumentException("Loader with given id is not found");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
            CategoriesLoader contentLoader = (CategoriesLoader) loader;
            categoriesSpinnerAdapter.swapCursor(newCursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            categoriesSpinnerAdapter.swapCursor(null);
        }
    }

    //TODO: refactor this out
    private class QuestionTypePagerAdapter extends PagerAdapter implements ColoredAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View child = mQuestionTypePager.getChildAt(position);

            container.setMinimumHeight(child.getHeight());

            return child;
        }

        @Override
        public int getCount() {
            return PAGES_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            View child = mQuestionTypePager.getChildAt(position);
            String title = (String) child.getTag();

            return title;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        @Override
        public int getTextColor() {
            return android.R.color.black;
        }
    }

    private class ImageChoiceClickListener implements View.OnClickListener {
        private ImageView imageView;

        public ImageChoiceClickListener(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public void onClick(View v) {
            AddQuestionFragment.this.currentImageHolder = new ImageHolder(imageView);

            final CharSequence fromCamera = getString(R.string.take_photo);
            final CharSequence fromGallery = getString(R.string.choose_from_gallery);
            final CharSequence items[] = { fromCamera, fromGallery };

            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (which == -1)
                                return;

                            if (items[which].equals(fromCamera)) {
                                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(takePicture, TAKE_PICTURE);
                            } else if (items[which].equals(fromGallery)) {
                                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(pickPhoto, SELECT_IMAGE);
                            }
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_IMAGE || requestCode == TAKE_PICTURE) {
                Uri selectedImage = data.getData();
                currentImageHolder.setSource(selectedImage);

                // generate cropped path
                File cacheDir = getActivity().getCacheDir();
                File croppedFile = new File(cacheDir,
                        selectedImage.getLastPathSegment() + CROPPED_FILE_EXTENSION);

                currentImageHolder.setCropped(Uri.fromFile(croppedFile));

                cropImage(currentImageHolder);
            } else if (requestCode == CROP_IMAGE) {
                showImage(currentImageHolder);
                sendImage(currentImageHolder);
            }
        }
    }

    private void cropImage(ImageHolder imageHolder) {
        CropImageIntentBuilder cropBuilder = new CropImageIntentBuilder(
                ASPECT_WIDTH, ASPECT_HEIGHT,
                CROPPED_WIDTH, CROPPED_HEIGHT,
                imageHolder.getCropped());
        cropBuilder.setSourceImage(imageHolder.getSource());

        startActivityForResult(cropBuilder.getIntent(getActivity()), CROP_IMAGE);
    }

    private void showImage(ImageHolder imageHolder) {
        currentImageHolder.getImageView()
                .setImageURI(imageHolder.getCropped());
    }

    private void sendImage(ImageHolder imageHolder) {
        Uri original = imageHolder.getSource();
        Uri preview = imageHolder.getCropped();
        UploadImageRequest.Image image = new UploadImageRequest.Image(original, preview);
        serviceHelper.uploadImage(image, callbacksKeeper.getCallback(OperationType.UPLOAD_IMAGE));
    }

}
