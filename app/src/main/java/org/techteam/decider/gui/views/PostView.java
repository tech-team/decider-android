package org.techteam.decider.gui.views;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

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

        Calendar cal = new GregorianCalendar();
        long time = date.getTime();
        cal.setTimeInMillis(time);

        TimeZone tz = cal.getTimeZone();
        Date d = new Date();
        d.setTime(time + tz.getRawOffset());

        String dateString = DateFormat.getMediumDateFormat(getContext()).format(d);
        dateString += ", " + DateFormat.getTimeFormat(getContext()).format(d);

        return dateString;
    }
}
