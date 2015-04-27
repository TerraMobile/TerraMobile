package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.view.View;

/**
 * Created by Andre Carvalho on 27/04/15.
 */
public class MenuMapController implements View.OnClickListener {

    private int childIdMenuPosition;
    private final Context context;

    public MenuMapController(Context context, int childPosition) {
        this.childIdMenuPosition=childPosition;
        this.context=context;
    }

    @Override
    public void onClick(View v) {
        return;
    }
}
