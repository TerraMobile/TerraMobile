package br.org.funcate.terramobile.controller.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.settings.GPSSettingController;
import br.org.funcate.terramobile.model.constants.OpenStreetMapConstants;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.service.AppGeoPackageService;
import br.org.funcate.terramobile.model.service.GPSService;
import br.org.funcate.terramobile.model.service.AppGeoPackageService;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceHelper;

/*import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.samplefragments.SampleFactory;*/

/**
 * Default map view fragment.
 *
 * @author Marc Kurtz
 * @author Manuel Stahl
 *
 */
public class MapFragment extends Fragment implements OpenStreetMapConstants{

    // ===========================================================
    // Fields
    // ===========================================================

    private SharedPreferences mPrefs;
    private MapView mMapView;
    private ImageView drawingImageView;

    private ImageButton gpsLocation;
    private ImageButton zoomIn;
    private ImageButton zoomOut;
    private MenuMapController menuMapController;

    private Context context;

    public void setMenuMapController(MenuMapController menuMapController)
    {
        this.menuMapController = menuMapController;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) v.findViewById(R.id.mapview);

        drawingImageView = (ImageView) v.findViewById(R.id.DrawingImageView);
        try {
            drawCross(drawingImageView);
            configureMapView(mMapView);
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage((MainActivity)context, R.string.failure_title_msg, e.getMessage());
        }

        gpsLocation = (ImageButton) v.findViewById(R.id.Gps);
        gpsLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centerMapToGPS();
            }
        });


        zoomIn = (ImageButton) v.findViewById(R.id.ZoomIn);
        zoomIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ZoomIn();
            }
        });

        zoomOut = (ImageButton) v.findViewById(R.id.ZoomOut);
        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZoomOut();
            }
        });

        super.onCreate(savedInstanceState);

        mapLoaded();

        return v;
    }

    private void mapLoaded()
    {

        try {
            menuMapController.getMainController().loadCurrentProject();
        } catch (InvalidAppConfigException e) {
            Message.showErrorMessage((MainActivity)context, R.string.error, e.getMessage());
        } catch (DAOException e) {
            Message.showErrorMessage((MainActivity)context, R.string.error, e.getMessage());
        }
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

        context = this.getActivity();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        // mResourceProxy = new ResourceProxyImpl(getActivity().getApplicationContext());

        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // only do static initialisation if needed
        if (CloudmadeUtil.getCloudmadeKey().length() == 0) {
            CloudmadeUtil.retrieveCloudmadeKey(context.getApplicationContext());
        }
    }

    @Override
    public void onPause()
    {
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
            applyGPSSetting();
        } catch (final IllegalArgumentException ignore) {
        }
    }

    /**
     * Reads the GPS configuration and inserts GPS overlay on map, if it is enabled.
     */
    public void applyGPSSetting() {
        // verify if GPS enable on device
        if(GPSService.isGPSProviderEnable(context) || GPSService.isNetworkProviderEnable(context)) {

            GPSSettingController gpsSettingController = new GPSSettingController(mMapView.getContext());
            Boolean showGPSLocation = gpsSettingController.getGPSLocationState();
            Boolean showGPSLocationOnCenter = gpsSettingController.getGPSCenterState();
            if (showGPSLocation != null)
                if (showGPSLocation) {
                    MainActivity mainActivity = (MainActivity) context;
                    GPSOverlayController gpsOverlayController = mainActivity.getMainController().getGpsOverlayController();
                    gpsOverlayController.addGPSTrackerLayer();
                    gpsOverlayController.setKeepOnCenter( ((showGPSLocationOnCenter != null)?(showGPSLocationOnCenter):(false)) );
                }
        }else {
            // TODO: notify user when the state of the GPS resource is disabled. use one icon on action bar?

        }
    }

    public void configureMapView(MapView mapView) throws InvalidAppConfigException {
        double x = ResourceHelper.getDoubleResource(R.dimen.default_map_center_x);
        double y = ResourceHelper.getDoubleResource(R.dimen.default_map_center_y);

        int initialZoomLevel = 5;
        int maxZoomLevel = ResourceHelper.getIntResource(R.integer.default_max_zoom_level);

        boolean builtInZoomControls= ResourceHelper.getBooleanResource(R.bool.default_built_in_zoom_controls);
        boolean multiTouchControls= ResourceHelper.getBooleanResource(R.bool.default_multi_touch_controls);

        GeoPoint gPt = new GeoPoint(x,y);

        mapView.getController().animateTo(gPt);
        mapView.setMaxZoomLevel(maxZoomLevel);
        mapView.setMultiTouchControls(multiTouchControls);

        mapView.getController().setZoom(initialZoomLevel);

        if(menuMapController==null)
        {
            throw new InvalidAppConfigException("Missing MenuMapController on MapFragment map configuration.");
        }
    }

    public void drawCross(ImageView drawingImageView) throws InvalidAppConfigException {
        Bitmap bitmap = Bitmap.createBitmap(getActivity().getWindowManager()
                .getDefaultDisplay().getWidth(), getActivity().getWindowManager()
                .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawingImageView.setImageBitmap(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);

        int centerW = canvas.getWidth() / 2;
        int centerH = canvas.getHeight() / 2;

        int offset = ResourceHelper.getIntResource(R.integer.offsetCross);

        canvas.drawLine(centerW, centerH + offset, centerW, centerH - offset, paint);
        canvas.drawLine(centerW + offset, centerH, centerW - offset, centerH, paint);
    }

    public void centerMapToGPS() {

        String waitingMsg="";
        try {
            waitingMsg = ResourceHelper.getStringResource(R.string.message_waiting_location_gps);
        }catch (InvalidAppConfigException e) {
            e.printStackTrace();
            waitingMsg="Waiting...";
        }

        final ProgressDialog progressDialog = Message.startProgressDialog(mMapView.getContext(), waitingMsg);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                progressDialog.dismiss();
                GPSService.unregisterListener(mMapView.getContext(), this);
                // Called when a new location is found by the GPS location provider.
                GeoPoint point=new GeoPoint(location);
                mMapView.getController().animateTo(point);
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {
                progressDialog.dismiss();
                Message.showErrorMessage((MainActivity) mMapView.getContext(), R.string.fail, R.string.disabled_provider);
            }
        };

        if(!GPSService.registerListener(mMapView.getContext(), locationListener)){
            progressDialog.dismiss();
            Message.showErrorMessage((MainActivity)mMapView.getContext(),R.string.fail,R.string.disabled_provider);
        }
    }

    public void ZoomIn(){
        mMapView.getController().zoomIn();
    }

    public void ZoomOut(){
        mMapView.getController().zoomOut();
    }

    public synchronized void updateMap()
    {

        getActivity().runOnUiThread(new UpdateMapThread(mMapView));

    }

    protected class UpdateMapThread implements Runnable
    {
        MapView mapView;
        protected UpdateMapThread(MapView mapView)
        {
            this.mapView = mapView;
        }

        public void run() {
            if(mapView!=null)
            {
                this.mapView.invalidate();
            }
        }
    }

    public MapView getMapView()
    {
        return mMapView;
    }

}
