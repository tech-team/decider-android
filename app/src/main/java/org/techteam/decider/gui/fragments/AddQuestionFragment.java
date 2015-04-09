package org.techteam.decider.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.techteam.decider.R;
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.util.Toaster;

public class AddQuestionFragment extends Fragment{
    private MainActivity activity;

    // child controls
    private EditText postText;
    private Spinner categorySpinner;
    private CheckBox anonymityCheckBox;

    // text choices
    private EditText textChoice1;
    private EditText textChoice2;

    // image choices
    //TODO

    private Button createButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_post, container, false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = (MainActivity) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // setup toolbar
        Toolbar toolbar = (Toolbar) this.activity.findViewById(R.id.post_add_toolbar);
        this.activity.setSupportActionBar(toolbar);

        ActionBar actionBar = this.activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // find controls
        postText = (EditText) this.activity.findViewById(R.id.add_post_text);
        categorySpinner = (Spinner) this.activity.findViewById(R.id.add_post_category_spinner);
        anonymityCheckBox = (CheckBox) this.activity.findViewById(R.id.add_post_anonymity_checkbox);

        // text choices
        textChoice1 = (EditText) this.activity.findViewById(R.id.add_post_text_choice1);
        textChoice2 = (EditText) this.activity.findViewById(R.id.add_post_text_choice2);

        createButton = (Button) this.activity.findViewById(R.id.add_post_send_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (createPost()) {
                    getActivity().onBackPressed();
                }
            }
        });
    }

    private boolean createPost() {
        // collect data
        String message = postText.getText().toString();
        //TODO: get category from spinner's adapter
        //categorySpinner
        boolean anonimity = anonymityCheckBox.isChecked();

        //TODO: check current question type
        // text choices
        String choice1 = textChoice1.getText().toString();
        String choice2 = textChoice2.getText().toString();

        // validate data
        if (message.isEmpty() || choice1.isEmpty() || choice2.isEmpty()) {
            Toaster.toast(getActivity(), R.string.fill_all_fields);
            return false;
        }

        // send if valid
        //TODO: send question

        return true;
    }
}
