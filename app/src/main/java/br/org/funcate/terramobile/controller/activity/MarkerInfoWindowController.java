package br.org.funcate.terramobile.controller.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.augtech.geoapi.geopackage.DateUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
import br.org.funcate.terramobile.model.geomsource.SFSPoint;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.osmbonuspack.overlays.SFSMarker;
import br.org.funcate.terramobile.model.service.AppGeoPackageService;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceHelper;
import br.org.funcate.terramobile.util.Util;

/**
 * Created by Andre Carvalho on 14/08/15.
 */
public class MarkerInfoWindowController {

    private ArrayList<File> temporaryImages;
    private MainActivity mainActivity;
    // identify the return of the request of the Activity Form
    private static int FORM_RESULT_CODE = 222;

    public MarkerInfoWindowController(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void editMarker(Marker marker) {
        startActivityForm(((SFSMarker)marker).getMarkerId().longValue());
    }

    /*private Long getMarkerId(Marker marker) {
        String markerId = ((SFSPoint)marker.getRelatedObject()).mId;
        String editableLayerName = this.mainActivity.getTreeView().getSelectedEditableLayer().getName();
        markerId = markerId.replaceFirst(editableLayerName,"");
        return new Long(markerId);
    }*/

    public void deleteMarker(Marker marker) throws TerraMobileException {
        GpkgLayer layer=this.mainActivity.getTreeView().getSelectedEditableLayer();
        boolean exec;

        try {
            exec = AppGeoPackageService.deleteFeature(layer, ((SFSMarker)marker).getMarkerId());
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
        GpkgLayer layer=this.mainActivity.getTreeView().getSelectedEditableLayer();
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

        GeoPoint point=null;
        GpkgLayer editableLayer;
        try{
            TreeView tv = mainActivity.getTreeView();
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
                images = AppGeoPackageService.getImagesFromDatabase(editableLayer, pointID);
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
                Message.showErrorMessage(mainActivity, R.string.failure_title_msg, R.string.error_start_form);
                return;
            }

            if (feature!=null && feature.getDefaultGeometry() != null) {
                geom = (Geometry) feature.getDefaultGeometry();
                if(geom!=null && geom.getGeometryType().equals("Point")) {
                    Coordinate[] coords = geom.getCoordinates();
                    if(coords.length>0 && coords.length==1)
                        point = new GeoPoint(coords[0].y, coords[0].x);
                }
                formDataValues = featureAttrsToBundle(feature);
                if(images!=null && !images.isEmpty()) {
                    formDataValues = mediaToBundle(formDataValues, images);
                }
            }
        }else {
            point = (GeoPoint) getMapView().getMapCenter();
        }

        try {
            Intent formIntent = new Intent(mainActivity, FragmentDetailActivity.class);
            formIntent.putExtra(LibraryConstants.SELECTED_POINT_ID, pointID);
            // The form name attribute, provided by JSON, shall be the same name of the editable layer.
            formIntent.putExtra(FormUtilities.ATTR_FORMNAME, editableLayer.getName());
            formIntent.putExtra(FormUtilities.ATTR_JSON_TAGS, editableLayer.getJSON());
            if(point!=null) {
                formIntent.putExtra(FormUtilities.TYPE_LATITUDE, point.getLatitude());
                formIntent.putExtra(FormUtilities.TYPE_LONGITUDE, point.getLongitude());
            }
            if(formDataValues!=null) {
                formIntent.putExtra(FormUtilities.ATTR_DATA_VALUES, formDataValues);
            }
            File directory = Util.getDirectory(mainActivity.getResources().getString(R.string.app_workspace_dir));

            formIntent.putExtra(FormUtilities.MAIN_APP_WORKING_DIRECTORY, directory.getAbsolutePath());
            mainActivity.startActivityForResult(formIntent, FORM_RESULT_CODE);

        } catch (Exception e) {
            Message.showErrorMessage(mainActivity, R.string.failure_title_msg, R.string.error_start_form);
            return;
        }
    }

    private Bundle featureAttrsToBundle(SimpleFeature feature) {

        List<Object> attrs = feature.getAttributes();
        if(attrs.size()<=0) return null;
        List<AttributeType> featureTypes = feature.getFeatureType().getTypes();

        Iterator<AttributeType> itTypes = featureTypes.iterator();
        Bundle bundle = new Bundle(attrs.size());

        while (itTypes.hasNext()) {
            AttributeType attributeType = itTypes.next();
            Name typeName = attributeType.getName();
            String typeClass = attributeType.getBinding().getName();
            Object o = feature.getAttribute(typeName);
            String s=null;
            if(String.class.getName().equals(typeClass)){
                s = (String)o;
                bundle.putString(typeName.toString(),s);
            }else if(Double.class.getName().equals(typeClass)){
                Double d = (Double)o;
                s = d.toString();
            }else if(Integer.class.getName().equals(typeClass)){
                Integer i = (Integer)o;
                s = i.toString();
            }else if(Boolean.class.getName().equals(typeClass)){
                Boolean b = (Boolean)o;
                s = b.toString();
            }else if(Date.class.getName().equals(typeClass)){
                Date date = (Date)o;
                s = DateUtil.serializeDate(date);
            }else if(typeClass.equals("[Ljava.lang.Byte;")){
                byte[] photo=(byte[])o;
                bundle.putByteArray(typeName.toString(), photo);
            }

            if(s!=null) bundle.putString(typeName.toString(), s);
        }
        return bundle;
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
/*                Bitmap imageBitmap = ImageUtilities.getBitmapFromBlob((byte[])value);
                Bitmap thumbnail = ImageUtilities.makeThumbnail(imageBitmap);
                imageMapBundle.putByteArray(key, ImageUtilities.getBlobFromBitmap(thumbnail));*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bundle.putBundle(FormUtilities.IMAGE_MAP, imageMapBundle);

        return bundle;
    }

    public void makeSomeProcessWithResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && requestCode == FORM_RESULT_CODE) {
            Bundle extras = data.getBundleExtra(LibraryConstants.PREFS_KEY_FORM);
            try {
                AppGeoPackageService.storeData(mainActivity, extras);
            }catch (TerraMobileException tme) {
                tme.printStackTrace();
                Message.showErrorMessage(mainActivity, R.string.error, R.string.missing_form_data);
            }catch (QueryException qe) {
                qe.printStackTrace();
                Message.showErrorMessage(mainActivity, R.string.error, R.string.error_while_storing_form_data);
            }
            this.mainActivity.getMainController().getMapFragment().updateMap();
        }
        /*else {
            Message.showErrorMessage(mainActivity, R.string.error, R.string.cancel_form_data);
        }*/
    }

    public MapView getMapView() {
        MapView mapView = (MapView) this.mainActivity.findViewById(R.id.mapview);
        return mapView;
    }

}
