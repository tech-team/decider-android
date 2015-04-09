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
import org.techteam.decider.gui.views.QuestionView;

public class QuestionDetailsFragment extends Fragment {
    private MainActivity activity;
    private QuestionEntry entry;
    private int qid;

    private RetrieveEntryTask retrieveEntryTask;

    private View rootView;
    // children
    private QuestionView questionView;

    @Override
    public void setArguments(Bundle args) {
        //TODO: hardcoded key, bla-bla-bla...
        if (args != null) {
            qid = args.getInt("qid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_question_details, container, false);

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
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.post_details_toolbar);
        this.activity.setSupportActionBar(toolbar);

        ActionBar actionBar = this.activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // find children
        questionView = (QuestionView) rootView.findViewById(R.id.post_view);

        // set data
        retrieveEntryTask = new RetrieveEntryTask();
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
            questionView.reuse(entry, null);
        }
    }
}
