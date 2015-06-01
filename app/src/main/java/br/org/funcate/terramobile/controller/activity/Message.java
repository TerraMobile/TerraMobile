package br.org.funcate.terramobile.controller.activity;

import android.app.Activity;

/**
 * Created by marcelo on 5/29/15.
 */
public class Message {
    /**
     * Shows a message dialog
     * @param activity Activity that the dialog will be shown
     * @param icon An icon representing an error ou success (R.drawable.error or R.drawable.success)
     * @param title The title of the dialog
     * @param message The error or success message
     */
    public static void showMessage(Activity activity, int icon, String title, String message){
        MessageFragment messageFragment = new MessageFragment();
        messageFragment.setMessageDialogContent(icon, title, message);
        messageFragment.show(activity.getFragmentManager(), "message");
    }
}
