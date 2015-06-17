package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.HashMap;

import br.org.funcate.dynamicforms.util.PositionUtilities;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.configuration.ViewContextParameters;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.geomsource.SFSLayer;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.tilesource.AppGeoPackageService;
import br.org.funcate.terramobile.model.tilesource.MapTileGeoPackageProvider;
import br.org.funcate.terramobile.model.tilesource.MapTileProviderArrayGeoPackage;
import br.org.funcate.terramobile.util.Util;

/**
 * Created by Andre Carvalho on 27/04/15.
 */
public class MenuMapController {

    private final Context context;
    private final int INDEX_BASE_LAYER=0;
    private int lastIndexDrawOrder;
    private GpkgLayer currentBaseLayer;

    public MenuMapController(Context context) {
        this.context=context;
        this.lastIndexDrawOrder = 0;
        this.currentBaseLayer = null;
    }

    public void addBaseLayer(GpkgLayer child) {

        if(child.getGeoPackage().isGPKGValid(false)) {
            if(child.getOsmOverLayer()==null)
            {
                MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
                mapView.setMaxZoomLevel(18);
                mapView.setBuiltInZoomControls(true);
                mapView.setMultiTouchControls(true);

                System.out.println("Overlay size:" + mapView.getOverlayManager().size());

/*        OnlineTileSourceBase mapQuestTileSource = TileSourceFactory.MAPQUESTOSM;
        String tileSourcePath = mapQuestTileSource.OSMDROID_PATH.getAbsolutePath() + "/";*/

                final MapTileProviderBasic tileProvider = new MapTileProviderBasic(context);

                final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", new String[] {"http://tile.openstreetmap.org/"});
                MapTileModuleProviderBase moduleProvider = new MapTileGeoPackageProvider(tileSource, child.getName(), child.getGeoPackage());
                SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(context);

                //MapTileProviderArray tileProviderArray = new MapTileProviderArray(tileSource, simpleReceiver, new MapTileModuleProviderBase[] { moduleProvider });
                MapTileProviderArray tileProviderArray = new MapTileProviderArrayGeoPackage(tileSource, simpleReceiver, new MapTileModuleProviderBase[] { moduleProvider }, ((MainActivity) this.context).getMapFragment());
/*        tileProvider.setTileSource(tileSource);*/
                final TilesOverlay tilesOverlay = new TilesOverlay(tileProviderArray, context);
                tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                mapView.getOverlays().add(INDEX_BASE_LAYER,tilesOverlay);
                child.setOsmOverLayer(tilesOverlay);
/*
            this.lastIndexDrawOrder++;
            child.setIndexOverlay(this.lastIndexDrawOrder);
*/
                //mapView.getOverlayManager().overlaysReversed();
                //mapView.getTileProvider().clearTileCache();
                tileProvider.setTileRequestCompleteHandler(new SimpleInvalidationHandler(mapView));
                mapView.setTileSource(tileSource);
                mapView.setUseDataConnection(false); //  letting osmdroid know you would use it in offline mode, keeps the mapView from loading online tiles using network connection.*/
                mapView.invalidate();
                currentBaseLayer=child;
            }
        }else {
            Toast.makeText(context, "Invalid GeoPackage file.", Toast.LENGTH_SHORT).show();
        }
        return;
    }

    public void removeBaseLayer() {

        if(currentBaseLayer!=null)
        {
            MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
            mapView.getOverlays().remove(currentBaseLayer.getOsmOverLayer());
            currentBaseLayer.setOsmOverLayer(null);
            currentBaseLayer=null;
        }

        return;
    }

    public GpkgLayer getBaseLayer() {
        return currentBaseLayer;
    }

    public void addVectorLayer(GpkgLayer child) {

        if(child.getOsmOverLayer()==null) {
            SFSLayer l = AppGeoPackageService.getFeatures(child);

            MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
            HashMap<String, Integer> colorMap = Util.getRandomColor();

            int contourColor = Color.rgb(colorMap.get("r"), colorMap.get("g"), colorMap.get("b"));
            int fillColor = Color.argb(80, colorMap.get("r"), colorMap.get("g"), colorMap.get("b"));
            Style defaultStyle = new Style(null, contourColor, 2.0f, fillColor);

            KmlDocument kmlDocument = new KmlDocument();
            Overlay overlay = l.buildOverlay(mapView, defaultStyle, null, kmlDocument);

            mapView.getOverlays().add(overlay);
            child.setOsmOverLayer(overlay);

            mapView.invalidate();
        }


    }

    public void removeVectorLayer(GpkgLayer child) {

        if(child.getOsmOverLayer()!=null)
        {
            MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
            mapView.getOverlays().remove(child.getOsmOverLayer());
            child.setOsmOverLayer(null);
            mapView.invalidate();
        }
        return;
    }

    public void addEditableLayer(GpkgLayer child) {
        return;
    }

    public void removeEditableLayer(GpkgLayer child) {
        return;
    }
}
