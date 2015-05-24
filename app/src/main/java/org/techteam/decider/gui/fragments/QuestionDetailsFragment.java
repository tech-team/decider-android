package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.loaders.LoadIntention;
import org.techteam.decider.gui.views.QuestionView;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;

public class QuestionDetailsFragment extends Fragment {
    private MainActivity activity;
    private QuestionEntry entry;
    private int qid;

    private RetrieveEntryTask retrieveEntryTask;

    private View rootView;
    // children
    private QuestionView questionView;
    private RecyclerView commentsView;
    private EditText commentEdit;
    private ImageButton sendCommentButton;

    private static final int COMMENTS_LIMIT = 30;
    private int commentsOffset = 0;

    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();
    private ServiceHelper serviceHelper;

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
    }

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

        serviceHelper = new ServiceHelper(activity);
        callbacksKeeper.addCallback(OperationType.GET_COMMENTS, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toast(QuestionDetailsFragment.this.activity.getBaseContext(), "GetComments: ok");
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                Toaster.toast(QuestionDetailsFragment.this.activity.getBaseContext(), "GetComments: failed. " + message);
            }
        });
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
        commentsView = (RecyclerView) rootView.findViewById(R.id.comments_recycler);

        commentEdit = (EditText) rootView.findViewById(R.id.comment_edit);
        sendCommentButton = (ImageButton) rootView.findViewById(R.id.comment_send);

        // set data
        retrieveEntryTask = new RetrieveEntryTask();
        retrieveEntryTask.execute();

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commentsView.setLayoutManager(layoutManager);

        sendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

        if (savedInstanceState != null) {
            serviceHelper.restoreOperationsState(savedInstanceState,
                    BundleKeys.PENDING_OPERATIONS,
                    callbacksKeeper);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        serviceHelper.saveOperationsState(outState, BundleKeys.PENDING_OPERATIONS);
    }

    @Override
    public void onResume() {
        super.onResume();
        serviceHelper.init();
    }

    @Override
    public void onPause() {
        super.onPause();
        serviceHelper.release();
    }

    private void sendComment() {
        //TODO: sendComment

        commentEdit.setText("");
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
            serviceHelper.getComments(entry.getQId(),
                    COMMENTS_LIMIT,
                    commentsOffset,
                    LoadIntention.REFRESH,
                    callbacksKeeper.getCallback(OperationType.GET_COMMENTS));
        }
    }
}
