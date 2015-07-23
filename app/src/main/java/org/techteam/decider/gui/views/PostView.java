package org.techteam.decider.gui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class PostView extends FrameLayout {
    public PostView(Context context) {
        super(context);
    }

    public PostView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected String getDateString(Date date) {
        if (date == null) {
            return null;
        }

        String dateString = DateFormat.getMediumDateFormat(getContext()).format(date);
        dateString += ", " + DateFormat.getTimeFormat(getContext()).format(date);

        return dateString;
    }
}
