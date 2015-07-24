package org.techteam.decider.gui.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Build;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

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
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.entities.DbHelper;
import org.techteam.decider.content.entities.UserEntry;
import org.techteam.decider.gcm.GcmPreferences;
import org.techteam.decider.gui.CategoriesGetter;
import org.techteam.decider.gui.activities.lib.AuthTokenGetter;
import org.techteam.decider.gui.adapters.CategoriesListAdapter;
import org.techteam.decider.gui.fragments.MainFragment;
import org.techteam.decider.gui.fragments.OnCategorySelectedListener;
import org.techteam.decider.gui.fragments.QuestionsListFragment;
import org.techteam.decider.gui.loaders.CategoriesLoader;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.CacheHelper;
import org.techteam.decider.util.ImageLoaderInitializer;
import org.techteam.decider.util.Toaster;

import java.util.List;


public class MainActivity extends ToolbarActivity implements
        AuthTokenGetter,
        OnCategorySelectedListener,
        CategoriesGetter {
    private static final String TAG = MainActivity.class.getName();

    public static final int AUTH_REQUEST_CODE = 101;
    private static final int LOGOUT_ID = 1;
    public static String PACKAGE_NAME;

    private Toolbar toolbar;
    // drawer related stuff
    private AccountHeader drawerHeader;
    private Drawer drawer;
    private RetrieveUserTask retrieveUserTask;

    private CategoriesListAdapter categoriesListAdapter;

    private ServiceHelper serviceHelper;
    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();
    private LoaderManager.LoaderCallbacks<Cursor> categoriesLoaderCallbacks = new LoaderCallbacksImpl();

    private BroadcastReceiver gcmRegistrationBroadcastReceiver;

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
        PACKAGE_NAME = getApplicationContext().getPackageName();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);

        getAuthToken(new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                if (future.isCancelled()) {
                    finish();
                } else {
                    finishAuthorization();
                }
            }
        });

        serviceHelper = new ServiceHelper(this);
        callbacksKeeper.addCallback(OperationType.USER_GET, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                serviceHelper.getCategories(getResources().getConfiguration().locale.toString(), callbacksKeeper.getCallback(OperationType.CATEGORIES_GET));
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        getAuthTokenOrExit(null);
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

        callbacksKeeper.addCallback(OperationType.CATEGORIES_GET, new ServiceCallback() {
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
                        getAuthTokenOrExit(null);
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

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        categoriesListAdapter = new CategoriesListAdapter(null, this, this);
        createDrawer(toolbar, categoriesListAdapter);
    }

    private void getUserInfo() {
        String currentUserId = ApiUI.getCurrentUserId(MainActivity.this);
        if (currentUserId != null) {
            serviceHelper.getUser(currentUserId, callbacksKeeper.getCallback(OperationType.USER_GET));
        }
    }

    private void finishAuthorization() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content_frame, new MainFragment(), MainFragment.TAG).commit();
        getUserInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        serviceHelper.init();
        gcmRegistrationBroadcastReceiver = new BroadcastReceiver() {
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
        LocalBroadcastManager.getInstance(this).registerReceiver(gcmRegistrationBroadcastReceiver,
                new IntentFilter(GcmPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    public void onPause() {
        super.onPause();
        serviceHelper.release();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gcmRegistrationBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                        am.removeAccount(account, null, new AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> future) {
                                // restart activity, it will request authorization and receive new user's data
//                            recreate();
                                getAuthTokenOrExit(cb);
                            }
                        }, null);
                    } else {
                        am.removeAccount(account, new AccountManagerCallback<Boolean>() {
                            @Override
                            public void run(AccountManagerFuture<Boolean> future) {
                                // restart activity, it will request authorization and receive new user's data
//                            recreate();
                                getAuthTokenOrExit(cb);
                            }
                        }, null);
                    }

                } else {
//                    recreate();
                    getAuthTokenOrExit(cb);
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
                MainFragment mainFragment = getMainFragment();
                QuestionsListFragment f = (QuestionsListFragment) mainFragment.getCurrentlyActiveFragment();
                f.categorySelected(category, isChecked);
            }
        });
    }

    public MainFragment getMainFragment() {
        return (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
    }

    class RetrieveUserTask extends AsyncTask<Void, Void, UserEntry> {

        @Override
        protected UserEntry doInBackground(Void... params) {
            return UserEntry.byUId(ApiUI.getCurrentUserId(MainActivity.this));
        }

        @Override
        protected void onPostExecute(UserEntry entry) {
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
