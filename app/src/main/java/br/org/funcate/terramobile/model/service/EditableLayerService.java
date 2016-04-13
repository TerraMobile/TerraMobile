package br.org.funcate.terramobile.model.service;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import com.augtech.geoapi.feature.SimpleFeatureImpl;
import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;
import com.augtech.geoapi.geopackage.ICursor;
import com.augtech.geoapi.geopackage.table.GpkgTriggers;
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

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.org.funcate.dynamicforms.FormUtilities;
import br.org.funcate.dynamicforms.images.ImageUtilities;
import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.extended.model.TMConfigEditableLayer;
import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.controller.activity.TreeViewController;
import br.org.funcate.terramobile.model.db.dao.LayerFormDAO;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.StyleException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.util.ResourceHelper;

/**
 * Created by Andre Carvalho on 19/11/15.
 */
public class EditableLayerService {

    /**
     * Prepare data related to one Feature and persist then.
     * @param context, The application context.
     * @param formData, The Feature's data provided from the gathering form.
     * @throws TerraMobileException
     * @throws QueryException
     */
    public static void storeData(Context context, Bundle formData) throws TerraMobileException, QueryException, DAOException {
        ArrayList<String> keys = formData.getStringArrayList(LibraryConstants.FORM_KEYS);
        TreeViewController tv = ((MainActivity)context).getMainController().getTreeViewController();
        if(keys==null || keys.isEmpty()){
            throw new TerraMobileException(context.getString(R.string.missing_form_data));
        }else {
            try {
                SimpleFeature feature = makeSimpleFeature(formData, tv);
                ArrayList<String> databaseImages = getStoredImagesFromForm(formData);// ids list from removed medias
                ArrayList<String> insertImages = getNewImagesFromForm(formData);// thumbnail medias in byte[] format
                GeoPackage geoPackage = tv.getSelectedEditableLayer().getGeoPackage();
                long featureID = GeoPackageService.writeLayerFeature(geoPackage, feature);
                String mediaTable = tv.getSelectedEditableLayer().getMediaTable();
                MediaService.upgradePictures(context, geoPackage, mediaTable, databaseImages, insertImages, featureID);

                ((MainActivity) context).getMainController().getMenuMapController().removeLayer(tv.getSelectedEditableLayer());
                ((MainActivity)context).getMainController().getMenuMapController().addLayer(tv.getSelectedEditableLayer());

            }catch (Exception e) {
                int flags = context.getApplicationInfo().flags;
                if((flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
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


    /**
     * Load the configuration to create form to editable layer.
     * @param gpkg, the GeoPackage reference
     * @return The id of the editable layer and the JSON configuration into TMConfigEditableLayer object
     * @throws QueryException
     */
    public static TMConfigEditableLayer getTMConfigEditableLayer(Context context, GeoPackage gpkg) throws DAOException, QueryException, InvalidAppConfigException {

        String tableName = LayerFormDAO.TABLE_NAME;//"tm_layer_form";
        String[] columns = new String[3];
        columns[0] = ResourceHelper.getStringResource(R.string.identify_layer_form);//"gpkg_layer_identify";
        columns[1] = ResourceHelper.getStringResource(R.string.json_layer_form);//"tm_form";
        columns[2] = ResourceHelper.getStringResource(R.string.media_layer_form);//"tm_media_table";

        TMConfigEditableLayer tmConfigEditableLayer=new TMConfigEditableLayer();
        ICursor c=null;
        try {
            c = gpkg.getDatabase().doQuery(tableName, columns, null);
            while (c.moveToNext()) {
                String id = c.getString(0);
                String json = c.getString(1);
                String mediaTable = c.getString(2);
                boolean isValid = EditableLayerService.validateEditableLayerConfiguration(context, gpkg, id, json, mediaTable);
                if(isValid) {
                    json = removeUnprintableCharacters(json);
                    if(mediaTable == null || mediaTable.isEmpty()) {
                        mediaTable = id + ResourceHelper.getStringResource(R.string.table_suffix_media);
                    }
                    tmConfigEditableLayer.addConfig(id, json, mediaTable);
                    EditableLayerService.removeTriggersOfTable(gpkg, id);
                }
            }
            c.close();
        }catch (Exception e) {
            if(c!=null) c.close();
            throw new QueryException(e.getMessage());
        }

        return tmConfigEditableLayer;
    }


    private static boolean validateEditableLayerConfiguration(Context context,
                                                              GeoPackage gpkg,
                                                              String layerName,
                                                              String jsonForm,
                                                              String mediaTable) throws Exception, DAOException, InvalidAppConfigException {

        boolean isValid=false;

        if(layerName==null || layerName.isEmpty() || jsonForm==null || jsonForm.isEmpty()) return false;

        String VERIFY_STATEMENT = "SELECT name FROM sqlite_master WHERE type = ''{0}'' AND tbl_name = ''{1}''";
        String stmt = MessageFormat.format(VERIFY_STATEMENT, "table", layerName);

        // validate if layer table exists on geopackage
        ICursor c = gpkg.getDatabase().doRawQuery(stmt);
        if (c.moveToFirst()) {
            String tableName = c.getString(0);
            c.close();
            if(tableName==null || tableName.isEmpty()) {
                return false;
            }else if(tableName.equals(layerName)) {
                isValid=true;
            }
        }else {
            c.close();
            return false;
        }

        // validate if picture field exists on json
        CharSequence cs = FormUtilities.TYPE_PICTURES;
        if(jsonForm.contains(cs)) {

            // validate if registered name of the media table is valid
            if (mediaTable == null || mediaTable.isEmpty()) {

                mediaTable = layerName + ResourceHelper.getStringResource(R.string.table_suffix_media);//"_picture_data";
                isValid = MediaService.createMediaTable(context, gpkg, layerName, mediaTable);

            } else {

                stmt = MessageFormat.format(VERIFY_STATEMENT, "table", mediaTable);
                // validate if media table exists on geopackage
                c = gpkg.getDatabase().doRawQuery(stmt);
                if (c.moveToFirst()) {
                    String tableName = c.getString(0);
                    c.close();
                    if(tableName==null || tableName.isEmpty() || !tableName.equals(mediaTable)) {
                        isValid = MediaService.createMediaTable(context, gpkg, layerName, mediaTable);
                    }
                } else {// the result set is empty, then media table not exists.
                    c.close();
                    isValid = MediaService.createMediaTable(context, gpkg, layerName, mediaTable);
                }
            }
        }

        return isValid;
    }

    private static String removeUnprintableCharacters(String str) {

        StringBuffer buf = new StringBuffer();
        Matcher m = Pattern.compile("\\\\u([0-9A-Fa-f]{4})").matcher(str);
        while (m.find()) {
            try {
                int cp = Integer.parseInt(m.group(1), 16);
                String rep = "";
                // Replace invisible control characters and unused code points
                switch (Character.getType(cp))
                {
                    case Character.CONTROL:     // \p{Cc}
                    case Character.FORMAT:      // \p{Cf}
                    case Character.PRIVATE_USE: // \p{Co}
                    case Character.SURROGATE:   // \p{Cs}
                    case Character.UNASSIGNED:  // \p{Cn}
                        m.appendReplacement(buf, rep);
                        break;
                    default:
                        char[] chars = Character.toChars(cp);
                        rep = new String(chars);
                        m.appendReplacement(buf, rep);
                        break;
                }

            } catch (NumberFormatException e) {
                System.err.println("Confused: " + e);
            }
        }
        m.appendTail(buf);
        str = buf.toString();
        return str;
    }

    public static boolean removeTriggersOfTable(GeoPackage gpkg, String tableName) throws Exception {
        boolean exec=false;
        if(!gpkg.getDatabase().hasRTreeEnabled()) {

            String stmt = MessageFormat.format(GpkgTriggers.SELECT_ALL_SPATIAL_TRIGGERS_TO_ONE_TABLE, tableName);

            ICursor c = gpkg.getDatabase().doRawQuery(stmt);

            while (c.moveToNext()) {
                String triggerName = c.getString(0);
                if(!triggerName.isEmpty()) {
                    String dropTrigger = MessageFormat.format(GpkgTriggers.DROP_SPATIAL_TRIGGERS, triggerName);
                    gpkg.getDatabase().execSQL(dropTrigger);
                    exec=true;
                }
            }
            c.close();
        }

        return exec;
    }

    /**
     * Get all pictures associated to one Feature using their ID.
     * @param context, The application context.
     * @param layer, The vector layer that contains the Feature.
     * @param featureID, The identity of the Feature.
     * @return A HashMap that contains the key and binary data to the pictures. The binary data are divide in thumbnail binary data and binary data in display size.
     * The structure contents: mediaIdentify=>{binaryData:{thumbnailBinary, displayBinary}}.
     * @throws TerraMobileException
     */
    public static Map<String, Object> getPhotosFromDatabase(Context context, GpkgLayer layer, long featureID) throws TerraMobileException, InvalidAppConfigException, DAOException {
        Map<String, Object> images;
        try{
            images = MediaService.getMedias(context, layer.getGeoPackage(), layer.getMediaTable(), featureID);
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
    private static ArrayList<String> getStoredImagesFromForm(Bundle formData) {
        ArrayList<String> imageIds=null;

        if(formData.containsKey(FormUtilities.DATABASE_IMAGE_IDS)) {
            imageIds = formData.getStringArrayList(FormUtilities.DATABASE_IMAGE_IDS);
        }
        return imageIds;
    }

    /**
     * Get Array of the byte[] loaded from system files using the paths registered into the Form.
     * This images are new images acquired.
     * @param formData, the form
     * @return list in memory of the images in binary format
     */
    private static ArrayList<String> getNewImagesFromForm(Bundle formData) {

        if(formData.containsKey(FormUtilities.INSERTED_IMAGE_PATHS)) {
            return formData.getStringArrayList(FormUtilities.INSERTED_IMAGE_PATHS);
            /*
            ArrayList<String> imagePaths = formData.getStringArrayList(FormUtilities.INSERTED_IMAGE_PATHS);

            if(imagePaths==null || imagePaths.isEmpty()) return null;

            images = new ArrayList<Object>(imagePaths.size());
            for (String path : imagePaths) {
                if (ImageUtilities.isImagePath(path)) {
                    image = ImageUtilities.getImageFromPath(path, 2);
                    images.add(image);
                }
            }*/
        }
        return null;
    }

    /**
     * Create a Feature as from form data.
     * @param formData, The Feature's data provided from the gathering form.
     * @param tv, The Treeview Controller reference.
     * @return A Feature.
     * @throws JSONException
     * @throws TerraMobileException
     */
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

        if(formKeys==null || formTypes==null) {
            throw new TerraMobileException("Attributes form are missing.");
        }

        // adding control attribute into form key list
        String statusKey;
        String objIdKey;
        int statusValue;
        try {
            statusKey = ResourceHelper.getStringResource(R.string.point_status_column);
            objIdKey = ResourceHelper.getStringResource(R.string.point_obj_id_column);
            String objIdValue="";
            if(formData.containsKey(FormUtilities.ATTR_DATA_VALUES)) {
                Bundle existingFeatureData = formData.getBundle(FormUtilities.ATTR_DATA_VALUES);
                if(existingFeatureData!=null)
                    objIdValue = existingFeatureData.getString(objIdKey);
            }

            if(objIdValue!=null && !objIdValue.isEmpty()) {// Update the feature that was loaded from official database
                statusValue = ResourceHelper.getIntResource(R.integer.point_status_changed);
            }else{
                statusValue = ResourceHelper.getIntResource(R.integer.point_status_unchanged);
            }

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            statusKey = "tm_status";
            statusValue=0;// default status, unchanged
        }
        attrs[ft.indexOf(statusKey)]=statusValue;

        for (int i = 0, len = formKeys.size(); i < len; i++) {
            String key = formKeys.get(i);
            GpkgField field = getFieldByName(fields, key);
            if(field==null) continue;
            String dbType = field.getFieldType();

            dbType = EditableLayerService.mappingAffinityDBType(dbType);

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
                if( !key.equals(feature.getFeatureType().getGeometryDescriptor().getName().toString())) {
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

        String affinityDBType;
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

    /**
     * Search a field in list of fields using own field name.
     * @param fields, The list of Fields.
     * @param name, The name of the field for find.
     * @return The found field, or null.
     */
    private static GpkgField getFieldByName(ArrayList<GpkgField> fields, String name) {
        GpkgField field=null;
        if(fields!=null) {
            for (int i = 0, len = fields.size(); i < len; i++) {
                field = fields.get(i);
                String fieldName=field.getFieldName();
                if(name.equals(fieldName)) return field;
            }
        }
        return field;
    }
}
