package br.org.funcate.terramobile.model.tilesource;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import org.osmdroid.tileprovider.MapTile;

public class TerraMobileInvalidationHandler extends Handler {

    private View mView;

    public TerraMobileInvalidationHandler(final View pView) {
        super();
        mView = pView;
    }

    @Override
    public void handleMessage(final Message msg) {
        switch (msg.what) {
            case MapTile.MAPTILE_SUCCESS_ID:
                if(mView!=null)
                {
                    mView.invalidate();
                }
                break;
        }
    }

    public void setMapView(View mView) {
        this.mView = mView;
    }
}
