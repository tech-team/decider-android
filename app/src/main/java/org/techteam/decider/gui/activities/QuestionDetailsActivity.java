package org.techteam.decider.gui.activities;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import junit.framework.Assert;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.content.question.CommentData;
import org.techteam.decider.gui.activities.lib.IAuthTokenGetter;
import org.techteam.decider.gui.adapters.CommentsListAdapter;
import org.techteam.decider.gui.fragments.OnListScrolledDownCallback;
import org.techteam.decider.gui.loaders.CommentsLoader;
import org.techteam.decider.gui.loaders.LoadIntention;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.gui.views.QuestionView;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;

public class QuestionDetailsActivity extends AppCompatActivity
            implements OnListScrolledDownCallback, IAuthTokenGetter {
    private RetrieveEntryTask retrieveEntryTask;

    // children
    private QuestionView questionView;
    private RecyclerView commentsView;
    private EditText commentEdit;
    private ImageButton sendCommentButton;

    private CommentsListAdapter adapter;

    private static final int COMMENTS_LIMIT = 30;
    private int commentsOffset = 0;

    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();
    private ServiceHelper serviceHelper;

    private LoaderManager.LoaderCallbacks<Cursor> commentsLoaderCallbacks = new CommentsLoaderCallbacksImpl();

    public static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
        public static final String Q_ID = "qid";
    }

    @Override
    public AccountManagerFuture<Bundle> getAuthToken(AccountManagerCallback<Bundle> cb) {
        return AuthTokenGetter.getAuthTokenByFeatures(this, cb);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.fragment_question_details);
        
        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.post_details_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // find children
        questionView = (QuestionView) findViewById(R.id.post_view);
        commentsView = (RecyclerView) findViewById(R.id.comments_recycler);

        commentEdit = (EditText) findViewById(R.id.comment_edit);
        sendCommentButton = (ImageButton) findViewById(R.id.comment_send);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commentsView.setLayoutManager(layoutManager);


        adapter = new CommentsListAdapter(null, this, null, QuestionDetailsActivity.this, null);
        commentsView.setAdapter(adapter);

        sendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

        serviceHelper = new ServiceHelper(this);
        callbacksKeeper.addCallback(OperationType.GET_COMMENTS, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toast(QuestionDetailsActivity.this, "GetComments: ok");

                boolean isFeedFinished = data.getBoolean(GetCommentsExtras.FEED_FINISHED, false);
                int insertedCount = data.getInt(GetCommentsExtras.COUNT, -1);
                int loadIntention = data.getInt(GetCommentsExtras.LOAD_INTENTION, LoadIntention.REFRESH);

                commentsOffset += insertedCount;

                String msg;
                if (isFeedFinished) {
                    msg = "No more posts";
                } else {
                    msg = "Successfully fetched posts";
                    Bundle args = new Bundle();
                    args.putInt(CommentsLoader.BundleKeys.INSERTED_COUNT, insertedCount);
                    args.putInt(CommentsLoader.BundleKeys.LOAD_INTENTION, loadIntention);
                    getLoaderManager().restartLoader(LoaderIds.COMMENTS_LOADER, args, commentsLoaderCallbacks);
                }
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.ERROR_CODE);
                if (code == ErrorsExtras.Codes.INVALID_TOKEN) {
                    getAuthToken(null);
                    return;
                }
                Toaster.toast(QuestionDetailsActivity.this, "GetComments: failed. " + message);
            }
        });

        callbacksKeeper.addCallback(OperationType.CREATE_COMMENT, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toast(QuestionDetailsActivity.this, "CreateComment: ok");
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.ERROR_CODE);
                if (code == ErrorsExtras.Codes.INVALID_TOKEN) {

                    return;
                }
                Toaster.toast(QuestionDetailsActivity.this, "CreateComment: failed. " + message);
            }
        });

        if (savedInstanceState != null) {
            serviceHelper.restoreOperationsState(savedInstanceState,
                    BundleKeys.PENDING_OPERATIONS,
                    callbacksKeeper);
        }

        // set data
        retrieveEntryTask = new RetrieveEntryTask();
        retrieveEntryTask.execute();
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
        String text = commentEdit.getText().toString();
        commentEdit.setText("");

        serviceHelper.createComment(new CommentData(text, getIntent().getIntExtra(BundleKeys.Q_ID, -1), false),
                callbacksKeeper.getCallback(OperationType.CREATE_COMMENT));
    }

    @Override
    public void onScrolledDown() {
        int intention;
        if (adapter.getCursor().getCount() == 0) {
            intention = LoadIntention.REFRESH;
        } else {
            intention = LoadIntention.APPEND;
        }

        // TODO
    }


    class RetrieveEntryTask extends AsyncTask<Void, Void, QuestionEntry> {

        @Override
        protected QuestionEntry doInBackground(Void... params) {
            int qid = getIntent().getIntExtra(BundleKeys.Q_ID, -1);
            Assert.assertNotSame("Q_ID is null", qid, -1);

            QuestionEntry entry = QuestionEntry.byQId(qid);
            Assert.assertNotSame("entry is null", entry, null);

            return entry;
        }

        @Override
        protected void onPostExecute(QuestionEntry entry) {
            // QuestionView will be migrated into list element
            //questionView.reuse(entry, null);
            serviceHelper.getComments(entry.getQId(),
                    COMMENTS_LIMIT,
                    commentsOffset,
                    LoadIntention.REFRESH,
                    callbacksKeeper.getCallback(OperationType.GET_COMMENTS));
        }
    }


    private class CommentsLoaderCallbacksImpl implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if  (id == LoaderIds.COMMENTS_LOADER) {
                Integer entryPos = null;
                Integer insertedCount = null;
                int loadIntention = LoadIntention.REFRESH;
                if (args != null) {
                    entryPos = args.getInt(CommentsLoader.BundleKeys.ENTRY_POSITION, -1);
                    entryPos = entryPos == -1 ? null : entryPos;

                    insertedCount = args.getInt(CommentsLoader.BundleKeys.INSERTED_COUNT, -1);
                    insertedCount = insertedCount == -1 ? null : insertedCount;

                    loadIntention = args.getInt(CommentsLoader.BundleKeys.LOAD_INTENTION, LoadIntention.REFRESH);
                }

                return new CommentsLoader(QuestionDetailsActivity.this, entryPos, insertedCount, loadIntention);
            }
            throw new IllegalArgumentException("Loader with given id is not found");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
            CommentsLoader questionsLoader = (CommentsLoader) loader;
            Integer entryPos = questionsLoader.getEntryPosition();
            Integer count = questionsLoader.getInsertedCount();
            int loadIntention = questionsLoader.getLoadIntention();

            if (loadIntention == LoadIntention.REFRESH) {
                adapter.swapCursor(newCursor);
            } else {
                if (entryPos != null) {
                    adapter.swapCursor(newCursor, entryPos);
                } else if (count != null) {
                    adapter.swapCursor(newCursor, newCursor.getCount() - count, count);
                } else {
                    adapter.swapCursor(newCursor);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.swapCursor(null);
        }
    }
}