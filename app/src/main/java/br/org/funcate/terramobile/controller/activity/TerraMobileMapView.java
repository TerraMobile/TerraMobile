package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import com.augtech.geoapi.geometry.BoundingBoxImpl;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.util.ResourceHelper;

/**
 * Created by bogo on 05/11/15.
 */
public class TerraMobileMapView extends MapView {

    private MainController mainController;
    private boolean initialized=false;

    protected TerraMobileMapView(Context context, int tileSizePixels, ResourceProxy resourceProxy, MapTileProviderBase tileProvider, Handler tileRequestCompleteHandler, AttributeSet attrs) {
        super(context, tileSizePixels, resourceProxy, tileProvider, tileRequestCompleteHandler, attrs);
    }

    public TerraMobileMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TerraMobileMapView(Context context, int tileSizePixels) {
        super(context, tileSizePixels);
    }

    public TerraMobileMapView(Context context, int tileSizePixels, ResourceProxy resourceProxy) {
        super(context, tileSizePixels, resourceProxy);
    }

    public TerraMobileMapView(Context context, int tileSizePixels, ResourceProxy resourceProxy, MapTileProviderBase aTileProvider) {
        super(context, tileSizePixels, resourceProxy, aTileProvider);
    }

    public TerraMobileMapView(Context context, int tileSizePixels, ResourceProxy resourceProxy, MapTileProviderBase aTileProvider, Handler tileRequestCompleteHandler) {
        super(context, tileSizePixels, resourceProxy, aTileProvider, tileRequestCompleteHandler);
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
       /* try {
            configureMapView(this);
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
        }*/
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
