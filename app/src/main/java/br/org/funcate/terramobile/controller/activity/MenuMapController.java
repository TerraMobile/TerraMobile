package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.configuration.ViewContextParameters;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.tilesource.MapTileGeoPackageProvider;

/**
 * Created by Andre Carvalho on 27/04/15.
 */
public class MenuMapController implements View.OnClickListener {

    private GpkgLayer child;
    private final Context context;

    public MenuMapController(Context context, GpkgLayer child) {
        this.child=child;
        this.context=context;
    }

    @Override
    public void onClick(View v) {
        storeSelectedItem(v);
        exec();
        return;
    }

    private void exec() {
        try{
            switch (child.getLayerType()){
                case TILES:{// base
                    loadTiles();
                    break;
                }
                case FEATURES:{// collect

                    break;
                }
                case EDITABLE:{// editable

                    break;
                }
                case ONLINE:{// online

                    break;
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Fail on read layer: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void storeSelectedItem(View v) {
        ViewContextParameters par = ((MainActivity) context).getParameters();
        try {
            if (v.isSelected()) {
                par.removeLayer(child);
                v.setSelected(false);
                v.setBackgroundColor(Color.BLACK);
                ((TextView) v).setTextColor(Color.WHITE);
            } else {
                par.addLayer(child);
                v.setSelected(true);
                v.setBackgroundColor(Color.WHITE);
                ((TextView) v).setTextColor(Color.BLACK);
            }

        }catch (Exception e){
            Toast.makeText(context, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void loadTiles() {
        if(child.getGeoPackage().isGPKGValid(true)) {
            createGeoPackageTileSourceOverlay();
        }else {
            Toast.makeText(context, "Invalid GeoPackage file.", Toast.LENGTH_SHORT).show();
        }
        return;
    }

    private void createGeoPackageTileSourceOverlay() {

        MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        mapView.setMaxZoomLevel(18);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);


        System.out.println("Overlay size:" + mapView.getOverlayManager().size());

/*        OnlineTileSourceBase mapQuestTileSource = TileSourceFactory.MAPQUESTOSM;
        String tileSourcePath = mapQuestTileSource.OSMDROID_PATH.getAbsolutePath() + "/";*/

        final MapTileProviderBasic tileProvider = new MapTileProviderBasic(context);

        final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", new String[] {"http://tile.openstreetmap.org/"});

        MapTileModuleProviderBase moduleProvider = new MapTileGeoPackageProvider(tileSource, child.getLayerName(), child.getGeoPackage());
        SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(context);
        MapTileProviderArray tileProviderArray = new MapTileProviderArray(tileSource, simpleReceiver, new MapTileModuleProviderBase[] { moduleProvider });

/*        tileProvider.setTileSource(tileSource);*/
        final TilesOverlay tilesOverlay = new TilesOverlay(tileProviderArray, context);
        tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        mapView.getOverlays().add(tilesOverlay);
        //mapView.getOverlayManager().overlaysReversed();
        //mapView.getTileProvider().clearTileCache();
        tileProvider.setTileRequestCompleteHandler(new SimpleInvalidationHandler(mapView));
        mapView.setTileSource(tileSource);
        mapView.setUseDataConnection(false); //  letting osmdroid know you would use it in offline mode, keeps the mapView from loading online tiles using network connection.*/
        mapView.invalidate();
    }
}
