package br.org.funcate.terramobile.controller.activity.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import br.org.funcate.terramobile.R;

/**
 * Fragment that shows the settings of the system
 *
 * Created by marcelo on 5/25/15.
 */
public class SettingsFragment extends PreferenceFragment{
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
            credentialsFragment.show(getActivity().getFragmentManager(), "credentials");
        }
        if(preference.getKey().equalsIgnoreCase("server_url")) {
            ServerURLFragment serverURLFragment = new ServerURLFragment();
            serverURLFragment.setCancelable(true);
            serverURLFragment.show(getActivity().getFragmentManager(), "serverURL");
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        SettingsDAO settingsDAO = new SettingsDAO(getActivity());
//        Settings settings = settingsDAO.getById(1);
//        if (settings != null) {
//            if (key.equals("serverURL")) {
//                settings.setUrl(sharedPreferences.getString(key, ""));
//            } else if (key.equals("userName")) {
//                settings.setUserName(sharedPreferences.getString(key, ""));
//            } else if (key.equals("password")) {
//                settings.setPassword(sharedPreferences.getString(key, ""));
//            }
//            settingsDAO.update(settings);
//        }
//    }
}
