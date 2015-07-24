package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.techteam.decider.R;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.gui.activities.AddQuestionActivity;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.activities.QuestionDetailsActivity;
import org.techteam.decider.gui.adapters.ColoredAdapter;
import org.techteam.decider.gui.widget.SlidingTabLayout;
import org.techteam.decider.rest.CallbacksKeeper;
import org.techteam.decider.rest.service_helper.ServiceHelper;

import java.util.LinkedList;
import java.util.List;

public class MainFragment
        extends Fragment {

    public static final String TAG = MainFragment.class.toString();
    private static final int ADD_QUESTION = 0;
    private static final int QUESTION_DETAILS = 1;
    private MainActivity activity;

//    private CategoriesListAdapter categoriesListAdapter;
    private List<OnCategorySelectedListener> onCategorySelectedListeners = new LinkedList<>();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    private FloatingActionButton createPostButton;

    //private Map<Integer, CategoryEntry> selectedCategories = new HashMap<>();

    private static final class BundleKeys {
        public static final String PENDING_OPERATIONS = "PENDING_OPERATIONS";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // sections
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) rootView.findViewById(R.id.sections_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.sections_pager_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        mSlidingTabLayout.setBackgroundColor(getResources().getColor(R.color.primary));
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(android.R.color.white);
            }
        });

        // setup plus button
        createPostButton = (FloatingActionButton) rootView.findViewById(R.id.create_post_button);
        createPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddQuestionActivity.class);
                startActivityForResult(intent, ADD_QUESTION);
            }
        });

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

        if (savedInstanceState != null) {

        }

//        ActionBar actionBar = this.activity.getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeButtonEnabled(true);
//        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_QUESTION && resultCode == Activity.RESULT_OK) {
            int qid = data.getIntExtra(AddQuestionActivity.QUESTION_ID, -1);
            Intent intent = new Intent(getActivity(), QuestionDetailsActivity.class);
            intent.putExtra(QuestionDetailsActivity.IntentExtras.Q_ID, qid);
            intent.putExtra(QuestionDetailsActivity.IntentExtras.FORCE_REFRESH, true);
            intent.putExtra(QuestionDetailsActivity.IntentExtras.AFTER_CREATE, true);
            startActivityForResult(intent, QUESTION_DETAILS);
        } else if (requestCode == QUESTION_DETAILS && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(QuestionDetailsActivity.IntentExtras.AFTER_CREATE, false)) {
                refreshPages();
            } else {
                int position = data.getIntExtra(QuestionDetailsActivity.IntentExtras.ENTRY_POSITION, -1);
                if (position != -1) {
                    QuestionsListFragment f = (QuestionsListFragment) getCurrentlyActiveFragment();
                    f.getAdapter().notifyItemChanged(position);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public Fragment getCurrentlyActiveFragment() {
        int currentFragment = mViewPager.getCurrentItem();
        return (Fragment) mSectionsPagerAdapter.instantiateItem(mViewPager, currentFragment);
    }

    public void invalidatePages() {
        for (Fragment f: mSectionsPagerAdapter.getFragments()) {
            if (f != null) {
                QuestionsListFragment questionsListFragment = (QuestionsListFragment) f;
                questionsListFragment.invalidate();
            }
        }
    }

    public void refreshPages() {
        for (Fragment f: mSectionsPagerAdapter.getFragments()) {
            if (f != null) {
                QuestionsListFragment questionsListFragment = (QuestionsListFragment) f;
                questionsListFragment.refresh();
            }
        }
    }

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter implements ColoredAdapter {

        private Fragment[] fragments;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new Fragment[getCount()];
        }

        @Override
        public Fragment getItem(int position) {
            if (fragments[position] == null) {
                QuestionsListFragment f = QuestionsListFragment.create(ContentSection.fromInt(position));
                onCategorySelectedListeners.add(f);
                fragments[position] = f;
            }
            return fragments[position];
        }

        @Override
        public int getCount() {
            return ContentSection.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int resId = ContentSection.fromInt(position).getResId();
            return getString(resId);
        }

        @Override
        public int getTextColor() {
            return android.R.color.white;
        }

        public Fragment[] getFragments() {
            return fragments;
        }
    }
}
