package br.org.funcate.terramobile.controller.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * DialogFragment to show the user's credentials form on the settings
 *
 * Created by marcelo on 5/25/15.
 */
public class MessageFragment extends DialogFragment{
    private int icon;
    private String message;
    private String title;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(this.title);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setView(getView())
                .setMessage(this.message)
                .setIcon(icon)
                .setCancelable(true).create();
    }

    public void setMessageDialogContent(int icon, String title, String message) {
        this.icon = icon;
        this.title = title;
        this.message = message;
    }
}