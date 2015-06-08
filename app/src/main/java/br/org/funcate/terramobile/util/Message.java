package br.org.funcate.terramobile.util;

import android.app.Activity;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MessageFragment;

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
}