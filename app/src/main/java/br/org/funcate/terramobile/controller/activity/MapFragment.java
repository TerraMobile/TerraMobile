// Created by plusminus on 00:23:14 - 03.10.2008
package br.org.funcate.terramobile.controller.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.augtech.geoapi.geopackage.GeoPackage;

import org.opengis.feature.simple.SimpleFeature;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.constants.OpenStreetMapConstants;
import br.org.funcate.terramobile.model.exception.FileException;
import br.org.funcate.terramobile.model.service.FileService;
import br.org.funcate.terramobile.model.task.DownloadTask;
import br.org.funcate.terramobile.model.tilesource.MapTileGeoPackageProvider;
import br.org.funcate.terramobile.view.ResourceProxyImpl;

/*import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.samplefragments.SampleFactory;*/

/**
 * Default map view activity.
 * 
 * @author Marc Kurtz
 * @author Manuel Stahl
 * 
 */
public class MapFragment extends Fragment implements OpenStreetMapConstants
{
    // ===========================================================
    // Constants
    // ===========================================================

    private static final int DIALOG_ABOUT_ID = 1;
    private static final int MENU_SAMPLES = Menu.FIRST + 1;
    private static final int MENU_ABOUT = MENU_SAMPLES + 1;
    private static final int MENU_LAST_ID = MENU_ABOUT + 1; // Always set to last unused id

    File appPath = getDirectory("GeoPackageTest");
    TextView statusText = null;
    Activity thisActivity = null;
    String tempURL = "http://200.144.100.34/temp/GPKG-TerraMobile-test.zip";

    // ===========================================================
    // Fields
    // ===========================================================

    private SharedPreferences mPrefs;
    private MapView mMapView;
    private ResourceProxy mResourceProxy;

    public MapFragment(){
        // Empty constructor required for fragment subclasses
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        thisActivity = this.getActivity();
        statusText = (TextView) thisActivity.findViewById(R.id.statusText);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());
        mMapView = new MapView(inflater.getContext(), 256, mResourceProxy);

        View rootView = inflater.inflate(R.layout.fragment_map, mMapView, false);
        //mMapView.setUseSafeCanvas(true);
        // Call this method to turn off hardware acceleration at the View level.
        // setHardwareAccelerationOff();
        return rootView;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setHardwareAccelerationOff()
    {
        // Turn off hardware acceleration here, or in manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mMapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        final Context context = this.getActivity();
		final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        // mResourceProxy = new ResourceProxyImpl(getActivity().getApplicationContext());

        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // only do static initialisation if needed
        if (CloudmadeUtil.getCloudmadeKey().length() == 0) {
            CloudmadeUtil.retrieveCloudmadeKey(context.getApplicationContext());
        }

/*        this.mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context),
                mMapView);*/
/*        this.mLocationOverlay = new MyLocationNewOverlay(context, new GpsMyLocationProvider(context),
                mMapView);*/

/*        mMinimapOverlay = new MinimapOverlay(getActivity(), mMapView.getTileRequestCompleteHandler());
		mMinimapOverlay.setWidth(dm.widthPixels / 5);
		mMinimapOverlay.setHeight(dm.heightPixels / 5);*/

/*		mScaleBarOverlay = new ScaleBarOverlay(context);
		mScaleBarOverlay.setCentred(true);
		mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);*/

/*        mRotationGestureOverlay = new RotationGestureOverlay(context, mMapView);
		mRotationGestureOverlay.setEnabled(false);*/

/*        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);*/
        /*mMapView.getOverlays().add(this.mLocationOverlay);*/
/*        mMapView.getOverlays().add(this.mCompassOverlay);*/
        /*mMapView.getOverlays().add(this.mMinimapOverlay);*/
		/*mMapView.getOverlays().add(this.mScaleBarOverlay);*/
        /*mMapView.getOverlays().add(this.mRotationGestureOverlay);*/

/*        mMapView.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 1));
        mMapView.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 23), mPrefs.getInt(PREFS_SCROLL_Y, 0));*/
        mMapView.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 10));
        mMapView.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 23), mPrefs.getInt(PREFS_SCROLL_Y, 10));

		/*mLocationOverlay.enableMyLocation();*/
		/*mCompassOverlay.enableCompass();*/

      //  setHasOptionsMenu(true);
    }

    @Override
    public void onPause()
    {
/*        final SharedPreferences.Editor edit = mPrefs.edit();
        edit.putString(PREFS_TILE_SOURCE, mMapView.getTileProvider().getTileSource().name());
        edit.putInt(PREFS_SCROLL_X, mMapView.getScrollX());
        edit.putInt(PREFS_SCROLL_Y, mMapView.getScrollY());
        edit.putInt(PREFS_ZOOM_LEVEL, mMapView.getZoomLevel());
        edit.commit();*/
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        final String tileSourceName = mPrefs.getString(PREFS_TILE_SOURCE,
                TileSourceFactory.DEFAULT_TILE_SOURCE.name());
        try {
            String[] url= {"http://tile.openstreetmap.org/"};
            final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", url);
            mMapView.setTileSource(tileSource);

        } catch (final IllegalArgumentException ignore) {
        }
    }

    /*********************************************************************************************
     * Metodos provenientes da activity principal, usada pelo bogo nos testes iniciais
     *********************************************************************************************/

    private void createBaseTileSource() {

    /*    MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setMaxZoomLevel(20);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        OnlineTileSourceBase mapQuestTileSource = TileSourceFactory.MAPQUESTOSM;
        String tileSourcePath = mapQuestTileSource.OSMDROID_PATH.getAbsolutePath() + "/";

        final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
        final ITileSource tileSource = new XYTileSource("MapquestOSM", ResourceProxy.string.mapnik, 1, 18, 256, ".png", new String[] { "http://tile.openstreetmap.org/" });

        tileProvider.setTileSource(tileSource);
        final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, this.getBaseContext());
        tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        mapView.getOverlays().add(tilesOverlay);

        tileProvider.setTileRequestCompleteHandler(new SimpleInvalidationHandler(mapView));

        mapView.setTileSource(tileSource);
        mapView.setUseDataConnection(false); //  letting osmdroid know you would use it in offline mode, keeps the mapView from loading online tiles using network connection.*/
    }

    private void createGeoPackageTileSourceOverlay()
    {
        MapView mapView = (MapView) thisActivity.findViewById(R.id.mapview);
        mapView.setMaxZoomLevel(20);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);


        System.out.println("Overlay size:" + mapView.getOverlayManager().size());

