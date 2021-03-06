package org.techteam.decider.gui.activities;

import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CommentEntry;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.content.question.CommentData;
import org.techteam.decider.gui.adapters.CommentsListAdapter;
import org.techteam.decider.gui.fragments.OnCommentEventCallback;
import org.techteam.decider.gui.fragments.OnMoreCommentsRequestedCallback;
import org.techteam.decider.gui.fragments.OnQuestionEventCallback;
import org.techteam.decider.gui.loaders.CommentsLoader;
import org.techteam.decider.gui.loaders.LoadIntention;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.gui.views.QuestionView;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;

public class QuestionDetailsActivity extends ToolbarActivity
            implements OnMoreCommentsRequestedCallback, OnQuestionEventCallback, OnCommentEventCallback {
    private static final String TAG = QuestionDetailsActivity.class.getName();
    private RetrieveEntryTask retrieveEntryTask;

    // children
    private QuestionView questionView;
    private RecyclerView commentsView;
    private EditText commentEdit;

    private CheckBox anonymityCheckBox;

    private CommentsListAdapter adapter;

    private static final int COMMENTS_LIMIT = 15;
    private int commentsOffset = 0;
    private boolean forceRefresh = false;
    private int remaining = Integer.MAX_VALUE;

    private ServiceHelper serviceHelper;

    private LoaderManager.LoaderCallbacks<Cursor> commentsLoaderCallbacks = new CommentsLoaderCallbacksImpl();


    public static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
        public static final String COMMENTS_OFFSET = "COMMENTS_OFFSET";
        public static final String REMAINING = "REMAINING";
    }

    public static final class IntentExtras {
        public static final String Q_ID = "qid";
        public static final String COMMENT_ID = "COMMENT_ID";
        public static final String FORCE_REFRESH = "FORCE_REFRESH";
        public static final String AFTER_CREATE = "AFTER_CREATE";
        public static final String ENTRY_POSITION = "ENTRY_POSITION";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        forceRefresh = getIntent().getBooleanExtra(IntentExtras.FORCE_REFRESH, false);
        getIntent().putExtra(IntentExtras.FORCE_REFRESH, false);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_question_details);
        
        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.post_details_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // find children
        questionView = (QuestionView) findViewById(R.id.post_view);
        commentsView = (RecyclerView) findViewById(R.id.comments_recycler);

        commentEdit = (EditText) findViewById(R.id.comment_edit);
        Button sendCommentButton = (Button) findViewById(R.id.comment_send);

        anonymityCheckBox = (CheckBox) findViewById(R.id.anonymity_checkbox);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commentsView.setLayoutManager(layoutManager);

        // comments stuff
        int qid = getIntent().getIntExtra(IntentExtras.Q_ID, -1);
//        Assert.assertNotSame("Q_ID is null", qid, -1);

        QuestionEntry entry = QuestionEntry.byQId(qid);
