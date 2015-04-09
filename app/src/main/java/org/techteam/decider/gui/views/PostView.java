package org.techteam.decider.gui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class PostView extends FrameLayout {
    // date formatters
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sourceDateFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat uiDateFormat =
            new SimpleDateFormat("d MMM, hh:mm");


    public PostView(Context context) {
        super(context);
    }

    public PostView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected String getDateString(String raw) {
        String result;

        try {
            Date date = sourceDateFormat.parse(raw);
            result = uiDateFormat.format(date);
        } catch (ParseException e) {
            result = "";
        }

        return result;
    }
}
