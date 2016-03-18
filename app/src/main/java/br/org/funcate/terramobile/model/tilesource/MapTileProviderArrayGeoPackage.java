package br.org.funcate.terramobile.model.tilesource;

import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import br.org.funcate.terramobile.controller.activity.MapFragment;

/**
 * Created by bogo on 09/04/15.
 */
public class MapTileProviderArrayGeoPackage extends MapTileProviderArray implements IMapTileProviderCallback {

    MapFragment mapFragment;

    public MapTileProviderArrayGeoPackage(final ITileSource pTileSource,
                                final IRegisterReceiver aRegisterReceiver,
                                final MapTileModuleProviderBase[] pTileProviderArray, final MapFragment mapFragment) {
        super(pTileSource, aRegisterReceiver, pTileProviderArray);
        this.mapFragment = mapFragment;
    }

    @Override
    public void mapTileRequestCompleted(final MapTileRequestState aState, final Drawable aDrawable) {
        super.mapTileRequestCompleted(aState, aDrawable);
        /*synchronized(mapView)
        {

        }*/

        if(mapFragment!=null)
        {
            mapFragment.updateMap();
        }
    }
}
