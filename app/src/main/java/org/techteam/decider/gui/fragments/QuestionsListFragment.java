package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.techteam.decider.R;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.CategoriesGetter;
import org.techteam.decider.gui.ServiceHelperGetter;
import org.techteam.decider.gui.activities.ActivityHelper;
import org.techteam.decider.gui.activities.QuestionDetailsActivity;
import org.techteam.decider.gui.activities.lib.AuthTokenGetter;
import org.techteam.decider.gui.adapters.QuestionsListAdapter;
import org.techteam.decider.gui.loaders.LoadIntention;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.gui.loaders.QuestionsLoader;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;

import java.util.LinkedList;
import java.util.Queue;

public class QuestionsListFragment
        extends Fragment
        implements
        SwipeRefreshLayout.OnRefreshListener,
        OnQuestionEventCallback,
        OnListScrolledDownCallback,
        SharedPreferences.OnSharedPreferenceChangeListener,
        OnCategorySelectedListener {

    public static final String TAG = QuestionsListFragment.class.getName();

    private static final int QUESTIONS_LIMIT = 30;
    private int questionsOffset = 0;
    private ContentSection currentSection = null;

    //see comment in onCreateView()
    private Queue<Runnable> delayedAdapterNotifications = new LinkedList<Runnable>();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView recyclerView;
    private TextView emptyListView;
    private QuestionsListAdapter adapter;

    private ServiceHelperGetter serviceHelperGetter;
    public boolean refreshing = false;

    private CategoriesGetter categoriesGetter;
    private AuthTokenGetter authTokenGetter;

    private LoaderManager.LoaderCallbacks<Cursor> questionsLoaderCallbacks = new QuestionsLoaderCallbacksImpl();

    private String serviceTag = null;
    private String getServiceTag() {
        if (serviceTag == null) {
            if (currentSection != null) {
                serviceTag = TAG + currentSection.toString();
            } else {
                serviceTag = TAG;
            }
        }
        return serviceTag;
    }

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
        public static final String QUESTIONS_OFFSET = "QUESTIONS_OFFSET";
        public static final String CURRENT_SECTION = "CURRENT_SECTION";
    }

    public static QuestionsListFragment create(ContentSection section) {
        QuestionsListFragment f = new QuestionsListFragment();
        f.currentSection = section;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_questions_list, container, false);

        emptyListView = (TextView) rootView.findViewById(R.id.empty_list);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.posts_recycler);
        recyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        //this thing waits for user to stop scrolling and adds new data or refreshes existing data
        //because it's impossible to notify*() adapter when scrolling
        // (getting IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling)
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    for (Runnable r : delayedAdapterNotifications)
                        r.run();

                    delayedAdapterNotifications.clear();
                }
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.categoriesGetter = (CategoriesGetter) activity;
        this.authTokenGetter = (AuthTokenGetter) activity;
        this.serviceHelperGetter = (ServiceHelperGetter) activity;
        String tag = getServiceTag();
        CallbacksKeeper callbacksKeeper = CallbacksKeeper.getInstance();
        callbacksKeeper.addCallback(tag, OperationType.QUESTIONS_GET, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                mSwipeRefreshLayout.setRefreshing(false);

                //TODO
                //content = data.getParcelable(GetPostsExtras.NEW_CONTENT_SOURCE);
                boolean isFeedFinished = data.getBoolean(GetQuestionsExtras.FEED_FINISHED, false);
                int insertedCount = data.getInt(GetQuestionsExtras.COUNT, -1);
                int loadIntention = data.getInt(GetQuestionsExtras.LOAD_INTENTION, LoadIntention.REFRESH);
                int loadedSection = data.getInt(GetQuestionsExtras.SECTION);

                String msg;
                if (isFeedFinished) {
                    msg = "No more posts";
                } else {
                    msg = "Successfully fetched posts";
                    if (isAdded()) {
                        Bundle args = new Bundle();
                        args.putInt(QuestionsLoader.BundleKeys.INSERTED_COUNT, insertedCount);
                        args.putInt(QuestionsLoader.BundleKeys.LOAD_INTENTION, loadIntention);
                        args.putInt(QuestionsLoader.BundleKeys.SECTION, loadedSection);
                        args.putBoolean(QuestionsLoader.BundleKeys.FEED_FINISHED, isFeedFinished);
                        getLoaderManager().restartLoader(LoaderIds.QUESTIONS_LOADER, args, questionsLoaderCallbacks);
                    }
                }

                adapter.setFeedFinished(isFeedFinished);
                if (insertedCount == 0 && adapter.getItemCount() == 1 && isFeedFinished) {
                    //recyclerView.setVisibility(View.GONE);
                    adapter.swapCursor(null);
                    emptyListView.setVisibility(View.VISIBLE);
                } else {
                    //recyclerView.setVisibility(View.VISIBLE);
                    emptyListView.setVisibility(View.GONE);
                }
                // 1 is like 0, ... but 1 ("Loading..." entry)


                Log.i(TAG, msg);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                mSwipeRefreshLayout.setRefreshing(false);
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        authTokenGetter.getAuthTokenOrExit(null);
                        return;
                    case ErrorsExtras.GenericErrors.SERVER_ERROR:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.server_problem);
                        return;
                    case ErrorsExtras.GenericErrors.NO_INTERNET:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.no_internet);
                        return;
                    case ErrorsExtras.GenericErrors.INTERNAL_PROBLEMS:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.internal_problems);
                        return;
                }
                Toaster.toastLong(getActivity().getApplicationContext(), message);
            }
        });

        callbacksKeeper.addCallback(tag, OperationType.POLL_VOTE, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                int entryPosition = data.getInt(PollVoteExtras.ENTRY_POSITION);
                Bundle args = new Bundle();
                args.putInt(QuestionsLoader.BundleKeys.ENTRY_POSITION, entryPosition);
                if (isAdded()) {
                    getLoaderManager().restartLoader(LoaderIds.QUESTIONS_LOADER, args, questionsLoaderCallbacks);
                }
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        authTokenGetter.getAuthTokenOrExit(null);
                        return;
                    case ErrorsExtras.GenericErrors.SERVER_ERROR:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.server_problem);
                        return;
                    case ErrorsExtras.GenericErrors.NO_INTERNET:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.no_internet);
                        return;
                    case ErrorsExtras.GenericErrors.INTERNAL_PROBLEMS:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.internal_problems);
                        return;
                }

                int serverErrorCode = data.getInt(ErrorsExtras.SERVER_ERROR_CODE, -1);
                switch (serverErrorCode) {
                    case PollVoteExtras.ErrorCodes.ALREADY_VOTED:
                        Toaster.toast(getActivity().getApplicationContext(), R.string.already_voted);
                        return;
                    default:
                        break;
                }
                Toaster.toastLong(getActivity().getApplicationContext(), message);
            }
        });

        callbacksKeeper.addCallback(tag, OperationType.QUESTION_LIKE, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                int entryPosition = data.getInt(PollVoteExtras.ENTRY_POSITION);
                Bundle args = new Bundle();
                args.putInt(QuestionsLoader.BundleKeys.ENTRY_POSITION, entryPosition);
                if (isAdded()) {
                    getLoaderManager().restartLoader(LoaderIds.QUESTIONS_LOADER, args, questionsLoaderCallbacks);
                }
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        authTokenGetter.getAuthTokenOrExit(null);
                        return;
                    case ErrorsExtras.GenericErrors.SERVER_ERROR:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.server_problem);
                        return;
                    case ErrorsExtras.GenericErrors.NO_INTERNET:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.no_internet);
                        return;
                    case ErrorsExtras.GenericErrors.INTERNAL_PROBLEMS:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.internal_problems);
                        return;
                }
                Toaster.toastLong(getActivity().getApplicationContext(), message);
            }
        });

        callbacksKeeper.addCallback(tag, OperationType.QUESTION_REPORT_SPAM, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toastLong(getActivity().getApplicationContext(), R.string.question_marked_spam);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                int code = data.getInt(ErrorsExtras.GENERIC_ERROR_CODE);
                switch (code) {
                    case ErrorsExtras.GenericErrors.INVALID_TOKEN:
                        authTokenGetter.getAuthTokenOrExit(null);
                        return;
                    case ErrorsExtras.GenericErrors.SERVER_ERROR:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.server_problem);
                        return;
                    case ErrorsExtras.GenericErrors.NO_INTERNET:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.no_internet);
                        return;
                    case ErrorsExtras.GenericErrors.INTERNAL_PROBLEMS:
                        Toaster.toastLong(getActivity().getApplicationContext(), R.string.internal_problems);
                        return;
                }
                Toaster.toastLong(getActivity().getApplicationContext(), message);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        if (savedInstanceState != null) {
//            boolean isRefreshing = serviceHelperGetter.restoreOperationsState(savedInstanceState,
//                    BundleKeys.PENDING_OPERATIONS,
//                    callbacksKeeper);
//
//            if (isRefreshing) {
//                // TODO: it is not working
//                mSwipeRefreshLayout.setRefreshing(true);
//            } else {
//                mSwipeRefreshLayout.setRefreshing(false);
//            }

            currentSection = ContentSection.fromInt(savedInstanceState.getInt(BundleKeys.CURRENT_SECTION));
            questionsOffset = savedInstanceState.getInt(BundleKeys.QUESTIONS_OFFSET); // TODO: probably it's not valid
        }

        adapter = new QuestionsListAdapter(null, getActivity(), currentSection, QuestionsListFragment.this, QuestionsListFragment.this);
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "restarting loader...");
        Bundle args = new Bundle();
        args.putInt(QuestionsLoader.BundleKeys.SECTION, currentSection.toInt());
        getLoaderManager().restartLoader(LoaderIds.QUESTIONS_LOADER, args, questionsLoaderCallbacks);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BundleKeys.QUESTIONS_OFFSET, questionsOffset);
        outState.putInt(BundleKeys.CURRENT_SECTION, currentSection.toInt());
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    public void refresh() {
        mSwipeRefreshLayout.setRefreshing(true);

//        Toaster.toast(getActivity().getBaseContext(), R.string.loading);

        questionsOffset = 0;
        serviceHelperGetter.getServiceHelper().getQuestions(getServiceTag(),
                currentSection,
                QUESTIONS_LIMIT,
                questionsOffset,
                categoriesGetter.getSelectedCategories(),
                LoadIntention.REFRESH,
                CallbacksKeeper.getInstance().getCallback(getServiceTag(), OperationType.QUESTIONS_GET));
    }

    @Override
    public void onScrolledDown() {
        MainFragment mainFragment = (MainFragment) getParentFragment();
//        if (mainFragment.getCurrentlyActiveFragment() == this) {
            int intention;
            if (adapter.getCursor().getCount() == 0) {
                intention = LoadIntention.REFRESH;
            } else {
                intention = LoadIntention.APPEND;
            }

            serviceHelperGetter.getServiceHelper().getQuestions(getServiceTag(),
                    currentSection,
                    QUESTIONS_LIMIT,
                    questionsOffset,
                    categoriesGetter.getSelectedCategories(),
                    intention,
                    CallbacksKeeper.getInstance().getCallback(getServiceTag(), OperationType.QUESTIONS_GET));
//        }
    }

    @Override
    public void onLikeClick(int entryPosition, QuestionEntry post) {
        serviceHelperGetter.getServiceHelper().likeQuestion(getServiceTag(),
                entryPosition,
                post.getQId(),
                CallbacksKeeper.getInstance().getCallback(getServiceTag(), OperationType.QUESTION_LIKE));
    }

    @Override
    public void onVoteClick(int entryPosition, QuestionEntry post, int voteId) {
        serviceHelperGetter.getServiceHelper().pollVote(getServiceTag(),
                entryPosition,
                post.getQId(),
                voteId,
                CallbacksKeeper.getInstance().getCallback(getServiceTag(), OperationType.POLL_VOTE));
    }

    @Override
    public void onCommentsClick(int entryPosition, QuestionEntry post) {
        Intent intent = new Intent(getActivity(), QuestionDetailsActivity.class);
        intent.putExtra(QuestionDetailsActivity.IntentExtras.Q_ID, post.getQId());
        intent.putExtra(QuestionDetailsActivity.IntentExtras.FORCE_REFRESH, true);
        intent.putExtra(QuestionDetailsActivity.IntentExtras.ENTRY_POSITION, entryPosition);
        getParentFragment().startActivityForResult(intent, ActivityHelper.QUESTION_DETAILS_REQUEST);
    }

    @Override
    public void onReportSpam(int entryPosition, QuestionEntry post) {
        serviceHelperGetter.getServiceHelper().reportSpamQuestion(getServiceTag(),
                entryPosition,
                post.getQId(),
                CallbacksKeeper.getInstance().getCallback(getServiceTag(), OperationType.QUESTION_REPORT_SPAM));
    }

    @Override
    public void categorySelected(CategoryEntry category, boolean isChecked) {
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (isAdded()) {
            Resources res = getResources();

            int resId = res.getIdentifier(key, "string", getActivity().getPackageName());

            //TODO: move defaults somewhere
            switch (resId) {
                //TODO: case R.string.pref_justify_by_width_key and so one

                default:
//                    Toaster.toast(getActivity().getBaseContext(),
//                            "Some settings changed programmatically");
                    break;
            }

            //cause posts to re-render

            getLoaderManager().restartLoader(LoaderIds.QUESTIONS_LOADER, null, questionsLoaderCallbacks);
        }
    }

    public void invalidate() {
        if (isAdded()) {
            adapter.swapCursor(null);
            refresh();
        }
    }

    public void setCurrentSection(ContentSection currentSection) {
        this.currentSection = currentSection;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityHelper.QUESTION_DETAILS_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                int position = data.getIntExtra(QuestionDetailsActivity.IntentExtras.ENTRY_POSITION, -1);
                if (position != -1) {
                    adapter.notifyItemChanged(position);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class QuestionsLoaderCallbacksImpl implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if  (id == LoaderIds.QUESTIONS_LOADER) {
                Integer entryPos = null;
                Integer insertedCount = null;
                int loadIntention = LoadIntention.REFRESH;
                boolean feedFinished = false;
                ContentSection loadedSection = currentSection;
                if (args != null) {
                    entryPos = args.getInt(QuestionsLoader.BundleKeys.ENTRY_POSITION, -1);
                    entryPos = entryPos == -1 ? null : entryPos;

                    insertedCount = args.getInt(QuestionsLoader.BundleKeys.INSERTED_COUNT, -1);
                    insertedCount = insertedCount == -1 ? null : insertedCount;

                    loadIntention = args.getInt(QuestionsLoader.BundleKeys.LOAD_INTENTION, LoadIntention.NONE);
                    int loadedSectionId = args.getInt(QuestionsLoader.BundleKeys.SECTION, -1);
                    if (loadedSectionId != -1) {
                        loadedSection = ContentSection.fromInt(loadedSectionId);
                    }

                    feedFinished = args.getBoolean(QuestionsLoader.BundleKeys.FEED_FINISHED, false);
                }

                return new QuestionsLoader(getActivity(), loadedSection, entryPos, insertedCount, loadIntention, feedFinished);
            }
            throw new IllegalArgumentException("Loader with given id is not found");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
            QuestionsLoader questionsLoader = (QuestionsLoader) loader;
            Integer entryPos = questionsLoader.getEntryPosition();
            Integer count = questionsLoader.getInsertedCount();
            int loadIntention = questionsLoader.getLoadIntention();
            boolean feedFinished = questionsLoader.isFeedFinished();

            if (loadIntention == LoadIntention.REFRESH) {
                questionsOffset = newCursor.getCount();
                adapter.swapCursor(newCursor);
            } else {
                if (entryPos != null) {
                    adapter.swapCursor(newCursor, entryPos);
                } else if (count != null) {
                    questionsOffset += count;
                    adapter.swapCursor(newCursor, newCursor.getCount() - count, count);
                } else {
                    questionsOffset = newCursor.getCount();
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
