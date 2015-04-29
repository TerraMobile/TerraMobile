package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.augtech.geoapi.geopackage.GeoPackage;

import org.opengis.feature.simple.SimpleFeature;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.configuration.ViewContextParameters;
import br.org.funcate.terramobile.model.exception.FileException;
import br.org.funcate.terramobile.model.service.FileService;
import br.org.funcate.terramobile.model.task.DownloadTask;
import br.org.funcate.terramobile.model.tilesource.MapTileGeoPackageProvider;

/**
 * Created by Andre Carvalho on 27/04/15.
 */
public class MenuToolController implements View.OnClickListener {

    private int childIdMenuPosition;
    private final Context context;
    private File appPath;
    private String tempURL;

    public MenuToolController(Context context, int childPosition) {
        this.childIdMenuPosition=childPosition;
        this.context=context;
        initResources();
    }

    public MenuToolController(Context context) {
        this.childIdMenuPosition=-1;
        this.context=context;
        initResources();
    }

    private void initResources() {
        appPath = getDirectory(context.getResources().getString(R.string.app_workspace_dir));
        tempURL = context.getResources().getString(R.string.gpkg_url);
    }
    @Override
    public void onClick(View v) {

        storeSelectedItem(v);
        exec();
        Toast.makeText(context, "Tool : " + v.getTag(),
                Toast.LENGTH_SHORT).show();
        return;
    }


    private void exec() {
        switch (this.childIdMenuPosition) {
            case 0: {//Download GeoPackage
                downloadGeoPackage();
                break;
            }
            case 1:{//Create GeoPackage
                createGeoPackage();
                break;
            }
            case 2:{//Read Geometries
                readGeometries();
                break;
            }
            case 3:{//Read Tiles
                readTiles();
                break;
            }
            case 4:{// Insert Data
                insertData();
                break;
            }
        }
    }

    public void readGeometries() {
        try {
            GeoPackage gpkg = GeoPackageService.readGPKG(context,appPath.getPath()+"/test.gpkg");

            List<SimpleFeature> features = GeoPackageService.getGeometries(gpkg, "municipios_2005");

            Toast.makeText(context, ""+features.size()+" features on the file", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(context, "Error reading gpkg file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return;
    }

    public void createGeoPackage() {
        GeoPackageService.createGPKG(context, appPath.getPath() + "/test.gpkg");
        Toast.makeText(context, "GeoPackage file successfully created", Toast.LENGTH_SHORT).show();
    }


    public void readTiles() {
        try {
            String path = appPath.getPath();

            GeoPackage geoPackage = GeoPackageService.readGPKG(context, path+"/landsat2009_tiles.gpkg");
            if(geoPackage.isGPKGValid(true)) {
                createGeoPackageTileSourceOverlay();
            }else {
                Toast.makeText(context, "Invalid GeoPackage file.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(context, "Error reading gpkg file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return;
    }

    public void insertData() {
        try {
            //GeoPackageService.insertDataGPKG(thisActivity,"/GeoPackageTest/test.gpkg");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(context, "Error insert GML on device: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return;
    }

    public void downloadGeoPackage() {
        String destinationFilePath = appPath.getPath() + "/" + context.getResources().getString(R.string.destination_file_path);

        try {
            DownloadTask task = new DownloadTask(tempURL, destinationFilePath, true);

            boolean downloaded = task.execute().get();

            if (!downloaded) {
                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            FileService.unzip(destinationFilePath, appPath.getPath() + "/");

        } catch (InterruptedException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (ExecutionException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (FileException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createGeoPackageTileSourceOverlay() {

        MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        mapView.setMaxZoomLevel(20);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);


        System.out.println("Overlay size:" + mapView.getOverlayManager().size());

/*        OnlineTileSourceBase mapQuestTileSource = TileSourceFactory.MAPQUESTOSM;
        String tileSourcePath = mapQuestTileSource.OSMDROID_PATH.getAbsolutePath() + "/";*/

        final MapTileProviderBasic tileProvider = new MapTileProviderBasic(context);

        final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", new String[] {"http://tile.openstreetmap.org/"});

        MapTileModuleProviderBase moduleProvider = new MapTileGeoPackageProvider(tileSource);
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

    private void storeSelectedItem(View v) {
        ViewContextParameters par = ((MainActivity) context).getParameters();
        TextView text = null;
        text = (TextView) v;

        try {
            if (v.isSelected()) {
                par.removeLayerName((String) text.getTag());
                text.setSelected(false);
                text.setBackgroundColor(Color.BLACK);
                text.setTextColor(Color.WHITE);
            } else {
                par.addLayerName((String) text.getTag());
                text.setSelected(true);
                text.setBackgroundColor(Color.WHITE);
                text.setTextColor(Color.BLACK);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Get a directory on extenal storage (SD card etc), ensuring it exists
     *
     * @return a new File representing the chosen directory
     */
    public static File getDirectory(String directory) {
        if (directory==null) return null;
        String path = Environment.getExternalStorageDirectory().toString();
        path += directory.startsWith("/") ? "" : "/";
        path += directory.endsWith("/") ? directory : directory + "/";
        File file = new File(path);
        file.mkdirs();
        return file;
    }
}
