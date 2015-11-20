package br.org.funcate.terramobile.controller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import br.org.funcate.dynamicforms.FormUtilities;
import br.org.funcate.dynamicforms.FragmentDetailActivity;
import br.org.funcate.dynamicforms.images.ImageUtilities;
import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.LowMemoryException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.osmbonuspack.overlays.SFSEditableMarker;
import br.org.funcate.terramobile.model.service.AppGeoPackageService;
import br.org.funcate.terramobile.model.service.EditableLayerService;
import br.org.funcate.terramobile.model.service.FeatureService;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceHelper;
import br.org.funcate.terramobile.util.Util;

/**
 * Created by Andre Carvalho on 14/08/15.
 */
public class MarkerInfoWindowController {

    private ArrayList<File> temporaryImages;
    private MainActivity mainActivity;
    private ProgressBar pgrInfoWindow;
    private ImageButton btnEditMarker;
    // identify the return of the request of the Activity Form
    private static int FORM_RESULT_CODE = 222;

    public MarkerInfoWindowController(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setProgressBar(ProgressBar progressBar) {
        pgrInfoWindow = progressBar;
    }

    public void setImageBtn(ImageButton imageBtn) {
        btnEditMarker = imageBtn;
    }

    private void hideProgress() {
        // in create mode, does not display the info Window of the new marker
        if(pgrInfoWindow!=null && btnEditMarker!=null) {
            pgrInfoWindow.setVisibility(View.GONE);
            btnEditMarker.setVisibility(View.VISIBLE);
        }
    }

    public void editMarker(Marker marker) {
        Long markerId = null;
        try {
            markerId = ((SFSEditableMarker) marker).getMarkerId().longValue();
        }catch (TerraMobileException e){
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.fail, e.getMessage());
            return;
        }catch (InvalidAppConfigException e){
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.fail, e.getMessage());
            return;
        }
        startActivityForm(markerId);
    }

    public void deleteMarker(Marker marker) throws TerraMobileException {
        GpkgLayer layer=this.mainActivity.getMainController().getTreeViewController().getSelectedEditableLayer();
        boolean exec;

        try {
            exec = AppGeoPackageService.deleteFeature(layer, ((SFSEditableMarker)marker).getMarkerId());
            if(!exec) {
                throw new TerraMobileException(ResourceHelper.getStringResource(R.string.feature_not_found));
            }else{
                this.mainActivity.getMainController().getMapFragment().updateMap();
            }
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
        } catch (LowMemoryException e) {
            e.printStackTrace();
        }
    }

    public void moveMarker(Marker marker) throws TerraMobileException {
        GpkgLayer layer=this.mainActivity.getMainController().getTreeViewController().getSelectedEditableLayer();
        try {
            if(!AppGeoPackageService.updateFeature(layer, marker)) {
                throw new TerraMobileException(ResourceHelper.getStringResource(R.string.failure_on_save_new_location));
            }
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            throw new TerraMobileException(e.getMessage());
        } catch (LowMemoryException e) {
            e.printStackTrace();
            throw new TerraMobileException(e.getMessage());
        }
    }

    public void startActivityForm() {
        // this magic value is used to determine when opening form to new collection data.
        long codePoint=-1;
        this.startActivityForm(codePoint);
    }

    public void startActivityForm(long pointID) {

        ArrayList<GeoPoint> geoPoints=null;
        GpkgLayer editableLayer;
        String geometryType = "";

        try{
            TreeViewController tv = mainActivity.getMainController().getTreeViewController();
            editableLayer = tv.getSelectedEditableLayer();
            if(editableLayer==null) {
                Message.showErrorMessage(mainActivity, R.string.failure_title_msg, R.string.missing_editable_layer);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.failure_title_msg, R.string.error_start_form);
            return;
        }

        SimpleFeature feature = null;
        Map<String, Object> images = null;
        Geometry geom;
        Bundle formDataValues=null;
        // if pointID >= 0 then this point exist on database
        if(pointID>=0) {
            try {
                feature = AppGeoPackageService.getFeature(editableLayer, pointID);
            } catch (InvalidAppConfigException e) {
                e.printStackTrace();
                Message.showErrorMessage(mainActivity, R.string.failure_title_msg, e.getMessage());
                return;
            } catch (LowMemoryException e) {
                e.printStackTrace();
                Message.showErrorMessage(mainActivity, R.string.failure_title_msg, e.getMessage());
                return;
            } catch (TerraMobileException e) {
                e.printStackTrace();
                Message.showErrorMessage(mainActivity, R.string.failure_title_msg, e.getMessage());
                return;
            }catch (Exception e) {
                e.printStackTrace();
                Message.showErrorMessage(mainActivity, R.string.failure_title_msg, R.string.error_start_form);
                return;
            }

            try {
                images = EditableLayerService.getImagesFromDatabase(editableLayer, pointID);
            } catch (TerraMobileException e) {
                e.printStackTrace();
                images = null;
            } catch (Exception e) {
                e.printStackTrace();
                images = null;
            }

            if (feature!=null && feature.getDefaultGeometry() != null) {
                geom = (Geometry) feature.getDefaultGeometry();
                if(geom!=null) {
                    geometryType=geom.getGeometryType();
                    if(geometryType.equals(FormUtilities.GEOJSON_TYPE_POINT) || geometryType.equals(FormUtilities.GEOJSON_TYPE_MULTIPOINT)) {
                        Coordinate[] coords = geom.getCoordinates();
                        int coordsLength = coords.length;
                        geoPoints=new ArrayList<GeoPoint>(coordsLength);
                        for (int i = 0; i < coordsLength; i++) {
                            geoPoints.add( new GeoPoint(coords[i].y, coords[i].x) );
                        }
                    }
                }
                formDataValues = FeatureService.featureAttrsToBundle(feature);
                if(images!=null && !images.isEmpty()) {
                    Bundle b = mediaToBundle(formDataValues, images);
                    if(b!=null) formDataValues = b;
                }
            }
        }else {
            geoPoints=new ArrayList<GeoPoint>(1);
            geoPoints.add((GeoPoint) getMapView().getMapCenter());
            SimpleFeatureType featureType = editableLayer.getFeatureType();
            GeometryDescriptor geometryDescriptor = featureType.getGeometryDescriptor();
            GeometryType defaultGeometryType = geometryDescriptor.getType();
            geometryType = defaultGeometryType.getDescription().toString();
        }

        try {
            Intent formIntent = new Intent(mainActivity, FragmentDetailActivity.class);
            formIntent.putExtra(LibraryConstants.SELECTED_POINT_ID, pointID);
            // The form name attribute, provided by JSON, shall be the same name of the editable layer.
            formIntent.putExtra(FormUtilities.ATTR_FORMNAME, editableLayer.getName());
            formIntent.putExtra(FormUtilities.ATTR_JSON_TAGS, editableLayer.getJSON());

            if(geoPoints!=null) {

                JSONObject geojson = new JSONObject();

                if(geometryType.equalsIgnoreCase(FormUtilities.GEOJSON_TYPE_POINT))
                    geojson.put(FormUtilities.GEOJSON_TAG_TYPE,FormUtilities.GEOJSON_TYPE_POINT);
                else if(geometryType.equalsIgnoreCase(FormUtilities.GEOJSON_TYPE_MULTIPOINT))
                    geojson.put(FormUtilities.GEOJSON_TAG_TYPE,FormUtilities.GEOJSON_TYPE_MULTIPOINT);

                JSONArray coordinates = new JSONArray();
                Iterator<GeoPoint> it = geoPoints.iterator();
                while (it.hasNext()) {
                    GeoPoint gp = it.next();
                    JSONArray coordinate = new JSONArray();
                    coordinate.put(gp.getLongitude());
                    coordinate.put(gp.getLatitude());
                    coordinates.put(coordinate);
                }
                geojson.put(FormUtilities.GEOJSON_TAG_COORDINATES, coordinates);
                String str_geojson = geojson.toString();
                formIntent.putExtra(FormUtilities.ATTR_GEOJSON_TAGS, str_geojson);
            }
            if(formDataValues!=null) {
                formIntent.putExtra(FormUtilities.ATTR_DATA_VALUES, formDataValues);
            }
            File directory = Util.getDirectory(mainActivity.getResources().getString(R.string.app_workspace_dir));

            formIntent.putExtra(FormUtilities.MAIN_APP_WORKING_DIRECTORY, directory.getAbsolutePath());
            mainActivity.startActivityForResult(formIntent, FORM_RESULT_CODE);

        } catch (Exception e) {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.failure_title_msg, R.string.error_start_form);
        }
    }

    private Bundle mediaToBundle(Bundle bundle, Map<String, Object> images) {
        Bundle imageMapBundle = new Bundle(images.size());

        Set<String> keys = images.keySet();
        Iterator<String> itKeys = keys.iterator();
        String imagePath = "";
        temporaryImages = new ArrayList<File>(keys.size());
        try {
            while (itKeys.hasNext()) {
                String key = itKeys.next();
                File tmpFile = File.createTempFile(ImageUtilities.getTempImageName(null), key);
                Object value = images.get(key);
                imagePath = tmpFile.getPath();
                ImageUtilities.writeImageDataToFile((byte[])value, imagePath);
                temporaryImages.add(tmpFile);
                imageMapBundle.putString(key, imagePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        bundle.putBundle(FormUtilities.IMAGE_MAP, imageMapBundle);

        return bundle;
    }

    public void makeSomeProcessWithResult(int requestCode, int resultCode, Intent data) {

        hideProgress();

        if (resultCode == Activity.RESULT_OK && requestCode == FORM_RESULT_CODE) {
            Bundle extras = data.getBundleExtra(LibraryConstants.PREFS_KEY_FORM);
            try {
                EditableLayerService.storeData(mainActivity, extras);
            }catch (TerraMobileException tme) {
                tme.printStackTrace();
                Message.showErrorMessage(mainActivity, R.string.error, R.string.missing_form_data);
            }catch (QueryException qe) {
                qe.printStackTrace();
                Message.showErrorMessage(mainActivity, R.string.error, R.string.error_while_storing_form_data);
            }
            this.mainActivity.getMainController().getMapFragment().updateMap();
        }
    }

    public MapView getMapView() {
        MapView mapView = (MapView) this.mainActivity.findViewById(R.id.mapview);
        return mapView;
    }

    public void viewFeatureData(long featureID) {
        GpkgLayer editableLayer;
        try{
            TreeViewController tv = mainActivity.getMainController().getTreeViewController();
            editableLayer = tv.getSelectedEditableLayer();
            if(editableLayer==null) {
                Message.showErrorMessage(mainActivity, R.string.failure_title_msg, R.string.missing_editable_layer);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.failure_title_msg, R.string.error_start_form);
            return;
        }

        FeatureInfoPanelController controller = mainActivity.getMainController().getFeatureInfoPanelController();
        controller.startFeatureInfoPanel(editableLayer, featureID);
    }

    public void closeAllInfoWindows() {
        MapView mapView = getMapView();
        if(mapView!=null) InfoWindow.closeAllInfoWindowsOn(mapView);
    }
}
