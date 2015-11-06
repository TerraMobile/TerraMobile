package br.org.funcate.terramobile.model.service;

/**
 * Created by Andre Carvalho on 29/04/15.
 */
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.augtech.geoapi.feature.SimpleFeatureImpl;
import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.org.funcate.dynamicforms.FormUtilities;
import br.org.funcate.dynamicforms.images.ImageUtilities;
import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.extended.model.TMConfigEditableLayer;
import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.controller.activity.TreeViewController;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.InvalidGeopackageException;
import br.org.funcate.terramobile.model.exception.LowMemoryException;
import br.org.funcate.terramobile.model.exception.StyleException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.geomsource.SFSLayer;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.osmbonuspack.overlays.SFSEditableMarker;
import br.org.funcate.terramobile.model.tilesource.MapTileGeoPackageProvider;
import br.org.funcate.terramobile.util.ResourceHelper;
import br.org.funcate.terramobile.util.Util;

public class AppGeoPackageService {

    private AppGeoPackageService()
    {

    }

    public static File getGpkgFile(Context context) {
        File directory = Util.getDirectory(context.getResources().getString(R.string.app_workspace_dir));
        ArrayList<File> gpkgFiles= Util.getGeoPackageFiles(directory, context.getResources().getString(R.string.geopackage_extension));

        File gpkgFile=null;
        if(gpkgFiles.size()>0){// now, using the first geopackage on list
            gpkgFile=gpkgFiles.get(0);
        }

        if(gpkgFiles.size()>1) {// if exist more than one item on the list...
            // TODO: show the geopackage files list to that user select one.
            /*for(File gpkg : gpkgFiles) {
                gpkgFile=gpkg;
            }*/
        }
        return gpkgFile;
    }

    /**
     * This method reads the layers metadata from the GeoPackage contents and convert it to GpkgLayer format.
     * @return ArrayList<GpkgLayer> listLayers, the list Layers
     * @throws Exception
     */
    public static ArrayList<GpkgLayer> getLayers(Project project, Context context) throws InvalidGeopackageException, QueryException {



        GeoPackage gpkg = null;

        if(project!=null)
            try {

                gpkg = GeoPackageService.readGPKG(context, project.getFilePath());
            }catch (Exception e) {
                throw new InvalidGeopackageException("Invalid GeoPackage file.");
            }
        else {
            Log.i("getLayers", "Project not found");
            return null;
        }

        if(!gpkg.isGPKGValid(false))
        {
            throw new InvalidGeopackageException("Invalid GeoPackage file.");
        }

        ArrayList<ArrayList<GpkgField>> layersList = GeoPackageService.getGpkgFieldsContents(gpkg, null, "");
        TMConfigEditableLayer tmConfigEditableLayer = GeoPackageService.getTMConfigEditableLayer(gpkg);
        ArrayList<GpkgLayer> listLayers=new ArrayList<GpkgLayer>();
        GpkgLayer layer;

        for (int i = 0,size = layersList.size(); i < size; i++) {

            ArrayList<GpkgField> aField = layersList.get(i);
            layer=new GpkgLayer(gpkg);// set geoPackage reference in this layer

            if(aField.size()!=10)
            {
                throw new InvalidGeopackageException("Invalid number of field on GPKG content table. ");
            }

            layer.setIndexOverlay(i);

            String layerName=null;
            GpkgField dataTypeField = null;

            // To getting bounding box
            Double minX=null;
            Double minY=null;
            Double maxX=null;
            Double maxY=null;

            for (int j = 0,len = aField.size(); j < len; j++) {
                GpkgField gpkgColumn = aField.get(j);
                String fieldname = gpkgColumn.getFieldName();
                if("table_name".equalsIgnoreCase(fieldname)){
                    layerName = (String) gpkgColumn.getValue();
                }else if("data_type".equalsIgnoreCase(fieldname)){
                    dataTypeField = gpkgColumn;
                }else if("min_x".equalsIgnoreCase(fieldname)){
                    minX=(Double) gpkgColumn.getValue();
                }else if("min_y".equalsIgnoreCase(fieldname)){
                    minY=(Double) gpkgColumn.getValue();
                }else if("max_x".equalsIgnoreCase(fieldname)){
                    maxX=(Double) gpkgColumn.getValue();
                }else if("max_y".equalsIgnoreCase(fieldname)){
                    maxY=(Double) gpkgColumn.getValue();
                }else if("srs_id".equalsIgnoreCase(fieldname)){
                    layer.setSrsId((Integer) gpkgColumn.getValue());
                }
            }

            layer.setName(layerName);

            // Getting data type
            if("features".equals(dataTypeField.getValue()))
            {
                if(!tmConfigEditableLayer.isEditable(layerName)) {
                    layer.setType(GpkgLayer.Type.FEATURES);
                    layer.setFeatureType(GeoPackageService.getLayerFeatureType(gpkg, layerName));
                }
                else {
                    layer.setType(GpkgLayer.Type.EDITABLE);
                    layer.setJSON(tmConfigEditableLayer.getJSONConfig(layerName));
                    layer.setFields(GeoPackageService.getLayerFields(gpkg, layerName));
                    layer.setFeatureType(GeoPackageService.getLayerFeatureType(gpkg, layerName));
                    layer.setMediaTable(tmConfigEditableLayer.getMediaTableConfig(layerName));
                    layer.setIndexOverlay(0);
                }
            } else if("tiles".equals(dataTypeField.getValue()))
            {
                layer.setType(GpkgLayer.Type.TILES);
                //layer.setIndexOverlay(1000);
            } else
            {
                //TODO:Verify if it's necessary to stop the process or ignore the current layer iteration
                throw new InvalidGeopackageException("Invalid layer .");
            }

            if((minX==null)||(maxX==null)||(minY==null)||(maxY==null))
            {
                minX=0.;
                maxX=0.;
                minY=0.;
                maxY=0.;
            }

            BoundingBox bb =new BoundingBoxImpl(minX, maxX, minY, maxY);

            layer.setBox(bb);
            listLayers.add(layer);
        }
        gpkg.close();

        return listLayers;
    }


