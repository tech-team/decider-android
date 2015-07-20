package org.techteam.decider.gui.activities;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.vk.sdk.VKUIHelper;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.entities.UserEntry;
import org.techteam.decider.gui.activities.lib.IAuthTokenGetter;
import org.techteam.decider.gui.adapters.CategoriesListAdapter;
import org.techteam.decider.gui.fragments.MainFragment;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;

import java.util.List;


public class MainActivity extends AppCompatActivity implements IAuthTokenGetter {
    public static final int AUTH_REQUEST_CODE = 101;
    public static String PACKAGE_NAME;


    // drawer related stuff
    private AccountHeader drawerHeader;
    private Drawer drawer;
    private RetrieveUserTask retrieveUserTask;

    private CategoriesListAdapter categoriesListAdapter;
    private ApiUI apiUI;

    private ServiceHelper serviceHelper;
    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();

    @Override
    public AccountManagerFuture<Bundle> getAuthToken(AccountManagerCallback<Bundle> cb) {
        return AuthTokenGetter.getAuthTokenByFeatures(this, cb);
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
                    finishAuthorization();
                }
            });
        }

        apiUI = new ApiUI(this);
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
                int code = data.getInt(ErrorsExtras.ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.Codes.INVALID_TOKEN:
                        getAuthToken(null);
                        return;
                    case ErrorsExtras.Codes.SERVER_ERROR:
                        Toaster.toastLong(getApplicationContext(), R.string.server_problem);
                        return;
                }
                Toaster.toastLong(getApplicationContext(), "GetUser: failed. " + message);
            }
        });

        serviceHelper.getUser(apiUI.getCurrentUserId(), callbacksKeeper.getCallback(OperationType.USER_GET));
    }

    private void finishAuthorization() {
        getFragmentManager().beginTransaction()
                .add(R.id.content_frame, new MainFragment()).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
            return;
        }

        getFragmentManager().popBackStack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createDrawer(Toolbar toolbar, CategoriesListAdapter categoriesListAdapter) {
        this.categoriesListAdapter = categoriesListAdapter;

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
                        new SecondaryDrawerItem().withName(R.string.drawer_item_logout)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
                        return false;
                    }
                })
                .build();
    }

    public List<CategoryEntry> getSelectedCategories() {
        if (categoriesListAdapter == null)
            return null;

        return CategoryEntry.getSelected();
    }

    class RetrieveUserTask extends AsyncTask<Void, Void, UserEntry> {

        @Override
        protected UserEntry doInBackground(Void... params) {
            return UserEntry.byUId(apiUI.getCurrentUserId());
        }

        @Override
        protected void onPostExecute(UserEntry entry) {
            String username = entry.getUsername();
            if (username == null || username.isEmpty())
                username = getString(R.string.no_nick);

            String fullname = "";
            if (entry.getFirstName() != null && entry.getLastName() != null)
                fullname = entry.getUsername() + " " + entry.getLastName();

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
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            String uid = apiUI.getCurrentUserId();
                            intent.putExtra(ProfileActivity.USER_ID, uid);
                            startActivity(intent);

                            return false;
                        }
                    })
                    .build();

            drawer.setHeader(drawerHeader.getView());
        }
    }
}