/*        OnlineTileSourceBase mapQuestTileSource = TileSourceFactory.MAPQUESTOSM;
        String tileSourcePath = mapQuestTileSource.OSMDROID_PATH.getAbsolutePath() + "/";*/

        final MapTileProviderBasic tileProvider = new MapTileProviderBasic(thisActivity.getApplicationContext());

        final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", new String[] {"http://tile.openstreetmap.org/"});

        MapTileModuleProviderBase moduleProvider = new MapTileGeoPackageProvider(tileSource);
        SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(thisActivity.getApplicationContext());
        MapTileProviderArray tileProviderArray = new MapTileProviderArray(tileSource, simpleReceiver, new MapTileModuleProviderBase[] { moduleProvider });

/*        tileProvider.setTileSource(tileSource);*/
        final TilesOverlay tilesOverlay = new TilesOverlay(tileProviderArray, thisActivity.getApplicationContext());
        tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        mapView.getOverlays().add(tilesOverlay);
        //mapView.getOverlayManager().overlaysReversed();
        //mapView.getTileProvider().clearTileCache();
        tileProvider.setTileRequestCompleteHandler(new SimpleInvalidationHandler(mapView));
        mapView.setTileSource(tileSource);
        mapView.setUseDataConnection(false); //  letting osmdroid know you would use it in offline mode, keeps the mapView from loading online tiles using network connection.*/
        mapView.invalidate();
    }

    private View.OnClickListener testCreateClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            GeoPackageService.createGPKG(thisActivity, appPath.getPath() + "/test.gpkg");

            statusText.setText("GeoPackage file successfully created");
        }
    };

    private View.OnClickListener testInsertClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try {
                //GeoPackageService.insertDataGPKG(thisActivity,"/GeoPackageTest/test.gpkg");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                statusText.setText("Error insert GML on device: " +e.getMessage());
                return;
            }


        }
    };
/*    private View.OnClickListener downloadFiles = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String fileName = appPath.getPath() +"/GPKG-TerraMobile-test.zip";

            try {
                DownloadTask task= new DownloadTask(tempURL, fileName, false);

                boolean downloaded = task.execute().get();

                if(!downloaded)
                {
                    statusText.setText(task.getException().getMessage());
                    return;
                }

                FileService.unzip(fileName, appPath.getPath() + "/");

            } catch (InterruptedException e) {
                statusText.setText(e.getMessage());
            } catch (ExecutionException e) {
                statusText.setText(e.getMessage());
            } catch (FileException e) {
                statusText.setText(e.getMessage());
            }




        }
    };*/
    
/*    @Override
    public void testComplete(String msg) {
        statusText.setText( msg );
    }*/

    private View.OnClickListener testReadClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            try {

                GeoPackage gpkg = GeoPackageService.readGPKG(thisActivity,appPath.getPath()+"/test.gpkg");


                List<SimpleFeature> features = GeoPackageService.getGeometries(gpkg, "municipios_2005");

                statusText.setText(""+features.size()+" features on the file");

            } catch (Exception e) {
                statusText.setText("Error reading gpkg file: " + e.getMessage());
                return;
            }

        }
    };

    private View.OnClickListener testReadTilesClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            try {

                String path = Environment.getExternalStorageDirectory().toString();

                GeoPackage gpkg = GeoPackageService.readGPKG(thisActivity,appPath.getPath()+"/landsat2009_tiles.gpkg");


                createGeoPackageTileSourceOverlay();

                //            List<SimpleFeature> features = GeoPackageService.getTiles(gpkg, "landsat2012_tiles");

/*
                byte[] b1 = GeoPackageService.getTile(gpkg, "landsat2012_tiles", 0,0,1 );

                byte[] b2 = GeoPackageService.getTile(gpkg, "landsat2012_tiles", 84,131,8 );

//                statusText.setText(""+features.size()+" features on the file");

                File file = new File(path+"/b1.png");
                FileOutputStream fos = new FileOutputStream(file);

                fos.write(b1);
                fos.flush();
                fos.close();

                file = new File(path+"/b2.png");
                fos = new FileOutputStream(file);

                fos.write(b2);
                fos.flush();
                fos.close();
*/



            } catch (Exception e) {
                statusText.setText("Error reading gpkg file: " + e.getMessage());
                return;
            }

        }
    };
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
