package org.techteam.decider.gui.activities;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.vk.sdk.VKUIHelper;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.gui.adapters.CategoriesListAdapter;
import org.techteam.decider.gui.adapters.Category;
import org.techteam.decider.gui.fragments.AuthFragment;
import org.techteam.decider.gui.fragments.MainFragment;
import org.techteam.decider.gui.fragments.ProfileFragment;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    public static final String TOKEN_PREF_KEY = "token";

    // drawer related stuff
    private AccountHeader.Result headerResult;
    private Drawer.Result drawerResult;

    private CategoriesListAdapter categoriesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // show main fragment if user has token
            // or auth fragment otherwise
            String token = sharedPrefs.getString(TOKEN_PREF_KEY, null);
            if (token != null) {
                getFragmentManager().beginTransaction()
                        .add(R.id.content_frame, new MainFragment()).commit();
            } else {
                getFragmentManager().beginTransaction()
                        .add(R.id.content_frame, new AuthFragment()).commit();
            }
        } else {

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    //
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(this, requestCode, resultCode, data);
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
                        //TODO: pass uid
                        ProfileFragment.create(MainActivity.this, null);

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

    public void lockDrawer() {
        if (drawerResult == null)
            return;

        drawerResult
                .getDrawerLayout()
                .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void unlockDrawer() {
        if (drawerResult == null)
            return;

        drawerResult
                .getDrawerLayout()
                .setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public List<CategoryEntry> getSelectedCategories() {
        if (categoriesListAdapter == null)
            return null;

        return CategoryEntry.getSelected();

//        List<CategoryEntry> selectedCategories = new ArrayList<>();
//
//        Cursor cursor = categoriesListAdapter.getCursor();
//        if (cursor.moveToFirst()){
//            while(!cursor.isAfterLast()){
//                CategoryEntry categoryEntry = CategoryEntry.fromCursor(cursor);
//                if (categoryEntry.isSelected())
//                    selectedCategories.add(categoryEntry);
//
//                cursor.moveToNext();
//            }
//        }
//        // it was opened, so why should i close it?
//        //cursor.close();
//
//        return selectedCategories;
    }
}
