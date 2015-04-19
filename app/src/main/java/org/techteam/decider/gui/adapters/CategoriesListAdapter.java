package org.techteam.decider.gui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.gui.fragments.OnCategorySelectedListener;

public class CategoriesListAdapter
        extends CursorRecyclerViewAdapter<CategoriesListAdapter.ViewHolder> {

    private Context context;
    private final OnCategorySelectedListener onCategorySelectedListener;

    public CategoriesListAdapter(Cursor contentCursor, Context context, OnCategorySelectedListener onCategorySelectedListener) {
        super(contentCursor);
        this.context = context;
        this.onCategorySelectedListener = onCategorySelectedListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor, int position) {
        final CategoryEntry category = CategoryEntry.fromCursor(cursor);
        viewHolder.name.setText(category.getLocalizedLabel());
        viewHolder.name.setChecked(category.isSelected());
        viewHolder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox b = (CheckBox) v;
                onCategorySelectedListener.categorySelected(category, b.isChecked());
            }
        });
    }

    @Override
    public CategoriesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.category_entry, parent, false);
        return new ViewHolder(v);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox name;

        public ViewHolder(View v) {
            super(v);
            name = (CheckBox) v.findViewById(R.id.category_checkbox);
        }
    }
}