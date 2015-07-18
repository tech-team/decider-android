package org.techteam.decider.gui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import junit.framework.Assert;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.UserEntry;

public class ProfileActivity extends AppCompatActivity {
    public final static String USER_ID = "USER_ID";

    public final static int EDIT_PROFILE = 0;

    private UserEntry entry;
    private String uid;

    private RetrieveEntryTask retrieveEntryTask;

    private View rootView;
    // children
    private ImageView profileImage;

    private TextView fullNameText;
    private TextView countryText;
    private TextView cityText;
    private TextView birthdayText;

    private Button editButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.fragment_profile);

        uid = getIntent().getStringExtra(USER_ID);
        Assert.assertNotSame("UID is null", uid, null);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // find children
        profileImage = (ImageView) findViewById(R.id.profile_image);

        fullNameText = (TextView) findViewById(R.id.full_name_text);
        countryText = (TextView) findViewById(R.id.country_text);
        cityText = (TextView) findViewById(R.id.city_text);
        birthdayText = (TextView) findViewById(R.id.birthday_text);

        editButton = (Button) findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra(EditProfileActivity.USER_ID, getIntent().getStringExtra(USER_ID));
                startActivityForResult(intent, EDIT_PROFILE);
            }
        });

        retrieveEntryTask = new RetrieveEntryTask();
        retrieveEntryTask.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_PROFILE && resultCode == Activity.RESULT_OK) {
            // update data
            retrieveEntryTask = new RetrieveEntryTask();
            retrieveEntryTask.execute();
        }
    }

    class RetrieveEntryTask extends AsyncTask<Void, Void, UserEntry> {

        @Override
        protected UserEntry doInBackground(Void... params) {
            entry = UserEntry.byUId(uid);

            return entry;
        }

        @Override
        protected void onPostExecute(UserEntry entry) {
            // populate gui with data
            ImageLoader.getInstance().displayImage(entry.getAvatar(), profileImage);

            fullNameText.setText(
                    entry.getFirstName() + " "
                            + entry.getMiddleName() + " "
                            + entry.getLastName());

            countryText.setText(entry.getCountry());
            cityText.setText(entry.getCity());
            birthdayText.setText(entry.getBirthday());
        }
    }
}
