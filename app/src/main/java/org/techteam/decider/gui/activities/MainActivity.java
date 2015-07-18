package org.techteam.decider.gui.activities;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.vk.sdk.VKUIHelper;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.gui.activities.lib.IAuthTokenGetter;
import org.techteam.decider.gui.adapters.CategoriesListAdapter;
import org.techteam.decider.gui.fragments.MainFragment;
import org.techteam.decider.rest.api.ApiUI;

import java.util.List;


public class MainActivity extends AppCompatActivity implements IAuthTokenGetter {
    public static final int AUTH_REQUEST_CODE = 101;
    public static String PACKAGE_NAME;


    // drawer related stuff
    private AccountHeader.Result headerResult;
    private Drawer.Result drawerResult;

    private CategoriesListAdapter categoriesListAdapter;
    private ApiUI apiUI;

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
    }

    private void finishAuthorization() {
        getFragmentManager().beginTransaction()
                .add(R.id.content_frame, new MainFragment()).commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
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
        headerResult = new AccountHeader()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName("Alekseyl")
                                .withEmail("alekseyl@list.ru")
                                .withIcon(this
                                        .getResources()
                                        .getDrawable(R.drawable.profile))
                )
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

        //Now create your drawer and pass the AccountHeader.Result
        drawerResult = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new CategoriesDrawerItem(categoriesListAdapter),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_logout)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                    }
                })
                .build();
    }

    public AccountHeader.Result getHeaderResult() {
        return headerResult;
    }

    public Drawer.Result getDrawerResult() {
        return drawerResult;
    }

    public List<CategoryEntry> getSelectedCategories() {
        if (categoriesListAdapter == null)
            return null;

        return CategoryEntry.getSelected();
    }
}
