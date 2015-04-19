package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import org.techteam.decider.R;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.adapters.ColoredAdapter;
import org.techteam.decider.gui.loaders.CategoriesLoader;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.gui.views.WrappingViewPager;
import org.techteam.decider.gui.widget.SlidingTabLayout;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;

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
    //TODO

    private Button createButton;

    // categories
    private LoaderManager.LoaderCallbacks<Cursor> categoriesLoaderCallbacks = new LoaderCallbacksImpl();
    private SimpleCursorAdapter categoriesSpinnerAdapter;
    private ServiceHelper serviceHelper;

    // question types
    private static final int PAGES_COUNT = 2;
    private QuestionTypePagerAdapter mQuestionTypePagerAdapter;
    private SlidingTabLayout mQuestionTypeTabLayout;
    private WrappingViewPager mQuestionTypePager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_question, container, false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = (MainActivity) activity;
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
        serviceHelper.getCategories(
                v.getResources().getConfiguration().locale.toString(),
                new ServiceCallback() {
                    @Override
                    public void onSuccess(String operationId, Bundle data) {
                        getLoaderManager().restartLoader(LoaderIds.CATEGORIES_LOADER, null, categoriesLoaderCallbacks);
                    }

                    @Override
                    public void onError(String operationId, Bundle data, String message) {

                    }
                }
        );

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
}
