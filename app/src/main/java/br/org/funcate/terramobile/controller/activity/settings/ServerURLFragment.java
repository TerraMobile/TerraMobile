package br.org.funcate.terramobile.controller.activity.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.Settings;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;

/**
 * DialogFragment to show the user's credentials form on the settings menu
 *
 * Created by marcelo on 5/25/15.
 */
public class ServerURLFragment extends DialogFragment{
    private EditText eTServerURL;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.fragment_server_url, null);

        eTServerURL = (EditText) v.findViewById(R.id.serverURL);

        Button btnSave = (Button) v.findViewById(R.id.btnSave);
        Button btnCancel = (Button) v.findViewById(R.id.btnCancel);

        SettingsDAO settingsDAO = new SettingsDAO(getActivity());
        Settings settings = settingsDAO.getById(1);
        if (settings != null) {
            eTServerURL.setText(settings.getUrl());
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    SettingsFragment settingsFragment = (SettingsFragment) getActivity().getFragmentManager().findFragmentByTag("settings");
                    SharedPreferences sharedPreferences = settingsFragment.getPreferenceManager().getSharedPreferences();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("serverURL", eTServerURL.getText().toString());
                    editor.apply();
                    dismiss();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.server_url)
                .setCancelable(true)
                .create();
    }

    private boolean validateFields(){
        String url = eTServerURL.getText().toString().trim();
        if(url.toString().endsWith("/"))
            url.substring(0, url.length() - 1);
        eTServerURL.setText(url.trim());
        if(Patterns.WEB_URL.matcher(eTServerURL.getText().toString().trim()).matches()){
            return true;
        }
        else {
            eTServerURL.setError("Invalid URL");
            return false;
        }
    }
}