package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.techteam.decider.R;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.gui.activities.AddQuestionActivity;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.activities.QuestionDetailsActivity;
import org.techteam.decider.gui.adapters.CategoriesListAdapter;
import org.techteam.decider.gui.adapters.ColoredAdapter;
import org.techteam.decider.gui.loaders.CategoriesLoader;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.gui.widget.SlidingTabLayout;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;

import java.util.LinkedList;
import java.util.List;

public class MainFragment
        extends Fragment
        implements OnCategorySelectedListener {

    public static final String TAG = MainFragment.class.toString();
    private static final int ADD_QUESTION = 0;
    private MainActivity activity;

    private Toolbar toolbar;
    private CategoriesListAdapter categoriesListAdapter;
    private List<OnCategorySelectedListener> onCategorySelectedListeners = new LinkedList<>();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    private FloatingActionButton createPostButton;

    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();
    private ServiceHelper serviceHelper;

    private LoaderManager.LoaderCallbacks<Cursor> categoriesLoaderCallbacks = new LoaderCallbacksImpl();
    //private Map<Integer, CategoryEntry> selectedCategories = new HashMap<>();

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = (MainActivity) activity;
        serviceHelper = new ServiceHelper(activity);

        callbacksKeeper.addCallback(OperationType.CATEGORIES_GET, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                getLoaderManager().restartLoader(LoaderIds.CATEGORIES_LOADER, null, categoriesLoaderCallbacks);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.Codes.INVALID_TOKEN:
                        MainFragment.this.activity.getAuthTokenOrExit(null);
                        return;
                    case ErrorsExtras.Codes.SERVER_ERROR:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.server_problem);
                        return;
                }
                String msg = "Categories error. " + message;
                Toaster.toast(getActivity().getApplicationContext(), msg);
                System.out.println(msg);
            }
        });

        serviceHelper.getCategories(getResources().getConfiguration().locale.toString(), callbacksKeeper.getCallback(OperationType.CATEGORIES_GET));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            serviceHelper.restoreOperationsState(savedInstanceState, BundleKeys.PENDING_OPERATIONS, callbacksKeeper);
        }

        toolbar = (Toolbar) this.activity.findViewById(R.id.main_toolbar);
        this.activity.setSupportActionBar(toolbar);

//        ActionBar actionBar = this.activity.getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeButtonEnabled(true);
//        }

        // setup drawer
        categoriesListAdapter = new CategoriesListAdapter(null, this.getActivity(), this);
        activity.createDrawer(toolbar, categoriesListAdapter);

        // sections
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) this.activity.findViewById(R.id.sections_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) this.activity.findViewById(R.id.sections_pager_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        mSlidingTabLayout.setBackgroundColor(this.activity.getResources().getColor(R.color.primary));
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(android.R.color.white);
            }
        });

        // setup plus button
        createPostButton = (FloatingActionButton) this.activity.findViewById(R.id.create_post_button);
        createPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddQuestionActivity.class);
                startActivityForResult(intent, ADD_QUESTION);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        serviceHelper.saveOperationsState(outState, BundleKeys.PENDING_OPERATIONS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_QUESTION && resultCode == Activity.RESULT_OK) {
            int qid = data.getIntExtra(AddQuestionActivity.QUESTION_ID, -1);
            Intent intent = new Intent(getActivity(), QuestionDetailsActivity.class);
            intent.putExtra(QuestionDetailsActivity.BundleKeys.Q_ID, qid);
            startActivity(intent);
        }
    }

    @Override
    public void categorySelected(CategoryEntry category, boolean isChecked) {
        Toaster.toast(getActivity(), "Selected");
        category.setSelectedAsync(isChecked);
        int currentFragment = mViewPager.getCurrentItem();
        QuestionsListFragment f =(QuestionsListFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, currentFragment);
        f.categorySelected(category, isChecked);
//        if (onCategorySelectedListeners != null) {
//            for (OnCategorySelectedListener listener : onCategorySelectedListeners) {
//                if (listener != null) {
//                    listener.categorySelected(category, isChecked);
//                }
//            }
//        }
    }

    public void invalidatePages() {
        if (mSectionsPagerAdapter != null) {
            mSectionsPagerAdapter.invalidateFragments();
        }
    }

    //TODO: refactor this out
    private class SectionsPagerAdapter extends FragmentStatePagerAdapter implements ColoredAdapter {

        private Fragment[] fragments;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new Fragment[getCount()];
        }

        @Override
        public Fragment getItem(int position) {
            if (fragments[position] == null) {
                QuestionsListFragment f = QuestionsListFragment.create(ContentSection.fromInt(position));
                onCategorySelectedListeners.add(f);
                fragments[position] = f;
            }
            return fragments[position];
        }

        @Override
        public int getCount() {
            return ContentSection.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int resId = ContentSection.fromInt(position).getResId();
            return getString(resId);
        }

        @Override
        public int getTextColor() {
            return android.R.color.white;
        }

        public void invalidateFragments() {
//            for (int position = 0; position < getCount(); ++position) {
//                if (fragments[position] != null) {
//                    destroyItem(null, position, fragments[position]);
//                }
//            }
            fragments = new Fragment[getCount()];
            onCategorySelectedListeners.clear();
        }
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
            categoriesListAdapter.swapCursor(newCursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            categoriesListAdapter.swapCursor(null);
        }
    }
}
