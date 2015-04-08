package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.views.PostView;

public class PostDetailsFragment extends Fragment {
    private MainActivity activity;
    private QuestionEntry entry;
    private int qid;

    private RetrieveEntryTask retrieveEntryTask;

    // children
    private PostView postView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_details, container, false);

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

        //TODO: hardcoded key, bla-bla-bla...
        qid = savedInstanceState.getInt("qid");

        // setup toolbar
        Toolbar toolbar = (Toolbar) this.activity.findViewById(R.id.post_details_toolbar);
        this.activity.setSupportActionBar(toolbar);

        ActionBar actionBar = this.activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // find children
        postView = (PostView) postView.findViewById(R.id.post_view);

        // set data
        retrieveEntryTask.execute();
    }

    class RetrieveEntryTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            entry = QuestionEntry.byQId(qid);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            postView.reuse(entry, null);
        }
    }
}