    public static void storeData(Context context, Bundle formData) throws TerraMobileException, QueryException {
        ArrayList<String> keys = formData.getStringArrayList(LibraryConstants.FORM_KEYS);
        TreeViewController tv = ((MainActivity)context).getMainController().getTreeViewController();
        if(keys==null || keys.isEmpty()){
            throw new TerraMobileException(context.getString(R.string.missing_form_data));
        }else {
            try {
                SimpleFeature feature = makeSimpleFeature(formData, tv);
                ArrayList<String> databaseImages = getDatabaseImages(formData);
                ArrayList<Object> insertImages = getInsertImages(formData);
                GeoPackageService.writeLayerFeature(tv.getSelectedEditableLayer().getGeoPackage(), tv.getSelectedEditableLayer().getMediaTable(), feature, databaseImages, insertImages);

                ((MainActivity)context).getMainController().getMenuMapController().removeLayer(tv.getSelectedEditableLayer());
                ((MainActivity)context).getMainController().getMenuMapController().addLayer(tv.getSelectedEditableLayer());

            }catch (Exception e) {
                int flags = context.getApplicationInfo().flags;
                if((flags & context.getApplicationInfo().FLAG_DEBUGGABLE) != 0) {
                    throw new TerraMobileException(e.getMessage());// write log here
                }else {
                    throw new TerraMobileException(context.getString(R.string.error_while_storing_form_data));
                }
            }catch (StyleException e) {
                e.printStackTrace();
                throw new TerraMobileException(e.getMessage());
            } catch (InvalidAppConfigException e) {
                e.printStackTrace();
                throw new TerraMobileException(e.getMessage());
            }
        }
    }

