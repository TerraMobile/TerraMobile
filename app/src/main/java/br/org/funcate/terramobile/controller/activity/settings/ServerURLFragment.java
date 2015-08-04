package br.org.funcate.terramobile.controller.activity.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;

/**
 * DialogFragment to show the user's credentials form on the settings menu
 *
 * Created by marcelo on 5/25/15.
 */
public class ServerURLFragment extends DialogFragment{
    private EditText eTServerURL;
    private ServerURLController urlController;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.fragment_server_url, null);
        urlController=new ServerURLController(this);


        eTServerURL = (EditText) v.findViewById(R.id.serverURL);

        Button btnSave = (Button) v.findViewById(R.id.btnSave);
        Button btnCancel = (Button) v.findViewById(R.id.btnCancel);

        String serverUrl = ((SettingsActivity)this.getActivity()).getController().getServerURL();
        if (serverUrl != null)
            eTServerURL.setText(serverUrl);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    urlController.save(eTServerURL.getText().toString());
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
        if(url.endsWith("/"))
            url.substring(0, url.length() - 1);
        eTServerURL.setText(url.trim());
        if(Patterns.WEB_URL.matcher(eTServerURL.getText().toString().trim()).matches())
            return true;
        else {
            eTServerURL.setError("Invalid URL");
            return false;
        }
    }
}