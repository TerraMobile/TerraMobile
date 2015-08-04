package br.org.funcate.terramobile.controller.activity.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Activity that shows a Fragment with all the settings of the system
 */
public class SettingsActivity extends PreferenceActivity {

    private SettingsController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment(), "settings").commit();
        controller = new SettingsController(this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // DON'T DELETE THIS METHOD
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Logo's click listener
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        finish(); // Finish the activity after touch the logo
        return super.onOptionsItemSelected(item);
    }

    public SettingsController getController() {
        return controller;
    }

    public void setController(SettingsController controller) {
        this.controller = controller;
    }
}
