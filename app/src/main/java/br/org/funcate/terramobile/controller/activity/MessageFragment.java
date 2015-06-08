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
    private int message;
    private String str_message;
    private int title;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (this.str_message != null ) {
            CharSequence msg = this.str_message.subSequence(0, this.str_message.length());
            return new AlertDialog.Builder(getActivity())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    })
                    .setView(getView())
                    .setTitle(this.title)
                    .setMessage(msg)
                    .setIcon(this.icon)
                    .create();
        }else{
            return new AlertDialog.Builder(getActivity())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    })
                    .setView(getView())
                    .setTitle(this.title)
                    .setMessage(this.message)
                    .setIcon(this.icon)
                    .create();
        }
    }

    public void setMessageDialogContent(int icon, int title, int message) {
        this.icon = icon;
        this.title = title;
        this.message = message;
    }

    public void setMessageDialogContent(int icon, int title, String message) {
        this.icon = icon;
        this.title = title;
        this.str_message = message;
    }
}