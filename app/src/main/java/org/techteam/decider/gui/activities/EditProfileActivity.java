package org.techteam.decider.gui.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import junit.framework.Assert;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.UserEntry;
import org.techteam.decider.util.image_selector.ActivityStarter;
import org.techteam.decider.util.image_selector.ImageSelector;

public class EditProfileActivity extends AppCompatActivity implements ActivityStarter {
    public final static String USER_ID = "USER_ID";

    private UserEntry entry;
    private String uid;

    private RetrieveEntryTask retrieveEntryTask;

    // children
    private ImageView profileImage;
    private ImageSelector imageSelector;

    private EditText nameText;
    private EditText surnameText;
    private EditText countryText;
    private EditText cityText;
    private EditText birthdayText;

    private Button saveButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.fragment_profile_edit);

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

        final int aspectWidth = 1;
        final int aspectHeight = 1;
        final int previewWidth = 1280;
        final int previewHeight = 1280;
        imageSelector = new ImageSelector(this, this, profileImage);
        imageSelector.setParams(aspectWidth, aspectHeight, previewWidth, previewHeight);

        nameText = (EditText) findViewById(R.id.name);
        surnameText = (EditText) findViewById(R.id.surname);
        countryText = (EditText) findViewById(R.id.country);
        cityText = (EditText) findViewById(R.id.city);
        birthdayText = (EditText) findViewById(R.id.birthday);

        saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: save profile
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageSelector.onActivityResult(requestCode, resultCode, data);
    }

    class RetrieveEntryTask extends AsyncTask<Void, Void, UserEntry> {

        @Override
        protected UserEntry doInBackground(Void... params) {
            entry = UserEntry.byUId(uid);

            return entry;
        }

        @Override
        protected void onPostExecute(UserEntry entry) {
            ImageLoader.getInstance().displayImage(entry.getAvatar(), profileImage);

            nameText.setText(entry.getFirstName());
            surnameText.setText(entry.getLastName());
            countryText.setText(entry.getCountry());
            cityText.setText(entry.getCity());
            birthdayText.setText(entry.getBirthday());
        }
    }
}
