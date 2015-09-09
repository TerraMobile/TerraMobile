package br.org.funcate.terramobile.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import br.org.funcate.terramobile.R;

/**
 * Created by Andre Carvalho on 27/08/15.
 */
public class FeatureInfoPanelActivity extends FragmentActivity {

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feature_info_panel);
    }

}
