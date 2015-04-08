package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.ContentCategory;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.adapters.CategoriesListAdapter;
import org.techteam.decider.gui.adapters.Category;
import org.techteam.decider.gui.loaders.CategoriesLoader;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.gui.widget.SlidingTabLayout;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;

import java.util.ArrayList;
import java.util.List;

public class MainFragment
        extends Fragment {

    public static final String TAG = MainFragment.class.toString();
    private MainActivity activity;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
//    private ListView mDrawerList;

    private CategoriesListAdapter categoriesListAdapter;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    private FloatingActionButton createPostButton;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView recyclerView;

    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();
    private ServiceHelper serviceHelper;

    private LoaderManager.LoaderCallbacks<Cursor> categoriesLoaderCallbacks = new LoaderCallbacksImpl();

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

        callbacksKeeper.addCallback(OperationType.GET_CATEGORIES, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                getLoaderManager().restartLoader(LoaderIds.CATEGORIES_LOADER, null, categoriesLoaderCallbacks);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                String msg = "Error. " + message;
                Toaster.toast(getActivity().getApplicationContext(), msg);
                System.out.println(msg);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            serviceHelper.restoreOperationsState(savedInstanceState, BundleKeys.PENDING_OPERATIONS, callbacksKeeper);
        }

        Toolbar toolbar = (Toolbar) this.activity.findViewById(R.id.main_toolbar);
        this.activity.setSupportActionBar(toolbar);

//        ActionBar actionBar = this.activity.getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeButtonEnabled(true);
//        }

        // setup drawer
        mDrawerLayout = (DrawerLayout) this.activity.findViewById(R.id.drawer_layout);
//        mDrawerList = (ListView) this.activity.findViewById(R.id.categories_list);
        recyclerView = (RecyclerView) this.activity.findViewById(R.id.categories_recycler);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        // Set the adapter for the list view
        //TODO: load categories via CursorLoader from server
//        List<Category> categories = new ArrayList<>();
//        categories.add(new Category(new ContentCategory(1, "Test 1"), false));
//        categories.add(new Category(new ContentCategory(2, "Test 2"), false));

        categoriesListAdapter = new CategoriesListAdapter(null, this.activity.getBaseContext());
        recyclerView.setAdapter(categoriesListAdapter);

        // Set the list's click listener
//        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this.activity,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                toolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

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
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return R.color.primary;
            }
        });

        // setup plus button
        createPostButton = (FloatingActionButton) this.activity.findViewById(R.id.create_post_button);
        createPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .add(R.id.content_frame, new AddPostFragment())
                        .addToBackStack("mainFragment")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });

        serviceHelper.getCategories(getResources().getConfiguration().locale.toLanguageTag(), callbacksKeeper.getCallback(OperationType.GET_CATEGORIES));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        serviceHelper.saveOperationsState(outState, BundleKeys.PENDING_OPERATIONS);
    }

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // TODO: static create() method with section arg
            return new PostsListFragment();
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
