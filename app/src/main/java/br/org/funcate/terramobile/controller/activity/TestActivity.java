/*
 * Copyright 2013, Augmented Technologies Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.org.funcate.terramobile.controller.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import com.augtech.geoapi.geopackage.GeoPackage;

import org.opengis.feature.simple.SimpleFeature;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.List;

import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.exception.FileException;
import br.org.funcate.terramobile.model.service.FileService;
import br.org.funcate.terramobile.test.JGPKGTestInterface;
//import com.augtech.geoapi.geopackage.GpkgTEST;
/** The main Activity for running test cases
 *
 * @author Augmented Technologies Ltd.
 *
 */
public class TestActivity extends Activity implements JGPKGTestInterface {

	static final String LOG_TAG = "GeoPackage Client";
	File appPath = getDirectory("GeoPackageTest");
	TextView statusText = null;
	TestActivity thisActivity = null;
    String tempURL = "http://200.144.100.34/temp/GPKG-TerraMobile-test.zip";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		thisActivity = this;
		setContentView(R.layout.activity_main);
		statusText = (TextView) findViewById(R.id.statusText);

        createBaseTileSource();

    }

    private void createBaseTileSource() {

        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setMaxZoomLevel(10);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setDrawingCacheEnabled(false);
        mapView.getController().setZoom(2);


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

 /*       MapView mapView = (MapView) findViewById(R.id.mapview);
*//*    mapView.getOverlayManager().get(0).onTouchEvent())*//*
*//*        OnlineTileSourceBase mapQuestTileSource = TileSourceFactory.MAPQUESTOSM;
        String tileSourcePath = mapQuestTileSource.OSMDROID_PATH.getAbsolutePath() + "/";*//*

        final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());

        final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 10, 256, ".png", new String[] {"http://tile.openstreetmap.org/"});

        MapTileModuleProviderBase moduleProvider = new MapTileGeoPackageProvider(tileSource);
        SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(getApplicationContext());
        MapTileProviderArrayGeoPackage tileProviderArray = new MapTileProviderArrayGeoPackage(tileSource, simpleReceiver, new MapTileModuleProviderBase[] { moduleProvider }, mapView);

*//*        tileProvider.setTileSource(tileSource);*//*
        final TilesOverlay tilesOverlay = new TilesOverlay(tileProviderArray, this.getApplicationContext());
        tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        mapView.getOverlays().add(tilesOverlay);
        //mapView.getOverlayManager().overlaysReversed();
        //mapView.getTileProvider().clearTileCache();
        tileProvider.setTileRequestCompleteHandler(new SimpleInvalidationHandler(mapView));
        mapView.setTileSource(tileSource);
        mapView.setUseDataConnection(false); //  letting osmdroid know you would use it in offline mode, keeps the mapView from loading online tiles using network connection.*//*
        mapView.invalidate();*/
    }

	private View.OnClickListener testCreateClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			GeoPackageService.createGPKG(thisActivity,appPath.getPath()+"/test.gpkg");

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
    private View.OnClickListener downloadFiles = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String fileName = appPath.getPath() +"/GPKG-TerraMobile-test.zip";

            try {
//                DownloadTask task= new DownloadTask(tempURL, fileName, true, (MainActivity));

//                boolean downloaded = task.execute().get();

//                if(!downloaded)
//                {
//                    statusText.setText(task.getException().getMessage());
//                    return;
//                }

                FileService.unzip(fileName, appPath.getPath()+"/");

//            } catch (InterruptedException e) {
//                statusText.setText(e.getMessage());
//            } catch (ExecutionException e) {
//                statusText.setText(e.getMessage());
            } catch (FileException e) {
                statusText.setText(e.getMessage());
            }




        }
    };
	@Override
	public void testComplete(String msg) {
		statusText.setText( msg );
	}
	
	private View.OnClickListener testReadClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			
			try {
 
				GeoPackage gpkg = GeoPackageService.readGPKG(thisActivity,appPath.getPath()+"/focosqueimadas.gpkg");

				List<SimpleFeature> features = GeoPackageService.getGeometries(gpkg, "focosqueimadas");
				
				statusText.setText(""+features.size()+" features on the file");
			
			} catch (Exception e) {
                e.printStackTrace();
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


            } catch (Exception e) {
                statusText.setText("Error reading gpkg file: " + e.getMessage());
                return;
            }

        }
    };
	
	
	@Override
	public void onBackPressed() {
		System.exit(0);
	}


	/** Do we have write access to the local SD card?
	 * 
	 * @return True if we can read from storage
	 */
	public static boolean isStorageAvailable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    } else {
	    	return false;
	    }
	}
	/** Check can read/write to SD card
	 * 
	 * @return True if we can
	 */
	public static boolean isStorageWriteable() {
	    String state = Environment.getExternalStorageState();
	    return Environment.MEDIA_MOUNTED.equals(state);
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
