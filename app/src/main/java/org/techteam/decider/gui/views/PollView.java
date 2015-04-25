package org.techteam.decider.gui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.techteam.decider.content.entities.PollItemEntry;

import java.util.ArrayList;
import java.util.List;


public class PollView extends LinearLayout {
    public interface Listener {
        void polled(int pollItemId);
    }

    private Listener listener;

    private Integer votedId = null;
    private List<PollItemView> pollItemViews;

    public PollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

    }

    public void setItems(PollItemEntry[] items) {
        Context context = getContext();
        pollItemViews = new ArrayList<>();

        super.removeAllViews();
        for (final PollItemEntry entry : items) {
            PollItemView pollItemView = new PollItemView(context);
            pollItemView.setEntry(entry);
            pollItemView.setListener(new PollItemView.Listener() {
                @Override
                public void polled(PollItemView pollItemView, PollItemEntry pollItemEntry) {
                    onEntryClick(pollItemView, pollItemEntry);
                }
            });

            super.addView(pollItemView);
            pollItemViews.add(pollItemView);
        }

        super.requestLayout();
        super.invalidate();
    }

    protected void onEntryClick(PollItemView pollItemView, PollItemEntry pollItemEntry) {
        for (PollItemView item: pollItemViews) {
            item.setMarked(false);
        }

        if (listener != null) {
            listener.polled(pollItemEntry.getPid());
        }
    }

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
