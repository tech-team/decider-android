package org.techteam.decider.gui.activities;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.materialdrawer.model.BaseDrawerItem;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.gui.adapters.CategoriesListAdapter;
import org.techteam.decider.gui.fragments.OnCategorySelectedListener;

public class CategoriesDrawerItem
        extends BaseDrawerItem<CategoriesDrawerItem> {

    CategoriesListAdapter categoriesListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    public CategoriesDrawerItem(CategoriesListAdapter categoriesListAdapter) {
        this.categoriesListAdapter = categoriesListAdapter;
    }

    @Override
    public View convertView(LayoutInflater layoutInflater, View view, ViewGroup viewGroup) {
        //TODO: reuseability, viewholders
        View v = layoutInflater.inflate(R.layout.fragment_categories_list, viewGroup, false);
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.categories_recycler);
        mLayoutManager = new RecyclerViewWrapContentLinearLayoutManager(v.getContext(), 1, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(categoriesListAdapter);

        return v;
    }

    @Override
    public String getType() {
        return this.getClass().getName();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_categories_list;
    }
}
