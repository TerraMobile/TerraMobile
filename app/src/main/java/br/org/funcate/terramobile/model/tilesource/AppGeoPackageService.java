package br.org.funcate.terramobile.model.tilesource;

/**
 * Created by Andre Carvalho on 29/04/15.
 */
import android.content.Context;
import android.graphics.Color;

import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;

import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.Envelope;
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

import java.io.File;
import java.util.ArrayList;

import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.exception.InvalidGeopackageException;
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

        String gpkgFilePath=appPath+"/rapideye-andadina.gpkg";

        return gpkgFilePath;
    }

    /**
     * This method reads the layers metadata from the GeoPackage contents and convert it to GpkgLayer format.
     * @return ArrayList<GpkgLayer> listLayers, the list Layers
     * @throws Exception
     */
    public static ArrayList<GpkgLayer> getLayers(Context context) throws InvalidGeopackageException, QueryException {

        String gpkgFilePath = getGpkgFilePath(context);

        GeoPackage gpkg = GeoPackageService.readGPKG(context, gpkgFilePath);
        if(!gpkg.isGPKGValid(true))
        {
            throw new InvalidGeopackageException("Invalid GeoPackage file.");
        }

        ArrayList<ArrayList<GpkgField>> fields;
        fields = GeoPackageService.getGpkgFieldsContents(gpkg, null, "");
        ArrayList<GpkgLayer> listLayers=new ArrayList<GpkgLayer>();
        GpkgLayer layer;

        for (int i = 0,size = fields.size(); i < size; i++) {

            ArrayList<GpkgField> aField = fields.get(i);
            layer=new GpkgLayer(gpkg);// set geoPackage reference in this layer

            if(aField.size()!=10)
            {
                throw new InvalidGeopackageException("Invalid number of field on GPKG content table. ");
            }

            GpkgField tableNameField = aField.get(0);
            GpkgField dataTypeField = aField.get(1);

            GpkgField minXField = aField.get(5);
            GpkgField minYField = aField.get(6);
            GpkgField maxXField = aField.get(7);
            GpkgField maxYField = aField.get(8);

            GpkgField srsIdField = aField.get(9);


            // Getting layer name
            layer.setName((String) tableNameField.getValue());

            // Getting data type
            if("features".equals(dataTypeField.getValue()))
            {
                layer.setType(GpkgLayer.Type.FEATURES);
            } else if("tiles".equals(dataTypeField.getValue()))
            {
                layer.setType(GpkgLayer.Type.TILES);
            } else
            {
                //TODO:Verify if it's necessary to stop the process or ignore the current layer iteration
                throw new InvalidGeopackageException("Invalid layer .");
            }

            // Getting bounding box

            Double minX=(Double) minXField.getValue();
            Double minY=(Double) minYField.getValue();
            Double maxX=(Double) maxXField.getValue();
            Double maxY=(Double) maxYField.getValue();

            BoundingBoxE6 bb =new BoundingBoxE6(minX,minY, maxX, maxY);

            layer.setBox(bb);

            // Getting srs ID

            layer.setSrsId((Integer) srsIdField.getValue());

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

        MapTileModuleProviderBase moduleProvider = new MapTileGeoPackageProvider(tileSource, layer.getName(), layer.getGeoPackage());
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
