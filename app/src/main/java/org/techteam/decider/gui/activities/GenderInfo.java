package org.techteam.decider.gui.activities;

import android.content.Context;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.UserEntry;

class GenderInfo {
    private Context context;
    private UserEntry.Gender value;

    public GenderInfo(Context context, UserEntry.Gender genderValue) {
        this.context = context;
        value = genderValue;
    }

    public UserEntry.Gender getValue() {
        return value;
    }

    @Override
    public String toString() {
        switch (value) {
            case Male:
                return context.getString(R.string.gender_male);
            case Female:
                return context.getString(R.string.gender_female);
            case None:
            default:
                return context.getString(R.string.gender_none);
        }
    }
}
