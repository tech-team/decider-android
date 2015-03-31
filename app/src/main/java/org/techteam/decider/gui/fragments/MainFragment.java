package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.techteam.decider.R;
import org.techteam.decider.content.ContentCategory;
import org.techteam.decider.content.ContentProvider;
import org.techteam.decider.content.PostEntry;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.adapters.CategoriesListAdapter;
import org.techteam.decider.gui.adapters.Category;
import org.techteam.decider.gui.adapters.PostsListAdapter;
import org.techteam.decider.gui.loaders.ContentLoader;
import org.techteam.decider.gui.loaders.LoadIntention;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MainFragment
        extends Fragment {

    public static final String TAG = MainFragment.class.toString();
    private MainActivity activity;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;

    private CategoriesListAdapter categoriesListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

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

        Toolbar toolbar = (Toolbar) this.activity.findViewById(R.id.toolbar_actionbar);
        this.activity.setSupportActionBar(toolbar);

//        ActionBar actionBar = this.activity.getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeButtonEnabled(true);
//        }

        // setup drawer
        mDrawerLayout = (DrawerLayout) this.activity.findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) this.activity.findViewById(R.id.categories_list);

        // Set the adapter for the list view
        //TODO: load categories via CursorLoader from server
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(new ContentCategory("Test 1", 1), false));
        categories.add(new Category(new ContentCategory("Test 2", 2), false));

        categoriesListAdapter = new CategoriesListAdapter(this.activity.getBaseContext(), categories);
        mDrawerList.setAdapter(categoriesListAdapter);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            //selectItem(position);
        }
    }
}
