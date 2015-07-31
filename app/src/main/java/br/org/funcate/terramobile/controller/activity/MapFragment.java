package br.org.funcate.terramobile.controller.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import java.io.File;

import br.org.funcate.dynamicforms.FormUtilities;
import br.org.funcate.dynamicforms.FragmentDetailActivity;
import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.constants.OpenStreetMapConstants;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.tilesource.AppGeoPackageService;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceHelper;
import br.org.funcate.terramobile.util.Util;

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
    // Constants
    // ===========================================================

    private static final int DIALOG_ABOUT_ID = 1;

    private static final int MENU_SAMPLES = Menu.FIRST + 1;
    private static final int MENU_ABOUT = MENU_SAMPLES + 1;

    private static final int MENU_LAST_ID = MENU_ABOUT + 1; // Always set to last unused id

    // ===========================================================
    // Fields
    // ===========================================================

    private SharedPreferences mPrefs;
    private MapView mMapView;
    private ResourceProxy mResourceProxy;
    private ImageView drawingImageView;

    private LayoutInflater inflater;
    private ViewGroup container;

    private MyLocationOverlay  myLocationoverlay;
    private ImageButton gpsLocation;
    private ImageButton zoomIn;
    private ImageButton zoomOut;




    private Context context;
    private static int FORM_COLLECT_DATA = 222;

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
                showGpsLocation(context);
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
        zoomOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ZoomOut();
            }
        });


        super.onCreate(savedInstanceState);

        return v;
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
        } catch (final IllegalArgumentException ignore) {
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

    public void startForm() {

        GeoPoint point = getCenterMap();

        // This id is provided from the selected point, if one it is selected otherwise -1 is default.
        long selectedPointID = -1;
        GpkgLayer editableLayer=null;
        try{
            TreeView tv = ((MainActivity) context).getTreeView();
            editableLayer = tv.getSelectedEditableLayer();
            if(editableLayer==null) {
                Message.showErrorMessage(((MainActivity) context), R.string.failure_title_msg, R.string.missing_editable_layer);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            Message.showErrorMessage(((MainActivity) context), R.string.failure_title_msg, R.string.error_start_form);
            return;
        }


        try {
            Intent formIntent = new Intent(context, FragmentDetailActivity.class);
            formIntent.putExtra(LibraryConstants.SELECTED_POINT_ID, selectedPointID);
            // The form name attribute, provided by JSON, shall be the same name of the editable layer.
            formIntent.putExtra(FormUtilities.ATTR_FORMNAME, editableLayer.getName());
            formIntent.putExtra(FormUtilities.ATTR_JSON_TAGS, editableLayer.getJSON());
            formIntent.putExtra(FormUtilities.TYPE_LATITUDE, point.getLatitude());
            formIntent.putExtra(FormUtilities.TYPE_LONGITUDE, point.getLongitude());
            File directory = Util.getDirectory(this.getResources().getString(R.string.app_workspace_dir));

            formIntent.putExtra(FormUtilities.MAIN_APP_WORKING_DIRECTORY, directory.getAbsolutePath());
            startActivityForResult(formIntent, FORM_COLLECT_DATA);

        } catch (Exception e) {
            Message.showErrorMessage(((MainActivity) context), R.string.failure_title_msg, R.string.error_start_form);
            return;
        }
    }

    public GeoPoint getCenterMap() {
        GeoPoint point = (GeoPoint) this.mMapView.getMapCenter();
        return point;
    }

    private void addBookmark(GeoPoint point) {

        Marker marker = new Marker(this.mMapView);
        marker.setPosition(new GeoPoint(point.getLatitude(), point.getLongitude()));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(R.drawable.marker_red));
        marker.setDraggable(true);
        if(!this.mMapView.getOverlays().add(marker)) {
            Message.showErrorMessage(((MainActivity) context), R.string.failure_title_msg, R.string.error_start_form);
        }else {
            updateMap();
        }
    }

    public void showGpsLocation(Context context){
        myLocationoverlay = new MyLocationOverlay(context, this.mMapView);
        myLocationoverlay.enableMyLocation();
        myLocationoverlay.disableCompass();
        myLocationoverlay.enableFollowLocation();
        myLocationoverlay.setDrawAccuracyEnabled(true);
        myLocationoverlay.runOnFirstFix(new Runnable() {
            public void run() {
                if (myLocationoverlay.getMyLocation() != null) {
                    // go to MyLocation
                    mMapView.getController().animateTo(myLocationoverlay
                            .getMyLocation());
                    myLocationoverlay.disableFollowLocation();
                } else {

                    mMapView.getController().animateTo(new GeoPoint(R.dimen.default_map_center_x, R.dimen.default_map_center_y));

                }
            }
        });
    }

    public void ZoomIn(){
        mMapView.getController().zoomIn();
    }

    public void ZoomOut(){
        mMapView.getController().zoomOut();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == FORM_COLLECT_DATA) {
            Bundle extras = data.getBundleExtra(LibraryConstants.PREFS_KEY_FORM);
            try {
                GeoPoint point = getCenterMap();
                addBookmark(point);
                AppGeoPackageService.storeData( context, extras);
            }catch (TerraMobileException tme) {
                //Message.showMessage(this, R.drawable.error, getResources().getString(R.string.error), tme.getMessage());
                tme.printStackTrace();
                Message.showErrorMessage(((MainActivity) context), R.string.error, R.string.missing_form_data);
            }catch (QueryException qe) {
                //Message.showMessage(this, R.drawable.error, getResources().getString(R.string.error), qe.getMessage());
                qe.printStackTrace();
                Message.showErrorMessage(((MainActivity) context), R.string.error, R.string.error_while_storing_form_data);
            }
        }else {
            Message.showErrorMessage(((MainActivity) context), R.string.error, R.string.cancel_form_data);
        }
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

}
