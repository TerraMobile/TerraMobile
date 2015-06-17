package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import com.augtech.geoapi.geometry.BoundingBoxImpl;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.List;

import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.tilesource.MapTileGeoPackageProvider;

/**
 * Created by Andre Carvalho on 27/04/15.
 */
public class MenuMapController {

    private final Context context;
    private final int INDEX_BASE_LAYER=0;
    private int lastIndexDrawOrder;

    public MenuMapController(Context context) {
        this.context=context;
        this.lastIndexDrawOrder = 1;
    }

    public void addBaseLayer(GpkgLayer child) {

        if(child.getGeoPackage().isGPKGValid(false)) {

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

            MapTileProviderArray tileProviderArray = new MapTileProviderArray(tileSource, simpleReceiver, new MapTileModuleProviderBase[] { moduleProvider });

/*        tileProvider.setTileSource(tileSource);*/
            final TilesOverlay tilesOverlay = new TilesOverlay(tileProviderArray, context);
            tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
            mapView.getOverlays().add(INDEX_BASE_LAYER,tilesOverlay);
            this.lastIndexDrawOrder++;
            child.setIndexOverlay(this.lastIndexDrawOrder);
            //mapView.getOverlayManager().overlaysReversed();
            //mapView.getTileProvider().clearTileCache();
            tileProvider.setTileRequestCompleteHandler(new SimpleInvalidationHandler(mapView));
            mapView.setTileSource(tileSource);
            mapView.setUseDataConnection(false); //  letting osmdroid know you would use it in offline mode, keeps the mapView from loading online tiles using network connection.*/
            mapView.invalidate();
        }else {
            Toast.makeText(context, "Invalid GeoPackage file.", Toast.LENGTH_SHORT).show();
        }
        return;
    }

    public void removeBaseLayer() {
        MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        mapView.getOverlays().remove(INDEX_BASE_LAYER);
        return;
    }

    public void addVectorLayer(GpkgLayer child) {

        MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        BoundingBoxE6 bbox = mapView.getBoundingBox();
        BoundingBox bbox1 = new BoundingBoxImpl(bbox.getLatNorthE6(),bbox.getLonEastE6(),bbox.getLatSouthE6(),bbox.getLonWestE6());

        List<SimpleFeature> features = null;
        try {
            features = GeoPackageService.getGeometries(child.getGeoPackage(), child.getName(), bbox1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);

        GeometrySource geometrySource = new GeometrySource(child);// features = GeoPackageService.getGeometries(child.getGeoPackage(), child.getName());

        MapGeometryGeoPackageProvider geometryProvider = new MapGeometryGeoPackageProvider(geometrySource, child.getName(), child.getGeoPackage());
        // org.osmdroid.bonuspack.overlay.GpkgGeometryOverlay
        GpkgGeometryOverlay gpkgGeometryOverlay = new GpkgGeometryOverlay(geometryProvider, context);

        mapView.getOverlays().add(this.lastIndexDrawOrder, gpkgGeometryOverlay);*/

        child.setIndexOverlay(this.lastIndexDrawOrder);
        this.lastIndexDrawOrder++;
    }

    public void removeVectorLayer(GpkgLayer child) {
        int location = child.getIndexOverlay();
        this.lastIndexDrawOrder--;
        MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        mapView.getOverlays().remove(location);
        return;
    }

    public void addEditableLayer(GpkgLayer child) {
        return;
    }

    public void removeEditableLayer(GpkgLayer child) {
        return;
    }
}
