package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.content.entities.UserEntry;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.views.QuestionView;
import org.techteam.decider.util.Toaster;

public class ProfileFragment extends Fragment {
    private MainActivity activity;
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

    public static void create(MainActivity activity, String uid) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        profileFragment.setArguments(bundle);

        activity.getFragmentManager().beginTransaction()
                .add(R.id.content_frame, profileFragment)
                .addToBackStack(ProfileFragment.class.getName())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void setArguments(Bundle args) {
        //TODO: hardcoded key, bla-bla-bla...
        if (args != null) {
            uid = args.getString("uid");
            if (uid == null) {
                System.err.println("NULL UID PASSED to ProfileFragment, please fix ProfileFragment::create() calls");
                return;
            }

            // set data
            retrieveEntryTask = new RetrieveEntryTask();
            retrieveEntryTask.execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

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

        // setup toolbar
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        this.activity.setSupportActionBar(toolbar);

        ActionBar actionBar = this.activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // find children
        profileImage = (ImageView) rootView.findViewById(R.id.profile_image);

        fullNameText = (TextView) rootView.findViewById(R.id.full_name_text);
        countryText = (TextView) rootView.findViewById(R.id.country_text);
        cityText = (TextView) rootView.findViewById(R.id.city_text);
        birthdayText = (TextView) rootView.findViewById(R.id.birthday_text);
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
