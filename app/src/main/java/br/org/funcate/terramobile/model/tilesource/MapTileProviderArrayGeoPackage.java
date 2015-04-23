package br.org.funcate.terramobile.model.tilesource;

import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.views.MapView;

/**
 * Created by bogo on 09/04/15.
 */
public class MapTileProviderArrayGeoPackage extends MapTileProviderArray implements IMapTileProviderCallback {

    MapView mapView;

    public MapTileProviderArrayGeoPackage(final ITileSource pTileSource,
                                final IRegisterReceiver aRegisterReceiver,
                                final MapTileModuleProviderBase[] pTileProviderArray, final MapView mapView) {
        super(pTileSource, aRegisterReceiver, pTileProviderArray);
        this.mapView = mapView;
    }

    @Override
    public void mapTileRequestCompleted(final MapTileRequestState aState, final Drawable aDrawable) {
        super.mapTileRequestCompleted(aState, aDrawable);
        /*synchronized(mapView)
        {
            if(mapView!=null)
            {
                mapView.invalidate();
            }
        }*/
    }
}
