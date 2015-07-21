package org.techteam.decider.gui.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
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
import org.techteam.decider.gui.activities.lib.IAuthTokenGetter;
import org.techteam.decider.gui.adapters.CategoriesListAdapter;
import org.techteam.decider.gui.fragments.MainFragment;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.CacheHelper;
import org.techteam.decider.util.ImageLoaderInitializer;
import org.techteam.decider.util.Toaster;

import java.util.List;


public class MainActivity extends ToolbarActivity implements IAuthTokenGetter {
    public static final int AUTH_REQUEST_CODE = 101;
    private static final int LOGOUT_ID = 1;
    public static String PACKAGE_NAME;


    // drawer related stuff
    private AccountHeader drawerHeader;
    private Drawer drawer;
    private RetrieveUserTask retrieveUserTask;

    private CategoriesListAdapter categoriesListAdapter;

    private ServiceHelper serviceHelper;
    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();

    @Override
    public AccountManagerFuture<Bundle> getAuthToken(AccountManagerCallback<Bundle> cb) {
        return AuthTokenGetter.getAuthTokenByFeatures(this, cb);
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
        return AuthTokenGetter.getAuthTokenByFeatures(this, actualCb);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = getApplicationContext().getPackageName();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
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
        }

        serviceHelper = new ServiceHelper(this);
        callbacksKeeper.addCallback(OperationType.USER_GET, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toast(getApplicationContext(), "GetUser: ok");
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
                }
                Toaster.toastLong(getApplicationContext(), "GetUser: failed. " + message);
            }
        });

        getUserInfo();
    }

    private void getUserInfo() {
        String currentUserId = ApiUI.getCurrentUserId(MainActivity.this);
        if (currentUserId != null) {
            serviceHelper.getUser(currentUserId, callbacksKeeper.getCallback(OperationType.USER_GET));
        }
    }

    private void finishAuthorization() {
        getFragmentManager().beginTransaction()
                .add(R.id.content_frame, new MainFragment(), MainFragment.TAG).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                        MainFragment f = (MainFragment) getFragmentManager().findFragmentByTag(MainFragment.TAG);
                        if (f != null) {
                            f.invalidatePages();
                        }
                        getUserInfo();
                    }
                };

                if (accounts.length != 0) {
                    Account account = accounts[0];
                    am.removeAccount(account, new AccountManagerCallback<Boolean>() {
                        @Override
                        public void run(AccountManagerFuture<Boolean> future) {
                            // restart activity, it will request authorization and receive new user's data
//                            recreate();
                            getAuthTokenOrExit(cb);
                        }
                    }, null);
                } else {
//                    recreate();
                    getAuthTokenOrExit(cb);
                }
            }
        };
        task.execute();
    }

    public List<CategoryEntry> getSelectedCategories() {
        if (categoriesListAdapter == null)
            return null;

        return CategoryEntry.getSelected();
    }

    class RetrieveUserTask extends AsyncTask<Void, Void, UserEntry> {

        @Override
        protected UserEntry doInBackground(Void... params) {
            return UserEntry.byUId(ApiUI.getCurrentUserId(MainActivity.this));
        }

        @Override
        protected void onPostExecute(UserEntry entry) {
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
}
