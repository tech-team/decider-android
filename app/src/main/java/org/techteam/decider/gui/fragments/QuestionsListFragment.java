package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.adapters.PostsListAdapter;
import org.techteam.decider.gui.loaders.ContentLoader;
import org.techteam.decider.gui.loaders.LoadIntention;
import org.techteam.decider.gui.loaders.LoaderIds;
import org.techteam.decider.gui.views.QuestionInteractor;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.service_helper.ServiceCallback;
import org.techteam.decider.rest.service_helper.ServiceHelper;
import org.techteam.decider.util.Toaster;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
    private List<CategoryEntry> chosenCategories = new LinkedList<>();

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

    private LoaderManager.LoaderCallbacks<Cursor> contentDataLoaderCallbacks = new LoaderCallbacksImpl();

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
    }

    @Deprecated
    private void setPosts(ArrayList<QuestionEntry> entries) {
        adapter.setAll(entries);
        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
            adapter.notifyDataSetChanged();
        } else {
            delayedAdapterNotifications.add(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Deprecated
    private void addPosts(ArrayList<QuestionEntry> entries) {
        final int oldCount = adapter.getItemCount() - 1; //minus "Loading..." item
        final int addedCount = entries.size();

        final String str = Integer.toString(addedCount) + " posts added";

        adapter.addAll(entries);
        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
            adapter.notifyItemRangeInserted(oldCount, addedCount);
            Toaster.toast(getActivity(), str + " (right away)");
        } else {
            delayedAdapterNotifications.add(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyItemRangeInserted(oldCount, addedCount);
                    Toaster.toast(getActivity(), str + " (after scroll)");
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_questions_list, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.posts_recycler);
        recyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        adapter = new PostsListAdapter(null, getActivity(), QuestionsListFragment.this, QuestionsListFragment.this, QuestionsListFragment.this);
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

                String msg;
                if (isFeedFinished) {
                    msg = "No more posts";
                } else {
                    msg = "Successfully fetched posts";
                    Bundle args = new Bundle();
                    args.putInt(ContentLoader.BundleKeys.INSERTED_COUNT, insertedCount);
                    args.putInt(ContentLoader.BundleKeys.LOAD_INTENTION, loadIntention);
                    args.putInt(ContentLoader.BundleKeys.SECTION, loadedSection);
                    getLoaderManager().restartLoader(LoaderIds.CONTENT_LOADER, args, contentDataLoaderCallbacks);
                }

//                Toaster.toast(getActivity().getApplicationContext(), msg);
                System.out.println(msg);
            }

            @Override
            public void onError(String operationId, Bundle data, String message) {
                mSwipeRefreshLayout.setRefreshing(false);
                String msg = "Error. " + message;
                Toaster.toast(getActivity().getApplicationContext(), msg);
                System.out.println(msg);
            }
        });

//        callbacksKeeper.addCallback(OperationType.BASH_VOTE, new ServiceCallback() {
//            @Override
//            public void onSuccess(String operationId, Bundle data) {
//                String entryId = data.getString(ServiceCallback.BashVoteExtras.ENTRY_ID);
//                int entryPosition = data.getInt(BashVoteExtras.ENTRY_POSITION);
//
//                String msg = "Voted for entry: " + entryId;
////                Toaster.toast(getActivity().getApplicationContext(), msg);
//                System.out.println(msg);
//
//                Bundle args = new Bundle();
//                args.putInt(ContentLoader.BundleKeys.ENTRY_POSITION, entryPosition);
//                getLoaderManager().restartLoader(LoaderIds.CONTENT_LOADER, args, contentDataLoaderCallbacks);
//            }
//
//            @Override
//            public void onError(String operationId, Bundle data, String message) {
//                String entryId = data.getString(ServiceCallback.BashVoteExtras.ENTRY_ID);
//
//                String msg = "Vote failed for entry: " + entryId + ". " + message;
//                Toaster.toast(getActivity().getApplicationContext(), msg);
//                System.out.println(msg);
//            }
//        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        if (savedInstanceState == null) {
            //factory = new ContentFactory(Locale.getDefault().toString());
        } else {
            //factory = savedInstanceState.getParcelable(BundleKeys.FACTORY);
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

        getLoaderManager().initLoader(LoaderIds.CONTENT_LOADER, null, contentDataLoaderCallbacks);
        initialized = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        serviceHelper.saveOperationsState(outState, BundleKeys.PENDING_OPERATIONS);
        //TODO
        //outState.putParcelable(BundleKeys.FACTORY, factory);

    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);

//        Toaster.toast(getActivity().getBaseContext(), R.string.loading);

        serviceHelper.getQuestions(currentSection,
                QUESTIONS_LIMIT,
                questionsOffset,
                chosenCategories,
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

//        serviceHelper.getQuestions(currentSection,
//                QUESTIONS_LIMIT,
//                questionsOffset = 0,
//                chosenCategories,
//                intention,
//                callbacksKeeper.getCallback(OperationType.GET_QUESTIONS));

//        serviceHelper.getQuestions(currentSection, QUESTIONS_LIMIT, questionsOffset, chosenCategories, intention, callbacksKeeper.getCallback(OperationType.GET_QUESTIONS));
    }

    @Override
    public void onCommentsClick(QuestionEntry entry) {
        QuestionDetailsFragment detailsFragment = new QuestionDetailsFragment();
        Bundle detailsBundle = new Bundle();
        detailsBundle.putInt("qid", entry.getQId());
        detailsFragment.setArguments(detailsBundle);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.content_frame, detailsFragment)
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
        System.out.println("onResume");
        serviceHelper.init();
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("onPause");
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

            getLoaderManager().restartLoader(LoaderIds.CONTENT_LOADER, null, contentDataLoaderCallbacks);
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    private class LoaderCallbacksImpl implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if  (id == LoaderIds.CONTENT_LOADER) {
                Integer entryPos = null;
                Integer insertedCount = null;
                int loadIntention = LoadIntention.REFRESH;
                ContentSection loadedSection = currentSection;
                if (args != null) {
                    entryPos = args.getInt(ContentLoader.BundleKeys.ENTRY_POSITION, -1);
                    entryPos = entryPos == -1 ? null : entryPos;

                    insertedCount = args.getInt(ContentLoader.BundleKeys.INSERTED_COUNT, -1);
                    insertedCount = insertedCount == -1 ? null : insertedCount;

                    loadIntention = args.getInt(ContentLoader.BundleKeys.LOAD_INTENTION, LoadIntention.REFRESH);
                    loadedSection = ContentSection.fromInt(args.getInt(ContentLoader.BundleKeys.SECTION));
                }

                //TODO: you may ask: why don't just store section and categories here, as field? idk
                return new ContentLoader(getActivity(), loadedSection, entryPos, insertedCount, loadIntention);
            }
            throw new IllegalArgumentException("Loader with given id is not found");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
            ContentLoader contentLoader = (ContentLoader) loader;
            Integer entryPos = contentLoader.getEntryPosition();
            Integer count = contentLoader.getInsertedCount();
            int loadIntention = contentLoader.getLoadIntention();

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
