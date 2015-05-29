package br.org.funcate.terramobile.controller.activity.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.View;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.Settings;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;

/**
 * Fragment that shows the settings of the system
 *
 * Created by marcelo on 5/25/15.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        SettingsDAO settingsDAO = new SettingsDAO(getActivity());
        if(settingsDAO.getById(1) == null){
            Settings settings = new Settings();
            settings.setId(1);
            settings.setUserName("");
            settings.setPassword("");
            settings.setUrl("");
            settingsDAO.insert(settings);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if(preference.getKey().equalsIgnoreCase("credentials")){
            CredentialsFragment credentialsFragment = new CredentialsFragment();
            credentialsFragment.setCancelable(true);
            credentialsFragment.show(getActivity().getFragmentManager(), "credentials");
        }
        if(preference.getKey().equalsIgnoreCase("url")) {
            SettingsDAO settingsDAO = new SettingsDAO(getActivity());
            Settings settings = settingsDAO.getById(1);
            if (settings != null) {
                SettingsFragment settingsFragment = (SettingsFragment) getActivity().getFragmentManager().findFragmentByTag("settings");
                SharedPreferences sharedPreferences = settingsFragment.getPreferenceManager().getSharedPreferences();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("url", String.valueOf(settings.getUrl()));
                editor.commit();
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SettingsDAO settingsDAO = new SettingsDAO(getActivity());
        Settings settings;
        if (settingsDAO.getById(1) != null) {
            settings = settingsDAO.getById(1);
            settings.setId(1);
            if (key.equals("url")) {
                settings.setUrl(sharedPreferences.getString(key, ""));
            } else if (key.equals("userName")) {
                settings.setUserName(sharedPreferences.getString(key, ""));
            } else if (key.equals("password")) {
                settings.setPassword(sharedPreferences.getString(key, ""));
            }
            settingsDAO.update(settings);
        }
    }

    public void onResume() {
        super.onResume();
        getPreferenceScreen()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
