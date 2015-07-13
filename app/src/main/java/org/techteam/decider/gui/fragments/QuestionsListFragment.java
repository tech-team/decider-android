package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Loader;
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

import org.techteam.decider.R;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.adapters.PostsListAdapter;
import org.techteam.decider.gui.loaders.QuestionsLoader;
import org.techteam.decider.gui.loaders.LoadIntention;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.gui.views.QuestionInteractor;
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
        QuestionInteractor,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = QuestionsListFragment.class.toString();


    private static final int QUESTIONS_LIMIT = 30;
    private int questionsOffset = 0;
    private ContentSection currentSection = ContentSection.NEW;

    //see comment in onCreateView()
    private Queue<Runnable> delayedAdapterNotifications = new LinkedList<Runnable>();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView recyclerView;
    private PostsListAdapter adapter;

    private CallbacksKeeper callbacksKeeper = new CallbacksKeeper();
    private ServiceHelper serviceHelper;
    public boolean refreshing = false;

    private MainActivity activity;
    private boolean initialized = false;

    private LoaderManager.LoaderCallbacks<Cursor> questionsLoaderCallbacks = new QuestionsLoaderCallbacksImpl();

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
    }

    public static QuestionsListFragment create(ContentSection section) {
        QuestionsListFragment f = new QuestionsListFragment();
        f.currentSection = section;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_questions_list, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.posts_recycler);
        recyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        adapter = new PostsListAdapter(null, getActivity(), currentSection, QuestionsListFragment.this, QuestionsListFragment.this, QuestionsListFragment.this);
        adapter.setOnQuestionEventCallback(new OnQuestionEventCallback() {
            @Override
            public void onLike(QuestionEntry post) {

            }

            @Override
            public void onVote(QuestionEntry post, int voteId) {
                Toaster.toast(activity.getBaseContext(), "Vote pressed. QId = " + post.getQId() + ". voteId = " + voteId);
                serviceHelper.pollVote(post.getQId(), voteId, callbacksKeeper.getCallback(OperationType.POLL_VOTE));
            }
        });
        recyclerView.setAdapter(adapter);

        //this thing waits for user to stop scrolling and adds new data or refreshes existing data
        //because it's impossible to notify*() adapter when scrolling
        // (getting IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling)
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    for (Runnable r: delayedAdapterNotifications)
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = (MainActivity) activity;
        serviceHelper = new ServiceHelper(activity);
        callbacksKeeper.addCallback(OperationType.GET_QUESTIONS, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                mSwipeRefreshLayout.setRefreshing(false);

                //TODO
                //content = data.getParcelable(GetPostsExtras.NEW_CONTENT_SOURCE);
                boolean isFeedFinished = data.getBoolean(GetQuestionsExtras.FEED_FINISHED, false);
                int insertedCount = data.getInt(GetQuestionsExtras.COUNT, -1);
                int loadIntention = data.getInt(GetQuestionsExtras.LOAD_INTENTION, LoadIntention.REFRESH);
                int loadedSection = data.getInt(GetQuestionsExtras.SECTION);

                questionsOffset += insertedCount;

                String msg;
                if (isFeedFinished) {
                    msg = "No more posts";
                } else {
                    msg = "Successfully fetched posts";
                    Bundle args = new Bundle();
                    args.putInt(QuestionsLoader.BundleKeys.INSERTED_COUNT, insertedCount);
                    args.putInt(QuestionsLoader.BundleKeys.LOAD_INTENTION, loadIntention);
                    args.putInt(QuestionsLoader.BundleKeys.SECTION, loadedSection);
                    getLoaderManager().restartLoader(LoaderIds.QUESTIONS_LOADER, args, questionsLoaderCallbacks);
                }

//                Toaster.toast(getActivity().getApplicationContext(), msg);
                System.out.println(msg);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                mSwipeRefreshLayout.setRefreshing(false);
                String msg = "Error. " + message;
                Toaster.toastLong(getActivity().getApplicationContext(), msg);
                System.out.println(msg);
            }
        });

        callbacksKeeper.addCallback(OperationType.POLL_VOTE, new ServiceCallback() {
            @Override
            public void onSuccess(String operationId, Bundle data) {
                Toaster.toastLong(getActivity().getApplicationContext(), "Successfully voted");
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                String msg = "Error. " + message;
                Toaster.toastLong(getActivity().getApplicationContext(), msg);
                System.out.println(msg);
            }
        });
        Log.d(TAG, "restarting loader...");
        Bundle args = new Bundle();
        args.putInt(QuestionsLoader.BundleKeys.SECTION, currentSection.toInt());
        getLoaderManager().restartLoader(LoaderIds.QUESTIONS_LOADER, args, questionsLoaderCallbacks);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        if (savedInstanceState == null) {

        } else {

            boolean isRefreshing = serviceHelper.restoreOperationsState(savedInstanceState,
                    BundleKeys.PENDING_OPERATIONS,
                    callbacksKeeper);

            if (isRefreshing) {
                // TODO: it is not working
                mSwipeRefreshLayout.setRefreshing(true);
            } else {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
        initialized = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        serviceHelper.saveOperationsState(outState, BundleKeys.PENDING_OPERATIONS);

    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);

//        Toaster.toast(getActivity().getBaseContext(), R.string.loading);

        serviceHelper.getQuestions(currentSection,
                QUESTIONS_LIMIT,
                questionsOffset = 0,
                activity.getSelectedCategories(),
                LoadIntention.REFRESH,
                callbacksKeeper.getCallback(OperationType.GET_QUESTIONS));
    }

    @Override
    public void onScrolledDown() {
//        Toaster.toast(getActivity().getBaseContext(), "Bottom reached");

        System.out.println("Load has begun");
        int intention;
        if (adapter.getCursor().getCount() == 0) {
            intention = LoadIntention.REFRESH;
        } else {
            intention = LoadIntention.APPEND;
        }

        serviceHelper.getQuestions(currentSection,
                QUESTIONS_LIMIT,
                questionsOffset,
                activity.getSelectedCategories(),
                intention,
                callbacksKeeper.getCallback(OperationType.GET_QUESTIONS));

//        serviceHelper.getQuestions(currentSection, QUESTIONS_LIMIT, questionsOffset, chosenCategories, intention, callbacksKeeper.getCallback(OperationType.GET_QUESTIONS));
    }

    @Override
    public void onCommentsClick(QuestionEntry entry) {
        QuestionDetailsFragment detailsFragment = new QuestionDetailsFragment();
        Bundle detailsBundle = new Bundle();
        detailsBundle.putInt(QuestionDetailsFragment.BundleKeys.Q_ID, entry.getQId());
        detailsFragment.setArguments(detailsBundle);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.content_frame, detailsFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("mainFragment")
                .commit();
    }

    @Override
    public void onLikeClick(QuestionEntry post) {
        Toaster.toast(getActivity(), "Like pressed");
    }

    @Override
    public void onLike(QuestionEntry post) {
        Toaster.toast(getActivity(), "Liked successfully");
    }

    @Override
    public void onVote(QuestionEntry post, int voteId) {
        Toaster.toast(getActivity(), "Voted successfully");
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

    public void setCurrentSection(ContentSection currentSection) {
        this.currentSection = currentSection;
    }

    public boolean isInitialized() {
        return initialized;
    }

    private class QuestionsLoaderCallbacksImpl implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if  (id == LoaderIds.QUESTIONS_LOADER) {
                Integer entryPos = null;
                Integer insertedCount = null;
                int loadIntention = LoadIntention.REFRESH;
                ContentSection loadedSection = currentSection;
                if (args != null) {
                    entryPos = args.getInt(QuestionsLoader.BundleKeys.ENTRY_POSITION, -1);
                    entryPos = entryPos == -1 ? null : entryPos;

                    insertedCount = args.getInt(QuestionsLoader.BundleKeys.INSERTED_COUNT, -1);
                    insertedCount = insertedCount == -1 ? null : insertedCount;

                    loadIntention = args.getInt(QuestionsLoader.BundleKeys.LOAD_INTENTION, LoadIntention.REFRESH);
                    loadedSection = ContentSection.fromInt(args.getInt(QuestionsLoader.BundleKeys.SECTION));
                }

                //TODO: you may ask: why don't just store section and categories here, as field? idk
                return new QuestionsLoader(getActivity(), loadedSection, entryPos, insertedCount, loadIntention);
            }
            throw new IllegalArgumentException("Loader with given id is not found");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
            QuestionsLoader questionsLoader = (QuestionsLoader) loader;
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
