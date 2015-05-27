package br.org.funcate.terramobile.controller.activity;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import br.org.funcate.terramobile.R;

/**
 * Created by marcelo on 5/25/15.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if(preference.getKey().equalsIgnoreCase("credentials")){
            CredentialsFragment credentialsFragment = new CredentialsFragment();
            credentialsFragment.setCancelable(true);
            credentialsFragment.show(getActivity().getFragmentManager(),"credentials");
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
