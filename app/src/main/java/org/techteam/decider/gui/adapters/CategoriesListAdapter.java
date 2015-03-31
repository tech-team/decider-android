package org.techteam.decider.gui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.techteam.decider.R;

import java.util.List;

//TODO: change source to CursorLoader and all that things
public class CategoriesListAdapter
        extends ArrayAdapter<Category> {

    public CategoriesListAdapter(Context context, List<Category> categories) {
        super(context, 0, categories);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("ADAPTER", Integer.toString(position));

        final Category category = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_entry, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.name = (CheckBox) convertView.findViewById(R.id.category_checkbox);

            viewHolder.name.setOnClickListener( new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;

                    category.setSelected(cb.isChecked());
                }
            });

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.name.setChecked(category.isSelected());
        viewHolder.name.setText(category.getLabel());

        return convertView;
    }

    private class ViewHolder {
        public CheckBox name;
    }
}