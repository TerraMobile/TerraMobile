package br.org.funcate.terramobile.controller.activity.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class CredentialsFragment extends DialogFragment{
    private EditText eTUserName;
    private EditText eTPassword;
    private EditText eTRetypePassword;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.fragment_credentials, null);

        eTUserName = (EditText) v.findViewById(R.id.userNameCredentials);
        eTPassword = (EditText) v.findViewById(R.id.passwordCredentials);
        eTRetypePassword = (EditText) v.findViewById(R.id.retypePasswordCredentials);

        Button btnSave = (Button) v.findViewById(R.id.btnSave);
        Button btnCancel = (Button) v.findViewById(R.id.btnCancel);

        SettingsDAO settingsDAO = new SettingsDAO(getActivity());
        Settings settings = settingsDAO.getById(1);
        if (settings != null) {
            eTUserName.setText(settings.getUserName());
            eTPassword.setText(settings.getPassword());
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    SettingsFragment settingsFragment = (SettingsFragment) getActivity().getFragmentManager().findFragmentByTag("settings");
                    SharedPreferences sharedPreferences = settingsFragment.getPreferenceManager().getSharedPreferences();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userName", eTUserName.getText().toString());
                    editor.putString("password", eTPassword.getText().toString());
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
                .setTitle(R.string.credentials)
                .setCancelable(true)
                .create();
    }

    private boolean validateFields(){
        if(eTUserName.getText().toString().isEmpty()){
            eTUserName.setError(getResources().getString(R.string.errorUserName));
            return false;
        }
        if(eTPassword.getText().toString().isEmpty()){
            eTPassword.setError(getResources().getString(R.string.errorPassword));
            return false;
        }
        if(eTRetypePassword.getText().toString().isEmpty()){
            eTRetypePassword.setError(getResources().getString(R.string.errorRetypePassword));
            return false;
        }
        if(!eTPassword.getText().toString().equals(eTRetypePassword.getText().toString())){
            eTPassword.setError(getResources().getString(R.string.errorPasswordsMatch));
            eTRetypePassword.setError(getResources().getString(R.string.errorPasswordsMatch));
            return false;
        }
        return true;
    }
}