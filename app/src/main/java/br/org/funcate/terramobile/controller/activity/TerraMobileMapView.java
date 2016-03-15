package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import br.org.funcate.terramobile.model.tilesource.TerraMobileInvalidationHandler;

/**
 * Created by bogo on 05/11/15.
 */
public class TerraMobileMapView extends MapView implements MapEventsReceiver {

    private MainController mainController;
    private boolean initialized=false;
    private Context context;
    public TerraMobileMapView(Context context, AttributeSet attrs) {
        super(context, 256, new DefaultResourceProxyImpl(context), null, new TerraMobileInvalidationHandler(null), attrs);
        this.context = context;
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(context, this);
        this.getOverlays().add(0, mapEventsOverlay);
        //super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if(mainController!=null
                &&!initialized)
        {
            mainController.onMapViewInitialized();
            initialized = true;
        }
        ((TerraMobileInvalidationHandler)getTileRequestCompleteHandler()).setMapView(this);
       /* try {
            configureMapView(this);
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
        }*/
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        Toast.makeText(this.context, "Tap on (" + geoPoint.getLatitude() + "," + geoPoint.getLongitude() + ")", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        return false;
    }
}
