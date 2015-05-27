package br.org.funcate.terramobile.model.tilesource;

/**
 * Created by Andre Carvalho on 29/04/15.
 */
import android.content.Context;
import android.graphics.Color;

import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;

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

import java.io.File;
import java.util.ArrayList;

import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.gpkg.objects.AppLayer;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.util.ResourceUtil;

public class AppGeoPackageService {

    private AppGeoPackageService()
    {

    }

    private static String getGpkgFilePath(Context context) {
        File appPath = ResourceUtil.getDirectory(context.getResources().getString(R.string.app_workspace_dir));
        // ------------------------------------------------------------------------
        // TODO: alter temporary file name to dynamic file name as from user action when him acquire online GeoPackege from one server.
        //gpkgFilePath=this.context.getCurrentGeoPackageName();
        String gpkgFilePath=appPath+"/inpe_geoeye_2013_mosaico.gpkg";
        return gpkgFilePath;
        // ------------------------------------------------------------------------
    }

    /**
     * This method reads the layer names from GeoPackage.
     * @return ArrayList<GpkgLayer> listLayers, the list Layers
     * @throws Exception
     */
    public static ArrayList<GpkgLayer> getLayers(Context context) throws Exception {

        String gpkgFilePath = getGpkgFilePath(context);

        GeoPackage gpkg = GeoPackageService.readGPKG(context, gpkgFilePath);
        if(!gpkg.isGPKGValid(true))
        {
            throw new Exception("Invalid GeoPackage file.");
        }

        String[] columns = new String[2];
        columns[0]="table_name";
        columns[1]="data_type";

        ArrayList<ArrayList<GpkgField>> fields;
        fields = GeoPackageService.getGpkgFieldsContents(gpkg,columns);
        ArrayList<GpkgLayer> listLayers=new ArrayList<GpkgLayer>();
        GpkgLayer layer;

        for (int i = 0,size = fields.size(); i < size; i++) {

            ArrayList<GpkgField> aField = fields.get(i);
            layer=new GpkgLayer(gpkg);// set geoPackage reference in this layer

            for (int j = 0,len = aField.size(); j < len; j++) {

                GpkgField field = aField.get(j);

                if(field.getFieldName().equals(columns[0]))
                    layer.setLayerName((String) field.getValue());
                else {
                    if("features".equals(field.getValue())){
                        layer.setLayerType(AppLayer.FEATURES);
                    }else if("tiles".equals(field.getValue())){
                        layer.setLayerType(AppLayer.TILES);
                    }
                }
            }
            listLayers.add(layer);
        }
        gpkg.close();
        return listLayers;
    }

    public static void createGeoPackageTileSourceOverlay(GpkgLayer layer, Context context) {

        MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        mapView.setMaxZoomLevel(18);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);


        System.out.println("Overlay size:" + mapView.getOverlayManager().size());

/*        OnlineTileSourceBase mapQuestTileSource = TileSourceFactory.MAPQUESTOSM;
        String tileSourcePath = mapQuestTileSource.OSMDROID_PATH.getAbsolutePath() + "/";*/

        final MapTileProviderBasic tileProvider = new MapTileProviderBasic(context);

        final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", new String[] {"http://tile.openstreetmap.org/"});

        MapTileModuleProviderBase moduleProvider = new MapTileGeoPackageProvider(tileSource, layer.getLayerName(), layer.getGeoPackage());
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
/*
    public ArrayList<String> getAttributes() {

        return null;
    }*/

}
