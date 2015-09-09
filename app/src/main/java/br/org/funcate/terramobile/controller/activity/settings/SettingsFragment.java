package br.org.funcate.terramobile.controller.activity.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.util.Message;

/**
 * Fragment that shows the settings of the system
 *
 * Created by marcelo on 5/25/15.
 */
public class SettingsFragment extends PreferenceFragment{

    private GPSSettingController gpsSettingController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        SettingsActivity activity = (SettingsActivity)getActivity();
        if(activity!=null) {
            gpsSettingController = activity.getGPSSettingController();
            if (gpsSettingController != null) {
                Boolean stateLocation = gpsSettingController.getGPSLocationState();
                Boolean stateCenter = gpsSettingController.getGPSCenterState();
                if (stateLocation != null) {
                    CheckBoxPreference preference = (CheckBoxPreference)findPreference("keep_gps_location");
                    preference.setChecked(stateLocation);
                }
                if (stateCenter != null) {
                    CheckBoxPreference preference = (CheckBoxPreference)findPreference("keep_gps_center");
                    preference.setChecked(stateCenter);
                }
            }
        }
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
        if(preference.getKey().equalsIgnoreCase("keep_gps_location")) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference)preference;
            boolean state = checkBoxPreference.isChecked();
            if(gpsSettingController!=null) {
                gpsSettingController.saveGPSLocation(state);
                gpsSettingController.applyGPSLocation(state);
            }else{
                Message.showErrorMessage(getActivity(), R.string.title_activity_settings, R.string.settings_update_exception);
            }
        }
        if(preference.getKey().equalsIgnoreCase("keep_gps_center")) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference)preference;
            boolean state = checkBoxPreference.isChecked();
            if(gpsSettingController!=null) {
                gpsSettingController.saveKeepCenter(state);
                gpsSettingController.applyKeepCenter(state);
            }else{
                Message.showErrorMessage(getActivity(), R.string.title_activity_settings, R.string.settings_update_exception);
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