//        Assert.assertNotSame("entry is null", entry, null);

        adapter = new CommentsListAdapter(null, this, entry, this, this, this);
        commentsView.setAdapter(adapter);

        sendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

        serviceHelper = new ServiceHelper(this);
        CallbacksKeeper callbacksKeeper = CallbacksKeeper.getInstance();
        callbacksKeeper.addCallback(TAG, OperationType.COMMENTS_GET, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                boolean isFeedFinished = data.getBoolean(GetCommentsExtras.FEED_FINISHED, false);
                int questionId = data.getInt(GetCommentsExtras.QUESTION_ID, -1);
                int insertedCount = data.getInt(GetCommentsExtras.COUNT, -1);
                int loadIntention = data.getInt(GetCommentsExtras.LOAD_INTENTION, LoadIntention.REFRESH);
                remaining = data.getInt(GetCommentsExtras.REMAINING, 0);

                adapter.setFeedFinished(remaining == 0);
                if (!isFeedFinished) {
                    Bundle args = new Bundle();
                    args.putInt(CommentsLoader.BundleKeys.QUESTION_ID, questionId);
                    args.putInt(CommentsLoader.BundleKeys.LOAD_INTENTION, loadIntention);
                    args.putInt(CommentsLoader.BundleKeys.INSERTED_COUNT, insertedCount);
                    args.putBoolean(CommentsLoader.BundleKeys.PREPEND, true);
                    getSupportLoaderManager().restartLoader(LoaderIds.COMMENTS_LOADER, args, commentsLoaderCallbacks);
                }
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        getAuthTokenAndCheck(null);
                        return;
                    case ErrorsExtras.GenericErrors.SERVER_ERROR:
                        Toaster.toastLong(getApplicationContext(), R.string.server_problem);
                        return;
                    case ErrorsExtras.GenericErrors.NO_INTERNET:
                        Toaster.toastLong(getApplicationContext(), R.string.no_internet);
                        return;
                    case ErrorsExtras.GenericErrors.INTERNAL_PROBLEMS:
                        Toaster.toastLong(getApplicationContext(), R.string.internal_problems);
                        return;
                }
                Toaster.toast(QuestionDetailsActivity.this, "GetComments: failed. " + message);
            }
        });

        callbacksKeeper.addCallback(TAG, OperationType.COMMENT_CREATE, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                int questionId = data.getInt(CreateQuestionExtras.QID, -1);
                int insertedCount = data.getInt(CreateQuestionExtras.COUNT, -1);

                new QuestionUpdateTask().execute();

                Bundle args = new Bundle();
                args.putInt(CommentsLoader.BundleKeys.QUESTION_ID, questionId);
                args.putInt(CommentsLoader.BundleKeys.INSERTED_COUNT, insertedCount);
                args.putInt(CommentsLoader.BundleKeys.LOAD_INTENTION, LoadIntention.APPEND);
                getSupportLoaderManager().restartLoader(LoaderIds.COMMENTS_LOADER, args, commentsLoaderCallbacks);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        getAuthTokenAndCheck(null);
                        return;
                    case ErrorsExtras.GenericErrors.SERVER_ERROR:
                        Toaster.toastLong(getApplicationContext(), R.string.server_problem);
                        return;
                    case ErrorsExtras.GenericErrors.NO_INTERNET:
                        Toaster.toastLong(getApplicationContext(), R.string.no_internet);
                        return;
                    case ErrorsExtras.GenericErrors.INTERNAL_PROBLEMS:
                        Toaster.toastLong(getApplicationContext(), R.string.internal_problems);
                        return;
                }
                Toaster.toast(QuestionDetailsActivity.this, "CreateComment: failed. " + message);
            }
        });

        callbacksKeeper.addCallback(TAG, OperationType.POLL_VOTE, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                QuestionUpdateTask task = new QuestionUpdateTask();
                task.execute();
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        getAuthTokenAndCheck(null);
                        return;
                    case ErrorsExtras.GenericErrors.SERVER_ERROR:
                        Toaster.toastLong(getApplicationContext(), R.string.server_problem);
                        return;
                    case ErrorsExtras.GenericErrors.NO_INTERNET:
                        Toaster.toastLong(getApplicationContext(), R.string.no_internet);
                        return;
                    case ErrorsExtras.GenericErrors.INTERNAL_PROBLEMS:
                        Toaster.toastLong(getApplicationContext(), R.string.internal_problems);
                        return;
                }

                int serverErrorCode = data.getInt(ErrorsExtras.SERVER_ERROR_CODE, -1);
                switch (serverErrorCode) {
                    case PollVoteExtras.ErrorCodes.ALREADY_VOTED:
                        Toaster.toast(getApplicationContext(), R.string.already_voted);
                        return;
                    default:
                        break;
                }
                Toaster.toastLong(getApplicationContext(), "Error. " + message);
            }
        });

        callbacksKeeper.addCallback(TAG, OperationType.QUESTION_LIKE, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                QuestionUpdateTask task = new QuestionUpdateTask();
                task.execute();
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        getAuthTokenAndCheck(null);
                        return;
                    case ErrorsExtras.GenericErrors.SERVER_ERROR:
                        Toaster.toastLong(getApplicationContext(), R.string.server_problem);
                        return;
                    case ErrorsExtras.GenericErrors.NO_INTERNET:
                        Toaster.toastLong(getApplicationContext(), R.string.no_internet);
                        return;
                    case ErrorsExtras.GenericErrors.INTERNAL_PROBLEMS:
                        Toaster.toastLong(getApplicationContext(), R.string.internal_problems);
                        return;
                }
                Toaster.toastLong(getApplicationContext(), "Error. " + message);
            }
        });

        callbacksKeeper.addCallback(TAG, OperationType.QUESTION_REPORT_SPAM, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toastLong(getApplicationContext(), R.string.question_marked_spam);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        getAuthTokenAndCheck(null);
                        return;
                    case ErrorsExtras.GenericErrors.SERVER_ERROR:
                        Toaster.toastLong(getApplicationContext(), R.string.server_problem);
                        return;
                    case ErrorsExtras.GenericErrors.NO_INTERNET:
                        Toaster.toastLong(getApplicationContext(), R.string.no_internet);
                        return;
                    case ErrorsExtras.GenericErrors.INTERNAL_PROBLEMS:
                        Toaster.toastLong(getApplicationContext(), R.string.internal_problems);
                        return;
                }
                String msg = "Error. " + message;
                Toaster.toastLong(getApplicationContext(), msg);
            }
        });

        callbacksKeeper.addCallback(TAG, OperationType.COMMENT_REPORT_SPAM, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toastLong(getApplicationContext(), R.string.comment_marked_spam);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        getAuthTokenAndCheck(null);
                        return;
                    case ErrorsExtras.GenericErrors.SERVER_ERROR:
                        Toaster.toastLong(getApplicationContext(), R.string.server_problem);
                        return;
                    case ErrorsExtras.GenericErrors.NO_INTERNET:
                        Toaster.toastLong(getApplicationContext(), R.string.no_internet);
                        return;
                    case ErrorsExtras.GenericErrors.INTERNAL_PROBLEMS:
                        Toaster.toastLong(getApplicationContext(), R.string.internal_problems);
                        return;
                }
                Toaster.toastLong(getApplicationContext(), "Error. " + message);
            }
        });

        if (savedInstanceState != null) {
            serviceHelper.restoreOperationsState(savedInstanceState,
                    BundleKeys.PENDING_OPERATIONS,
                    callbacksKeeper, TAG);
            commentsOffset = forceRefresh ? 0 : savedInstanceState.getInt(BundleKeys.COMMENTS_OFFSET, 0);
            remaining = forceRefresh ? Integer.MAX_VALUE : savedInstanceState.getInt(BundleKeys.REMAINING, Integer.MAX_VALUE);
        }

        adapter.setFeedFinished(remaining == 0);
        // set data
        retrieveEntryTask = new RetrieveEntryTask();
        retrieveEntryTask.execute();

        Bundle args = new Bundle();
        args.putInt(CommentsLoader.BundleKeys.QUESTION_ID, getIntent().getIntExtra(IntentExtras.Q_ID, -1));
        args.putInt(CommentsLoader.BundleKeys.INSERTED_COUNT, 0);
        args.putInt(CommentsLoader.BundleKeys.LOAD_INTENTION, LoadIntention.REFRESH);
        getSupportLoaderManager().initLoader(LoaderIds.COMMENTS_LOADER, args, commentsLoaderCallbacks);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        serviceHelper.saveOperationsState(outState, BundleKeys.PENDING_OPERATIONS);
        outState.putInt(BundleKeys.COMMENTS_OFFSET, commentsOffset);
        outState.putInt(BundleKeys.REMAINING, remaining);
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

    @Override
    public void onBackPressed() {
        boolean afterCreate = getIntent().getBooleanExtra(IntentExtras.AFTER_CREATE, false);
        if (afterCreate) {
            Intent data = new Intent();
            data.putExtra(IntentExtras.AFTER_CREATE, afterCreate);
            setResult(Activity.RESULT_OK, data);
            finish();
        } else {
            Intent data = new Intent();
            int entryPosition = getIntent().getIntExtra(IntentExtras.ENTRY_POSITION, -1);
            data.putExtra(IntentExtras.ENTRY_POSITION, entryPosition);
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    private void sendComment() {
        String text = commentEdit.getText().toString();
        if (text.isEmpty()) {
            Toaster.toast(this, R.string.error_empty_comment);
            return;
        }

        commentEdit.setText("");

        int questionId = getIntent().getIntExtra(IntentExtras.Q_ID, -1);
        int lastCommentId = CommentData.NO_LAST_COMMENT_ID;

        Cursor commentsCursor = adapter.getCursor();
        if (commentsCursor != null) {
            int prevPosition = commentsCursor.getPosition();
            if (commentsCursor.moveToLast()) {
                CommentEntry commentEntry = CommentEntry.fromCursor(commentsCursor);
                lastCommentId = commentEntry.getCid();
                commentsCursor.moveToPosition(prevPosition);
            }
        }

        boolean anon = anonymityCheckBox.isChecked();

        serviceHelper.createComment(TAG, new CommentData(text, questionId, lastCommentId, anon),
                CallbacksKeeper.getInstance().getCallback(TAG, OperationType.COMMENT_CREATE));
    }

    @Override
    public void moreCommentsRequested() {
        serviceHelper.getComments(TAG,
                adapter.getQuestionEntry().getQId(),
                COMMENTS_LIMIT,
                commentsOffset,
                LoadIntention.APPEND,
                CallbacksKeeper.getInstance().getCallback(TAG, OperationType.COMMENTS_GET));
    }

    @Override
    public void onLikeClick(int entryPosition, QuestionEntry post) {
        serviceHelper.likeQuestion(TAG, entryPosition, post.getQId(), CallbacksKeeper.getInstance().getCallback(TAG, OperationType.QUESTION_LIKE));
    }

    @Override
    public void onVoteClick(int entryPosition, QuestionEntry post, int voteId) {
        serviceHelper.pollVote(TAG, entryPosition, post.getQId(), voteId, CallbacksKeeper.getInstance().getCallback(TAG, OperationType.POLL_VOTE));
    }

    @Override
    public void onCommentsClick(int entryPosition, QuestionEntry post) {

    }

    @Override
    public void onReportSpam(int entryPosition, QuestionEntry post) {
        serviceHelper.reportSpamQuestion(TAG, entryPosition, post.getQId(), CallbacksKeeper.getInstance().getCallback(TAG, OperationType.QUESTION_REPORT_SPAM));
    }

    @Override
    public void onReportSpam(int entryPosition, CommentEntry entry) {
        serviceHelper.reportSpamComment(TAG, entryPosition, entry.getCid(), CallbacksKeeper.getInstance().getCallback(TAG, OperationType.COMMENT_REPORT_SPAM));
    }


    class RetrieveEntryTask extends AsyncTask<Void, Void, QuestionEntry> {

        @Override
        protected QuestionEntry doInBackground(Void... params) {
            int qid = getIntent().getIntExtra(IntentExtras.Q_ID, -1);
//            Assert.assertNotSame("Q_ID is null", qid, -1);

            QuestionEntry entry = QuestionEntry.byQId(qid);
//            Assert.assertNotSame("entry is null", entry, null);

            return entry;
        }

        @Override
        protected void onPostExecute(QuestionEntry entry) {
            // QuestionView will be migrated into list element
            //questionView.reuse(entry, null);
            if (commentsOffset == 0) {
                serviceHelper.getComments(TAG,
                        entry.getQId(),
                        COMMENTS_LIMIT,
                        commentsOffset,
                        LoadIntention.REFRESH,
                        CallbacksKeeper.getInstance().getCallback(TAG, OperationType.COMMENTS_GET));
            }
            String currentUserId = ApiUI.getCurrentUserId(QuestionDetailsActivity.this);
            if (currentUserId != null) {
                boolean anonymous = entry.isAnonymous() &&
                        currentUserId.equals(entry.getAuthor().getUid());
                anonymityCheckBox.setChecked(anonymous);
            }
        }
    }

    class QuestionUpdateTask extends AsyncTask<Void, Void, QuestionEntry> {

        @Override
        protected QuestionEntry doInBackground(Void... params) {
            int qid = getIntent().getIntExtra(IntentExtras.Q_ID, -1);
//            Assert.assertNotSame("Q_ID is null", qid, -1);

            QuestionEntry entry = QuestionEntry.byQId(qid);
//            Assert.assertNotSame("entry is null", entry, null);

            return entry;
        }

        @Override
        protected void onPostExecute(QuestionEntry entry) {
            // QuestionView will be migrated into list element
            adapter.updateQuestionEntry(entry);
        }
    }


    private class CommentsLoaderCallbacksImpl implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if  (id == LoaderIds.COMMENTS_LOADER) {
                Integer questionId = null;
                Integer entryPos = null;
                Integer insertedCount = null;
                int loadIntention = LoadIntention.REFRESH;
                boolean prepend = false;
                if (args != null) {
                    questionId = args.getInt(CommentsLoader.BundleKeys.QUESTION_ID, -1);
                    if (questionId == -1) questionId = null;

                    entryPos = args.getInt(CommentsLoader.BundleKeys.ENTRY_POSITION, -1);
                    if (entryPos == -1) entryPos = null;

                    insertedCount = args.getInt(CommentsLoader.BundleKeys.INSERTED_COUNT, -1);
                    if (insertedCount == -1) insertedCount = null;

                    loadIntention = args.getInt(CommentsLoader.BundleKeys.LOAD_INTENTION, LoadIntention.REFRESH);

                    prepend = args.getBoolean(CommentsLoader.BundleKeys.PREPEND, false);
                }

                return new CommentsLoader(QuestionDetailsActivity.this, questionId, entryPos, insertedCount, loadIntention, prepend);
            }
            throw new IllegalArgumentException("Loader with given id is not found");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
            CommentsLoader questionsLoader = (CommentsLoader) loader;
            Integer entryPos = questionsLoader.getEntryPosition();
            Integer count = questionsLoader.getInsertedCount();
            int loadIntention = questionsLoader.getLoadIntention();
            boolean prepend = questionsLoader.isPrepend();

            if (loadIntention == LoadIntention.REFRESH) {
                commentsOffset = newCursor.getCount();
                adapter.swapCursor(newCursor);
            } else {
                if (entryPos != null) {
                    adapter.swapCursor(newCursor, entryPos);
                } else if (count != null) {
                    commentsOffset += count;
                    if (prepend) {
                        adapter.swapCursor(newCursor, 0, count);
                        commentsView.scrollToPosition(count + 2);
                    } else {
                        adapter.swapCursor(newCursor, 2 + newCursor.getCount() - count, count);
                        commentsView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                } else {
                    commentsOffset = newCursor.getCount();
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
