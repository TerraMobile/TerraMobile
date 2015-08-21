package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import org.opengis.geometry.BoundingBox;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.LowMemoryException;
import br.org.funcate.terramobile.model.exception.StyleException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.geomsource.SFSLayer;
import br.org.funcate.terramobile.model.geomsource.overlay.SFSLayerOverlay;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.service.LayersService;
import br.org.funcate.terramobile.model.service.StyleService;
import br.org.funcate.terramobile.model.service.AppGeoPackageService;
import br.org.funcate.terramobile.model.tilesource.MapTileGeoPackageProvider;
import br.org.funcate.terramobile.model.tilesource.MapTileProviderArrayGeoPackage;
import br.org.funcate.terramobile.util.GeoUtil;

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

    private void addBaseLayer(GpkgLayer child) {



        if(child.getGeoPackage().isGPKGValid(false)) {
            if(child.getOsmOverLayer()==null)
            {
                MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);




                final MapTileProviderBasic tileProvider = new MapTileProviderBasic(context);

                final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", new String[] {"http://tile.openstreetmap.org/"});
                MapTileModuleProviderBase moduleProvider = new MapTileGeoPackageProvider(tileSource, child.getName(), child.getGeoPackage());
                SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(context);

                MapTileProviderArray tileProviderArray = new MapTileProviderArrayGeoPackage(tileSource, simpleReceiver, new MapTileModuleProviderBase[] { moduleProvider }, ((MainActivity) this.context).getMainController().getMapFragment());

                final TilesOverlay tilesOverlay = new TilesOverlay(tileProviderArray, context);
                tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                mapView.getOverlays().add(INDEX_BASE_LAYER,tilesOverlay);
                child.setOsmOverLayer(tilesOverlay);

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

    private void removeBaseLayer(GpkgLayer layer) {

        if(layer!=null)
        {
            MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
            mapView.getOverlays().remove(layer.getOsmOverLayer());
            layer.setOsmOverLayer(null);
            currentBaseLayer=null;
        }

        return;
    }

    public void addLayer(GpkgLayer layer) throws TerraMobileException, StyleException, InvalidAppConfigException, LowMemoryException {

        if(layer.getType()==GpkgLayer.Type.FEATURES
                || layer.getType()==GpkgLayer.Type.EDITABLE)
        {

            addVectorLayer(layer);;

        } else
          if(layer.getType()==GpkgLayer.Type.TILES)
          {

                addBaseLayer(layer);

          }
    }

    public void removeLayer(GpkgLayer layer) {

        if(layer.getType()==GpkgLayer.Type.FEATURES
                || layer.getType()==GpkgLayer.Type.EDITABLE)
        {

            removeVectorLayer(layer);;

        } else
        if(layer.getType()==GpkgLayer.Type.TILES)
        {

            removeBaseLayer(layer);

        }
    }


    public GpkgLayer getBaseLayer() {
        return currentBaseLayer;
    }

    private void addVectorLayer(GpkgLayer child) throws LowMemoryException, InvalidAppConfigException, TerraMobileException, StyleException {

        if(child.getOsmOverLayer()==null) {


            MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);

            Style defaultStyle = StyleService.loadStyle(context, child.getGeoPackage().getDatabaseFileName(),child);

            if(!((MainActivity) context).useNewOverlaySFS)
            {
                SFSLayer l = AppGeoPackageService.getFeatures(child);

                KmlDocument kmlDocument = new KmlDocument();

                Overlay overlay = l.buildOverlay(mapView, defaultStyle, null, kmlDocument);

                mapView.getOverlays().add(overlay);

                child.setOsmOverLayer(overlay);
            }
            else
            {
                SFSLayerOverlay overlay = new SFSLayerOverlay(child, this.context);

                overlay.setStyle(defaultStyle);

                mapView.getOverlays().add(overlay);

                child.setOsmOverLayer(overlay);
            }
            mapView.invalidate();
        }


    }

    private void removeVectorLayer(GpkgLayer child) {

        if(child.getOsmOverLayer()!=null)
        {
            MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
            mapView.getOverlays().remove(child.getOsmOverLayer());
            child.setOsmOverLayer(null);
            mapView.invalidate();
        }
        return;
    }

    /**
     * Allows to pan the mapView to the requested BoundingBox and calculating the extent required zoom level to fit on canvas
     * @param bb Requested BoundingBox to pan
     */
    public void panTo(BoundingBox bb)
    {
        MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        BoundingBoxE6 bbe6 = GeoUtil.convertToBoundingBoxE6(bb);
        mapView.zoomToBoundingBox(bbe6);
    }

    public void removeAllLayers(boolean updateMap)
    {
        MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        mapView.getOverlays().clear();
        if(updateMap)
        {
            mapView.invalidate();
        }

    }
    public void updateOverlaysOrder(ArrayList<GpkgLayer> orderedLayers)
    {
        MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        LayersService.sortOverlayByGPKGLayer(mapView.getOverlays(), orderedLayers);
        mapView.invalidate();
    }

}
