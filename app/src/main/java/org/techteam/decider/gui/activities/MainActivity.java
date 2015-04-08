package org.techteam.decider.gui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;

import org.techteam.decider.R;
import org.techteam.decider.gui.fragments.AuthFragment;
import org.techteam.decider.gui.fragments.MainFragment;
import org.techteam.decider.gui.fragments.PostsListFragment;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;

import java.util.List;


public class MainActivity extends ActionBarActivity {
    public static final String TOKEN_PREF_KEY = "token";

    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();
    private ServiceHelper serviceHelper;
    private boolean refreshing = false;

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceHelper = new ServiceHelper(this);

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
            refreshing = serviceHelper.restoreOperationsState(savedInstanceState,
                    BundleKeys.PENDING_OPERATIONS,
                    callbacksKeeper);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        serviceHelper.saveOperationsState(outState, BundleKeys.PENDING_OPERATIONS);
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


    public CallbacksKeeper getCallbacksKeeper() {
        return callbacksKeeper;
    }

    public ServiceHelper getServiceHelper() {
        return serviceHelper;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
        serviceHelper.init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
        serviceHelper.release();
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
}