    public static Map<String, Object> getImagesFromDatabase(GpkgLayer layer, long featureID) throws TerraMobileException {
        Map<String, Object> images;
        try{
            images = GeoPackageService.getMedias(layer.getGeoPackage(), layer.getMediaTable(), featureID);
        }catch (QueryException qe){
            qe.printStackTrace();
            throw new TerraMobileException(qe.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            throw new TerraMobileException(e.getMessage());
        }
        return images;
    }

    /**
     * Get Array of the image's identifiers loaded from the Form.
     * This identifiers represent the database images of the list image display on the Form.
     * @param formData, the form
     * @return list of the images identifiers
     */
    private static ArrayList<String> getDatabaseImages(Bundle formData) {
        ArrayList<String> imageIds=null;

        if(formData.containsKey(FormUtilities.DATABASE_IMAGE_IDS)) {
            imageIds = formData.getStringArrayList(FormUtilities.DATABASE_IMAGE_IDS);
        }
        return imageIds;
    }

    /**
     * Get Array of the byte[] loaded from system files using the paths registered into the Form.
     * @param formData, the form
     * @return list in memory of the images in binary format
     */
    private static ArrayList<Object> getInsertImages(Bundle formData) {

        ArrayList<Object> images = null;
        Object image;

        if(formData.containsKey(FormUtilities.INSERTED_IMAGE_PATHS)) {
            ArrayList<String> imagePaths = formData.getStringArrayList(FormUtilities.INSERTED_IMAGE_PATHS);

            if(imagePaths.isEmpty()) return images;

            images = new ArrayList<Object>(imagePaths.size());
            Iterator<String> it = imagePaths.iterator();
            while (it.hasNext()) {
                String path = it.next();
                if (ImageUtilities.isImagePath(path)) {
                    image = ImageUtilities.getImageFromPath(path, 2);
                    images.add(image);
                }
            }
        }
        return images;
    }

    private static SimpleFeature getSimpleFeatureInstance(GpkgLayer layer, String featureID) {
        SimpleFeatureType ft = layer.getFeatureType();
        SimpleFeatureImpl feature = new SimpleFeatureImpl(featureID, null, ft);
        return feature;
    }

    private static SimpleFeature makeSimpleFeature(Bundle formData, TreeViewController tv) throws JSONException, TerraMobileException {


        ArrayList<GpkgField> fields = tv.getSelectedEditableLayer().getFields();
        SimpleFeatureType ft = tv.getSelectedEditableLayer().getFeatureType();
        GeometryType geometryType = ft.getGeometryDescriptor().getType();
        Object[] attrs = new Object[fields.size()];
        String featureID=null;
        // if contains Geometry identification when update feature process is call
        if(formData.containsKey(FormUtilities.GEOM_ID)) {
            featureID=ft.getTypeName()+formData.getLong(FormUtilities.GEOM_ID);
        }

        SimpleFeatureImpl feature = new SimpleFeatureImpl(featureID, null, ft);

        GeometryFactory factory=new GeometryFactory();

        if(formData.containsKey(FormUtilities.ATTR_GEOJSON_TAGS)) {
            String geojsonTags = formData.getString(FormUtilities.ATTR_GEOJSON_TAGS);
            JSONObject geojsonGeometry = new JSONObject(geojsonTags);
            String geojsonGeometryType = geojsonGeometry.getString(FormUtilities.GEOJSON_TAG_TYPE);
            JSONArray geojsonCoordinates = geojsonGeometry.getJSONArray(FormUtilities.GEOJSON_TAG_COORDINATES);

            if ( !geometryType.getName().toString().equalsIgnoreCase(geojsonGeometryType) ) {
                throw new TerraMobileException("Geometry type is incompatible.");
            }

            if(geojsonGeometryType.equalsIgnoreCase(FormUtilities.GEOJSON_TYPE_POINT)) {
                Coordinate coordinate = new Coordinate(geojsonCoordinates.getDouble(0), geojsonCoordinates.getDouble(1));
                Point point = factory.createPoint(coordinate);
                Name geomColName = ft.getGeometryDescriptor().getName();
                attrs[ft.indexOf(geomColName)] = point;

            }else if(geojsonGeometryType.equalsIgnoreCase(FormUtilities.GEOJSON_TYPE_MULTIPOINT)) {

                int geomSize = geojsonCoordinates.length();
                Coordinate[] coordinates = new Coordinate[geomSize];
                for (int i = 0; i < geomSize; i++) {
                    JSONArray geojsonCoordinate = geojsonCoordinates.getJSONArray(i);
                    Coordinate coordinate = new Coordinate(geojsonCoordinate.getDouble(0), geojsonCoordinate.getDouble(1));
                    coordinates[i]=coordinate;
                }
                MultiPoint multiPoint = factory.createMultiPoint(coordinates);
                Name geomColName = ft.getGeometryDescriptor().getName();
                attrs[ft.indexOf(geomColName)] = multiPoint;

            }else {
                throw new TerraMobileException("Geometry type is wrong.");
            }
        }

        ArrayList<String> formKeys = formData.getStringArrayList(LibraryConstants.FORM_KEYS);
        ArrayList<String> formTypes = formData.getStringArrayList(LibraryConstants.FORM_TYPES);

        for (int i = 0, len = formKeys.size(); i < len; i++) {
            String key = formKeys.get(i);
            GpkgField field = getFieldByName(fields, key);
            if(field==null) continue;
            String dbType = field.getFieldType();

            dbType = AppGeoPackageService.mappingAffinityDBType(dbType);

            String formType = formTypes.get(i);
            int index = ft.indexOf(key);

            if(index<0) continue;

            if ("REAL".equalsIgnoreCase(dbType)) {
                Double d = formData.getDouble(key);
                attrs[index]=d;
            } else if ("TEXT".equalsIgnoreCase(dbType)) {
                String s = formData.getString(key);
                attrs[index]=s;
            } else if ("INTEGER".equalsIgnoreCase(dbType)) {

                if("INTEGER".equalsIgnoreCase(formType)) {
                    Integer in = formData.getInt(key);
                    attrs[index]=in;
                }else {
                    Boolean aBoolean = formData.getBoolean(key);
                    attrs[index]=aBoolean;
                }
            } else if ("NUMERIC".equalsIgnoreCase(dbType)) {

                if("BOOLEAN".equalsIgnoreCase(formType)) {
                    Boolean aBoolean = formData.getBoolean(key);
                    attrs[index]=aBoolean;
                }else if("DATE".equalsIgnoreCase(formType)) {
                    String date = formData.getString(key);
                    Date dt = stringToDate(date);
                    if (dt == null) {
                        dt = new Date();
                    }
                    attrs[index]=dt;
                }else if("DATETIME".equalsIgnoreCase(formType)) {
                    String date = formData.getString(key);
                    Date dt = stringToDate(date);
                    if (dt == null) {
                        dt = new Date();
                    }
                    attrs[index]=dt;
                }
            } else if ("BLOB".equalsIgnoreCase(dbType)) {
                if( !key.equals(feature.getFeatureType().getGeometryDescriptor().getName())) {
                    String path = formData.getString(key);
                    if (path!=null && ImageUtilities.isImagePath(path)) {
                        byte[] blob = ImageUtilities.getImageFromPath(path, 1);
                        attrs[index]=blob;
                    }else{
                        attrs[index]=null;
                    }
                }
            }
        }
        feature.setAttributes(attrs);
        return feature;
    }

    /**
     * Method to map data types from table for affinity data types used on sqlite mechanisms.
     * <pre>
     * Determination Of Column Affinity.
     * The affinity of a column is determined by the declared type of the column, according to the following rules in the order shown:
     *  - If the declared type contains the string "INT" then it is assigned INTEGER affinity.
     *  - If the declared type of the column contains any of the strings "CHAR", "CLOB", or "TEXT" then that column has TEXT affinity. Notice that the type VARCHAR contains the string "CHAR" and is thus assigned TEXT affinity.
     *  - If the declared type for a column contains the string "BLOB" or if no type is specified then the column has affinity BLOB.
     *  - If the declared type for a column contains any of the strings "REAL", "FLOA", or "DOUB" then the column has REAL affinity.
     *  - Otherwise, the affinity is NUMERIC.
     *
     * Note that the order of the rules for determining column affinity is important. A column whose declared type is "CHARINT" will match both rules 1 and 2 but the first rule takes precedence and so the column affinity will be INTEGER.
     * </pre>
     * @see <a href="https://www.sqlite.org/datatype3.html">www.sqlite.org/datatype3.html</a>
     * @return originalDBType, the original column data type read from table.
     */
    private static String mappingAffinityDBType(String originalDBType) {

        String affinityDBType="";
        originalDBType = originalDBType.toUpperCase();

        if(originalDBType.contains("INT")) {
            affinityDBType = "INTEGER";
        }else if(originalDBType.contains("CHAR") || originalDBType.contains("CLOB") || originalDBType.contains("TEXT")) {
            affinityDBType = "TEXT";
        }else if(originalDBType.contains("BLOB") || originalDBType.isEmpty()) {
            affinityDBType = "BLOB";
        }else if(originalDBType.contains("REAL") || originalDBType.contains("FLOA") || originalDBType.contains("DOUB")) {
            affinityDBType = "REAL";
        }else {
            affinityDBType = "NUMERIC";
        }

        return affinityDBType;
    }

    /**
     * This method parse a date String, in this format (yyyy-MM-dd), to a Date, in this format ( YYYY-MM-DDTHH:MM:SS.SSS )
     * See this link for more detail: https://www.sqlite.org/lang_datefunc.html
     * @param strDate, the date in this format yyyy-MM-dd HH:mm:ss
     * @return a Date
     */
    private static Date stringToDate(String strDate) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        //SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date dt = null;
        try {
            dt = inputFormat.parse(strDate);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return dt;
    }

    private static GpkgField getFieldByName(ArrayList<GpkgField> fields, String name) {
        GpkgField field=null;
        if(fields!=null) {
            for (int i = 0, len = fields.size(); i < len; i++) {
                field = fields.get(i);
                if(name.equals(field.getFieldName())) return field;
            }
        }
        return field;
    }

    public static SFSLayer getFeatures(GpkgLayer layer) throws InvalidAppConfigException, LowMemoryException, TerraMobileException {

        try {

            List<SimpleFeature> features = GeoPackageService.getGeometries(layer.getGeoPackage(),layer.getName(),null);

            SFSLayer l = new SFSLayer(features, layer);

            return l;

        }
        catch (Exception e) {
            e.printStackTrace();
            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.read_features_exception), e);
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
            throw new LowMemoryException(ResourceHelper.getStringResource(R.string.read_features_out_of_memory_exception));
        }
    }

    public static SimpleFeature getFeature(GpkgLayer layer, long featureID) throws InvalidAppConfigException, LowMemoryException, TerraMobileException {

        try {

            SimpleFeature feature = GeoPackageService.getFeatureByID(layer.getGeoPackage(), layer.getName(), featureID);

            return feature;

        }
        catch (Exception e) {
            e.printStackTrace();
            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.read_features_exception), e);
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
            throw new LowMemoryException(ResourceHelper.getStringResource(R.string.read_features_out_of_memory_exception));
        }
    }

    public static boolean deleteFeature(GpkgLayer layer, long featureID) throws InvalidAppConfigException, LowMemoryException, TerraMobileException {

        try {

            return GeoPackageService.deleteFeature(layer.getGeoPackage(), layer.getName(), featureID);

        }
        catch (Exception e) {
            e.printStackTrace();
            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.read_features_exception), e);
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
            throw new LowMemoryException(ResourceHelper.getStringResource(R.string.read_features_out_of_memory_exception));
        }
    }

    public static boolean updateFeature(GpkgLayer layer, Marker marker) throws InvalidAppConfigException, LowMemoryException, TerraMobileException {

        if(!(marker instanceof SFSEditableMarker)) return false;

        SimpleFeature feature = getSimpleFeatureInstance(layer, ((SFSEditableMarker) marker).getFeatureId());

        ArrayList<GpkgField> fields = layer.getFields();
        SimpleFeatureType ft = layer.getFeatureType();
        Object[] attrs = new Object[fields.size()];
        GeometryFactory factory=new GeometryFactory();
        GeoPoint p = marker.getPosition();
        Coordinate coordinate = new Coordinate(p.getLongitude(), p.getLatitude());
        Point point = factory.createPoint(coordinate);
        Name geomColName = ft.getGeometryDescriptor().getName();
        attrs[ft.indexOf(geomColName)]=point;

        feature.setAttributes(attrs);

        try {

            return GeoPackageService.updateFeature(layer.getGeoPackage(), feature);

        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
            throw new LowMemoryException(ResourceHelper.getStringResource(R.string.read_features_out_of_memory_exception));
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.read_features_exception), e);
        }
    }

}
