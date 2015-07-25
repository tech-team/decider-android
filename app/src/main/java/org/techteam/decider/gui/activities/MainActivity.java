package org.techteam.decider.gui.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.techteam.decider.R;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.entities.DbHelper;
import org.techteam.decider.content.entities.UserEntry;
import org.techteam.decider.gcm.GcmPreferences;
import org.techteam.decider.gui.CategoriesGetter;
import org.techteam.decider.gui.ServiceHelperGetter;
import org.techteam.decider.gui.adapters.CategoriesListAdapter;
import org.techteam.decider.gui.adapters.ColoredAdapter;
import org.techteam.decider.gui.fragments.OnCategorySelectedListener;
import org.techteam.decider.gui.fragments.QuestionsListFragment;
import org.techteam.decider.gui.loaders.CategoriesLoader;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.gui.widget.SlidingTabLayout;
import org.techteam.decider.misc.NetworkStateReceiver;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.CacheHelper;
import org.techteam.decider.util.ImageLoaderInitializer;
import org.techteam.decider.util.Toaster;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;


public class MainActivity extends ToolbarActivity implements
        OnCategorySelectedListener,
        CategoriesGetter,
        ServiceHelperGetter {
    private static final String TAG = MainActivity.class.getName();
    public static String PACKAGE_NAME;

    public static final int AUTH_REQUEST_CODE = 101;
    private static final int LOGOUT_ID = 1;

    private static final int ADD_QUESTION = 0;
    private static final int QUESTION_DETAILS = 1;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    private FloatingActionButton createPostButton;

    private Toolbar toolbar;
    // drawer related stuff
    private AccountHeader drawerHeader;
    private Drawer drawer;
    private RetrieveUserTask retrieveUserTask;

    private CategoriesListAdapter categoriesListAdapter;

    private ServiceHelper serviceHelper;
    private LoaderManager.LoaderCallbacks<Cursor> categoriesLoaderCallbacks = new LoaderCallbacksImpl();

    private EventBus eventBus = EventBus.getDefault();

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
    }

    @Override
    public ServiceHelper getServiceHelper() {
        return serviceHelper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = getApplicationContext().getPackageName();

        getAuthToken(new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                if (future.isCancelled()) {
                    finish();
                } else {
                    getUserInfo();
                    reloadPages();
                    ((QuestionsListFragment) getCurrentlyActiveFragment()).reload();
                }
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);

        // toolbar
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // sections
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.sections_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount() - 1);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sections_pager_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        mSlidingTabLayout.setBackgroundColor(getResources().getColor(R.color.primary));
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(android.R.color.white);
            }
        });

        // setup plus button
        createPostButton = (FloatingActionButton) findViewById(R.id.create_post_button);
        createPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddQuestionActivity.class);
                startActivityForResult(intent, ADD_QUESTION);
            }
        });

        serviceHelper = new ServiceHelper(this);
        final CallbacksKeeper callbacksKeeper = CallbacksKeeper.getInstance();
        callbacksKeeper.addCallback(TAG, OperationType.USER_GET, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                serviceHelper.getCategories(TAG, getResources().getConfiguration().locale.toString(), callbacksKeeper.getCallback(TAG, OperationType.CATEGORIES_GET));
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


        callbacksKeeper.addCallback(TAG, OperationType.CATEGORIES_GET, new ServiceCallback() {
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
                String msg = "Categories error. " + message;
                Toaster.toast(getApplicationContext(), msg);
            }
        });

        if (savedInstanceState != null) {
            serviceHelper.restoreOperationsState(savedInstanceState, BundleKeys.PENDING_OPERATIONS, callbacksKeeper, TAG);
        }

        categoriesListAdapter = new CategoriesListAdapter(null, this, this);
        createDrawer(toolbar, categoriesListAdapter);

        new RetrieveUserTask().execute();
        getSupportLoaderManager().restartLoader(LoaderIds.CATEGORIES_LOADER, null, categoriesLoaderCallbacks);

        eventBus.register(this);
    }

    private void getUserInfo() {
        String currentUserId = ApiUI.getCurrentUserId(MainActivity.this);
        if (currentUserId != null) {
            serviceHelper.getUser(TAG, currentUserId, CallbacksKeeper.getInstance().getCallback(TAG, OperationType.USER_GET));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_QUESTION && resultCode == Activity.RESULT_OK) {
            int qid = data.getIntExtra(AddQuestionActivity.QUESTION_ID, -1);
            Intent intent = new Intent(this, QuestionDetailsActivity.class);
            intent.putExtra(QuestionDetailsActivity.IntentExtras.Q_ID, qid);
            intent.putExtra(QuestionDetailsActivity.IntentExtras.FORCE_REFRESH, true);
            intent.putExtra(QuestionDetailsActivity.IntentExtras.AFTER_CREATE, true);
            startActivityForResult(intent, QUESTION_DETAILS);
        } else if (requestCode == QUESTION_DETAILS && resultCode == Activity.RESULT_OK && data.getBooleanExtra(QuestionDetailsActivity.IntentExtras.AFTER_CREATE, false)) {
            refreshPages();
        } else {
            for (WeakReference<Fragment> weak : mSectionsPagerAdapter.getFragments().values()) {
                if (weak != null) {
                    Fragment f = weak.get();
                    if (f != null) {
                        f.onActivityResult(requestCode, resultCode, data);
                    }
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        serviceHelper.saveOperationsState(outState, BundleKeys.PENDING_OPERATIONS);
    }

    @Override
    public void onResume() {
        super.onResume();
        serviceHelper.init();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(gcmRegistrationBroadcastReceiver, new IntentFilter(GcmPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    public void onPause() {
        super.onPause();
        serviceHelper.release();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(gcmRegistrationBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventBus.unregister(this);
    }

    public void onEvent(NetworkStateReceiver.NetworkIsUpEvent event) {
        Log.d(TAG, "Got NetworkIsUp event. Updating information...");
        getUserInfo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createDrawer(Toolbar toolbar, CategoriesListAdapter categoriesListAdapter) {
        this.categoriesListAdapter = categoriesListAdapter;

        DrawerImageLoader.init(new DrawerImageLoader.IDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                ImageLoader imageLoader = ImageLoaderInitializer.getImageLoader(MainActivity.this);
                imageLoader.displayImage(uri.toString(), imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                ImageLoader imageLoader = ImageLoaderInitializer.getImageLoader(MainActivity.this);
                imageLoader.cancelDisplayTask(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx) {
                return getResources().getDrawable(R.drawable.profile);
            }
        });

        // Create the AccountHeader
        drawerHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(new ProfileDrawerItem())
                .withSelectionListEnabled(false)
                .withSelectionListEnabledForSingleProfile(false)
                .build();

        //Now create your drawer and pass the AccountHeader.Result
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(drawerHeader)
                .addDrawerItems(
                        new CategoriesDrawerItem(categoriesListAdapter),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem()
                                .withName(R.string.drawer_item_logout)
                                .withIdentifier(LOGOUT_ID)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
                        if (iDrawerItem.getIdentifier() == LOGOUT_ID) {
                            logout();
                            return true;
                        } else {
                            return true;
                        }
                    }
                })
                .build();
    }

    private void logout() {
        drawer.closeDrawer();

        CleanDataTask task = new CleanDataTask() {
            @Override
            protected Void doInBackground(Void... params) {
                CacheHelper.deleteCache(MainActivity.this);
                DbHelper.cleanDb();
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                AccountManager am = AccountManager.get(MainActivity.this);
                Account[] accounts = am.getAccountsByType(getApplicationContext().getPackageName());

                final AccountManagerCallback<Bundle> cb = new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
//                        MainFragment f = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
//                        if (f != null) {
//                            f.invalidatePages();
//                        }
//                        getUserInfo();
                        recreate();
                    }
                };

                if (accounts.length != 0) {
                    Account account = accounts[0];
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        am.removeAccountExplicitly(accounts[0]);
                        getAuthTokenAndCheck(cb);
                    } else {
                        am.removeAccount(account, new AccountManagerCallback<Boolean>() {
                            @Override
                            public void run(AccountManagerFuture<Boolean> future) {
                                getAuthTokenAndCheck(cb);
                            }
                        }, null);
                    }
                } else {
//                    recreate();
                    getAuthTokenAndCheck(cb);
                }
            }
        };
        task.execute();
    }

    @Override
    public List<CategoryEntry> getSelectedCategories() {
        if (categoriesListAdapter == null)
            return null;

        return CategoryEntry.getSelected();
    }

    @Override
    public void categorySelected(CategoryEntry category, boolean isChecked) {
        category.setSelectedAsync(isChecked, new OnCategorySelectedListener() {
            @Override
            public void categorySelected(CategoryEntry category, boolean isChecked) {
                QuestionsListFragment f = (QuestionsListFragment) getCurrentlyActiveFragment();
                f.categorySelected(category, isChecked);
            }
        });
    }

    public Fragment getCurrentlyActiveFragment() {
        int currentFragment = mViewPager.getCurrentItem();
        return (Fragment) mSectionsPagerAdapter.instantiateItem(mViewPager, currentFragment);
    }

    public void invalidatePages() {
        for (WeakReference<Fragment> weak : mSectionsPagerAdapter.getFragments().values()) {
            if (weak != null) {
                Fragment f = weak.get();
                if (f != null) {
                    QuestionsListFragment questionsListFragment = (QuestionsListFragment) f;
                    questionsListFragment.invalidate();
                }
            }
        }
    }

    public void refreshPages() {
        for (WeakReference<Fragment> weak : mSectionsPagerAdapter.getFragments().values()) {
            if (weak != null) {
                Fragment f = weak.get();
                if (f != null) {
                    QuestionsListFragment questionsListFragment = (QuestionsListFragment) f;
                    questionsListFragment.refresh();
                }
            }
        }
    }

    public void reloadPages() {
        for (WeakReference<Fragment> weak : mSectionsPagerAdapter.getFragments().values()) {
            if (weak != null) {
                Fragment f = weak.get();
                if (f != null) {
                    QuestionsListFragment questionsListFragment = (QuestionsListFragment) f;
                    questionsListFragment.reload();
                }
            }
        }
    }

    private BroadcastReceiver gcmRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean sentToken = sharedPreferences.getBoolean(GcmPreferences.SENT_TOKEN_TO_SERVER, false);
            if (sentToken) {
                Log.d(TAG, "sent gcm token successfully");
            } else {
                Log.d(TAG, "send gcm token failed");
            }
        }
    };

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter implements ColoredAdapter {
        private HashMap<Integer, WeakReference<Fragment>> fragments;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new HashMap<>(getCount());
        }

        @Override
        public Fragment getItem(int position) {
            QuestionsListFragment f = QuestionsListFragment.create(ContentSection.fromInt(position));
            fragments.put(position, new WeakReference<Fragment>(f));
            return f;
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

        public HashMap<Integer, WeakReference<Fragment>> getFragments() {
            return fragments;
        }
    }

    class RetrieveUserTask extends AsyncTask<Void, Void, UserEntry> {

        @Override
        protected UserEntry doInBackground(Void... params) {
            String userId = ApiUI.getCurrentUserId(MainActivity.this);
            if (userId != null) {
                return UserEntry.byUId(userId);
            }
            return null;
        }

        @Override
        protected void onPostExecute(UserEntry entry) {
            if (entry == null) {
                return;
            }
            getSupportLoaderManager().restartLoader(LoaderIds.CATEGORIES_LOADER, null, categoriesLoaderCallbacks);
            String username = entry.getUsername();
            if (username == null || username.isEmpty())
                username = getString(R.string.no_nick);

            String fullname = "";
            if (entry.getFirstName() != null && entry.getLastName() != null)
                fullname = entry.getFirstName() + " " + entry.getLastName();

            ProfileDrawerItem profile = new ProfileDrawerItem()
                    .withName(username)
                    .withEmail(fullname);

            String avatar = entry.getAvatar();
            if (avatar == null || avatar.isEmpty())
                profile.withIcon(getResources().getDrawable(R.drawable.profile));
            else
                profile.withIcon(ApiUI.resolveUrl(avatar));


            drawerHeader = new AccountHeaderBuilder()
                    .withActivity(MainActivity.this)
                    .withHeaderBackground(R.drawable.header)
                    .addProfiles(profile)
                    .withDrawer(drawer)
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            String uid = ApiUI.getCurrentUserId(MainActivity.this);
                            intent.putExtra(ProfileActivity.USER_ID, uid);
                            startActivity(intent);

                            return true;
                        }
                    })
                    .build();

            drawer.setHeader(drawerHeader.getView());
        }
    }

    abstract class CleanDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        abstract protected Void doInBackground(Void... params);

        @Override
        abstract protected void onPostExecute(Void v);
    }


    private class LoaderCallbacksImpl implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if  (id == LoaderIds.CATEGORIES_LOADER) {
                return new CategoriesLoader(MainActivity.this);
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
