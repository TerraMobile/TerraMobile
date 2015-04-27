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
import android.util.TypedValue;
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
import org.osmdroid.util.GeoPoint;
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
import br.org.funcate.terramobile.util.ResourceUtil;
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

    private static final int MENU_SAMPLES = Menu.FIRST + 1;
    private static final int MENU_ABOUT = MENU_SAMPLES + 1;

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
/*
        mMapView.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 10));
        mMapView.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 23), mPrefs.getInt(PREFS_SCROLL_Y, 10));
*/

		/*mLocationOverlay.enableMyLocation();*/
		/*mCompassOverlay.enableCompass();*/

      //  setHasOptionsMenu(true);
        configureMapView(mMapView);
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

    public void configureMapView(MapView mapView)
    {

        double x = ResourceUtil.getDoubleResource(getResources(), R.dimen.default_map_center_x);
        double y = ResourceUtil.getDoubleResource(getResources(), R.dimen.default_map_center_y);

        GeoPoint gPt = new GeoPoint(x,y);

        mapView.getController().setCenter(gPt);

        int maxZoomLevel = ResourceUtil.getIntResource(getResources(), R.integer.default_max_zoom_level);

        mapView.setMaxZoomLevel(maxZoomLevel);

        boolean builtInZoomControls=ResourceUtil.getBooleanResource(getResources(), R.bool.default_built_in_zoom_controls);

        mapView.setBuiltInZoomControls(builtInZoomControls);

        boolean multiTouchControls=ResourceUtil.getBooleanResource(getResources(), R.bool.default_multi_touch_controls);

        mapView.setMultiTouchControls(multiTouchControls);

        int initialZoomLevel = ResourceUtil.getIntResource(getResources(), R.integer.default_initial_zoom_level);

        mapView.getController().setZoom(initialZoomLevel);

    }

    public synchronized void updateMap()
    {
        this.mMapView.invalidate();
    }

}
