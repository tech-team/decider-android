package org.techteam.decider.gui.activities;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class ToolbarActivity extends AppCompatActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
