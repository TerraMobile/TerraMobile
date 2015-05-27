package br.org.funcate.terramobile.controller.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.Settings;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;

/**
 * DialogFragment to show the user's credentials form on the settings
 *
 * Created by marcelo on 5/25/15.
 */
public class CredentialsFragment extends DialogFragment{
    private EditText eTUserName;
    private EditText eTPassword;
    private EditText eTRetypePassword;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.getDialog().setTitle(R.string.credentials);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.fragment_credentials, null);
        eTUserName = (EditText) v.findViewById(R.id.userNameCredentials);
        eTPassword = (EditText) v.findViewById(R.id.passwordCredentials);
        eTRetypePassword = (EditText) v.findViewById(R.id.retypePasswordCredentials);

        SettingsDAO settingsDAO = new SettingsDAO(getActivity());
        Settings settings = settingsDAO.getById(1);
        if (settings != null) {
            eTUserName.setText(settings.getUserName());
            eTPassword.setText(settings.getPassword());
        }
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setCancelable(true)
                .setPositiveButton(R.string.btnSave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (eTPassword.getText().equals(eTRetypePassword.getText())) {
                            SettingsFragment settingsFragment = (SettingsFragment) getActivity().getFragmentManager().findFragmentByTag("settings");
                            SharedPreferences sharedPreferences = settingsFragment.getPreferenceManager().getSharedPreferences();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("userName", String.valueOf(eTUserName.getText()));
                            editor.putString("password", String.valueOf(eTPassword.getText()));
                            editor.commit();
                        }
                    }
                })
                .setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
    }
}