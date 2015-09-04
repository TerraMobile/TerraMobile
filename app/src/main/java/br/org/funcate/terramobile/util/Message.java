package br.org.funcate.terramobile.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MessageFragment;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;

/**
 * Created by marcelo on 5/29/15.
 */
public class Message {
    /**
     * Shows an error message dialog
     * @param activity Activity that the dialog will be shown
     * @param title The title of the dialog (MUST be from strings file (example: R.string.text))
     * @param message The error or success message (MUST be from strings file (example: R.string.text))
     */
    public static void showErrorMessage(Activity activity, int title, int message){
        MessageFragment messageFragment = new MessageFragment();
        messageFragment.setMessageDialogContent(R.drawable.error, title, message);
        messageFragment.show(activity.getFragmentManager(), "message");
    }

    /**
     * Shows an error message dialog
     * @param activity Activity that the dialog will be shown
     * @param title The title of the dialog (MUST be from strings file (example: R.string.text))
     * @param message The error or success message (MUST be from strings file (example: R.string.text))
     */
    public static void showErrorMessage(Activity activity, int title, String message){
        MessageFragment messageFragment = new MessageFragment();
        messageFragment.setMessageDialogContent(R.drawable.error, title, message);
        messageFragment.show(activity.getFragmentManager(), "message");
    }

    /**
     * Shows a success message dialog
     * @param activity Activity that the dialog will be shown
     * @param title The title of the dialog (MUST be from strings file (example: R.string.text))
     * @param message The error or success message (MUST be from strings file (example: R.string.text))
     */
    public static void showSuccessMessage(Activity activity, int title, int message){
        MessageFragment messageFragment = new MessageFragment();
        messageFragment.setMessageDialogContent(R.drawable.success, title, message);
        messageFragment.show(activity.getFragmentManager(), "message");
    }

    public static void showConfirmMessage(Activity activity, int title, int message, final CallbackConfirmMessage callback){

        // Default values
        String yes = "Yes";
        String no = "Yes";
        try{
            yes = ResourceHelper.getStringResource(R.string.yes);
            no = ResourceHelper.getStringResource(R.string.no);
        }catch (InvalidAppConfigException e){
            e.printStackTrace();
        }

        new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.confirmResponse(true);
                    }

                })
                .setNegativeButton(no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.confirmResponse(false);
                    }

                }).show();
    }

    /**
     * Start and show a ProgressDialog with a simple configuration to ProgressDialog widget.
     * Who call this method need call dismiss method of the ProgressDialog to destroy the ProgressDialog widget.
     * @param context, the application context
     * @param waitingMsg, the initial message
     * @return a reference to new instance of this widget.
     */
    public static ProgressDialog startProgressDialog(Context context, String waitingMsg) {
        final ProgressDialog progressDialog = new MarkerProgressView(context);
        progressDialog.setMessage(waitingMsg);
        progressDialog.show();
        return progressDialog;
    }

    private static class MarkerProgressView extends ProgressDialog {

        public MarkerProgressView(Context context) {
            super(context);
            this.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            this.setIndeterminate(true);
            this.setCanceledOnTouchOutside(false);
        }
    }
}