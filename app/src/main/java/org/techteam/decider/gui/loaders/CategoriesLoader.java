package org.techteam.decider.gui.loaders;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;

import com.activeandroid.content.ContentProvider;

import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.entities.ContentCategory;
import org.techteam.decider.content.entities.QuestionEntry;

import java.util.List;

public class CategoriesLoader extends CursorLoader {


    public abstract class BundleKeys {
        public static final String ENTRY_POSITION = "ENTRY_POSITION";
        public static final String INSERTED_COUNT = "INSERTED_COUNT";
        public static final String LOAD_INTENTION = "LOAD_INTENTION";
    }

    public CategoriesLoader(Context context) {
        super(context);
    }

    @Override
    public Cursor loadInBackground() {
        Uri uri = ContentProvider.createUri(ContentCategory.class, null);
        return getContext().getContentResolver().query(uri, null, null, null, null);
    }
}