package org.techteam.decider.gui.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.techteam.decider.R;


public class PreferencesFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
