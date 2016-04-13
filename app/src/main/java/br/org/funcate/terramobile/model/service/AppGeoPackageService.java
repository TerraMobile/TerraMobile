package br.org.funcate.terramobile.model.service;

/**
 * Created by Andre Carvalho on 29/04/15.
 */

import android.content.Context;
import android.util.Log;

import com.augtech.geoapi.feature.SimpleFeatureImpl;
import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.org.funcate.extended.model.TMConfigEditableLayer;
import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.InvalidGeopackageException;
import br.org.funcate.terramobile.model.exception.LowMemoryException;
import br.org.funcate.terramobile.model.exception.ProjectException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.model.exception.StyleException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.geomsource.SFSLayer;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.osmbonuspack.overlays.SFSEditableMarker;
import br.org.funcate.terramobile.util.ResourceHelper;
import br.org.funcate.terramobile.util.Util;

public class AppGeoPackageService {

    private AppGeoPackageService()
    {

    }

    /**
     * This method reads the layers metadata from the GeoPackage contents and convert it to GpkgLayer format.
     * @return ArrayList<GpkgLayer> listLayers, the list Layers
     * @throws Exception
     */
    public static ArrayList<GpkgLayer> getLayers(Project project, Context context) throws InvalidGeopackageException, QueryException, InvalidAppConfigException {



        GeoPackage gpkg = null;

        if(project!=null) {
            try {

                gpkg = GeoPackageService.readGPKG(context, project.getFilePath());
            } catch (Exception e) {
                throw new InvalidGeopackageException("Invalid GeoPackage file.");
            }
        }else {
            Log.i("getLayers", "Project not found");
            return null;
        }

        if(!gpkg.isGPKGValid(false))
        {
            throw new InvalidGeopackageException("Invalid GeoPackage file.");
        }

        ArrayList<ArrayList<GpkgField>> layersList = GeoPackageService.getGpkgFieldsContents(gpkg, null, "");
        TMConfigEditableLayer tmConfigEditableLayer = null;
        try {
            tmConfigEditableLayer = EditableLayerService.getTMConfigEditableLayer(context, gpkg);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        ArrayList<GpkgLayer> listLayers=new ArrayList<GpkgLayer>();
        GpkgLayer layer;

        for (int i = 0,size = layersList.size(); i < size; i++) {

            ArrayList<GpkgField> aField = layersList.get(i);
            layer=new GpkgLayer(gpkg);// set geoPackage reference in this layer

            if(aField.size()!=10)
            {
                throw new InvalidGeopackageException("Invalid number of field on GPKG content table. ");
            }

            layer.setPosition(i);

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
                if(tmConfigEditableLayer==null || !tmConfigEditableLayer.isEditable(layerName)) {
                    layer.setType(GpkgLayer.Type.FEATURES);
                    layer.setFeatureType(GeoPackageService.getLayerFeatureType(gpkg, layerName));
                }else {
                    layer.setType(GpkgLayer.Type.EDITABLE);
                    layer.setJSON(tmConfigEditableLayer.getJSONConfig(layerName));
                    layer.setFields(GeoPackageService.getLayerFields(gpkg, layerName));
                    layer.setFeatureType(GeoPackageService.getLayerFeatureType(gpkg, layerName));
                    layer.setMediaTable(tmConfigEditableLayer.getMediaTableConfig(layerName));
                    layer.setPosition(0);
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

    private static SimpleFeature getSimpleFeatureInstance(GpkgLayer layer, String featureID) {
        SimpleFeatureType ft = layer.getFeatureType();
        SimpleFeatureImpl feature = new SimpleFeatureImpl(featureID, null, ft);
        return feature;
    }


    public static SFSLayer getFeatures(GpkgLayer layer) throws InvalidAppConfigException, LowMemoryException, TerraMobileException {

        try {

            List<SimpleFeature> features = GeoPackageService.getGeometries(layer.getGeoPackage(),layer.getName(), layer.defaultFilter(), null);

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

        String statusKey;
        try {

            statusKey = ResourceHelper.getStringResource(R.string.point_status_column);
            attrs[ft.indexOf(statusKey)]=ResourceHelper.getIntResource(R.integer.point_status_changed);

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.feature_set_status_exception));
        }

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

    public static boolean setRemovedFeature(GpkgLayer layer, SimpleFeature feature) throws InvalidAppConfigException, LowMemoryException, TerraMobileException {

        if(!(feature instanceof SimpleFeatureImpl)) return false;

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


    /**
     * Copy a originalGeoPackage to uploadGeoPackage, keep all changed and new features in new package.
     * Removes all TILED layers, all non editable layers and editable layers with no changed or new data.
     * In originalGeoPackage, mark the features with send status and remove the features marked as removed.
     * @param context, reference to application context
     * @param project, The current project
     * @param ExportLayers, The list os gathering layers from which read the gathering features.
     * @return The name and location of the Geopackage file.
     * @throws InvalidAppConfigException
     * @throws TerraMobileException
     */
    public static String createGeopackageForUpload(Context context, Project project, ArrayList<GpkgLayer> ExportLayers)
            throws InvalidAppConfigException, TerraMobileException, StyleException {

        if(ExportLayers==null || ExportLayers.isEmpty()) {
            // nothing to export, done
            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.no_layer_to_send_exception));
        }


        String tempGPKGName = null;
        try {
            tempGPKGName = ProjectsService.getUploadFilePath(context, project);
        } catch (ProjectException e) {
            e.printStackTrace();
            throw new TerraMobileException(e.getMessage());
        }

        // Create a GeoPackage to UPLOAD
        Util.copyFile(project.getFilePath(), tempGPKGName);

        if(!(new File(tempGPKGName)).exists()) {
            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.build_upload_gpkg_exception));
        }

        // load all layers from originalGeoPackage
        ArrayList<GpkgLayer> gpkgLayers;
        try {
            gpkgLayers = AppGeoPackageService.getLayers(project, context);
        }catch (QueryException e) {
            e.printStackTrace();
            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.build_upload_gpkg_exception));
        } catch (InvalidGeopackageException e) {
            e.printStackTrace();
            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.build_upload_gpkg_exception));
        }

        GeoPackage uploadGeoPackage = GeoPackageService.readGPKG(context, tempGPKGName);
        if(uploadGeoPackage==null) {
            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.build_upload_gpkg_exception));
        }

        if(gpkgLayers==null) {
            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.build_upload_gpkg_exception));
        }

        // remove gathering layers from the list layers reading from originalGeoPackage
        //gpkgLayers.removeAll(ExportLayers);
        ArrayList<GpkgLayer> gpkgLayersAux = new ArrayList<GpkgLayer>();
        for (GpkgLayer l : gpkgLayers) {
            boolean exist=false;
            for (GpkgLayer el : ExportLayers) {
                if (l.getName().equals(el.getName())) {
                    exist=true;
                    break;
                }
            }
            if(!exist) gpkgLayersAux.add(l);
        }

        for (GpkgLayer l : gpkgLayersAux) {

            if (GpkgLayer.Type.EDITABLE.equals(l.getType()) || GpkgLayer.Type.FEATURES.equals(l.getType())) {
                StyleService.deleteReferenceByLayer(context, tempGPKGName, l);
                LayersService.deleteReferenceByLayer(context, tempGPKGName, l);
                LayerFormService.deleteReferenceByLayer(context, tempGPKGName, l);
            }
            else
            {
                LayersService.deleteReferenceByLayer(context, tempGPKGName, l);
            }

            String tableName = l.getName();
            if (!uploadGeoPackage.dropTable(tableName)) {
                // drop upload package
                uploadGeoPackage.close();
                if (GeoPackageService.dropGPKG(tempGPKGName)) {
                    throw new TerraMobileException(ResourceHelper.getStringResource(R.string.build_upload_gpkg_exception));
                } else {
                    throw new TerraMobileException(ResourceHelper.getStringResource(R.string.drop_gpkg_exception));
                }
            }

            String mediaTableName = l.getMediaTable();
            if(mediaTableName!=null && !mediaTableName.isEmpty()) {
                try {
                    if (!MediaService.dropTable(context, uploadGeoPackage, mediaTableName)) {
                        throw new TerraMobileException(ResourceHelper.getStringResource(R.string.build_upload_gpkg_exception));
                    }
                }catch (DAOException e) {
                    e.printStackTrace();
                    throw new TerraMobileException(ResourceHelper.getStringResource(R.string.build_upload_gpkg_exception));
                }
            }
        }

        GeoPackage originalGPKG = null;
        ArrayList<GpkgLayer> layerHasData=new ArrayList<GpkgLayer>();
        ArrayList<String[]> layerStmts=new ArrayList<String[]>();
        String[] originalStatements = new String[2];
        String[] uploadStatements = new String[ExportLayers.size()];

        for (int i=0, o=0, u=0; i < ExportLayers.size(); i++) {
            GpkgLayer layer = ExportLayers.get(i);

            if(layer!=null) {
                if (originalGPKG == null) {
                    originalGPKG = layer.getGeoPackage();
                }

                try {

                    List<SimpleFeature> features = layer.getGeoPackage().getFeatures(layer.getName(), layer.toSendFilter());
                    if (features.size() > 0) {

                        layerHasData.add(layer);

                        // -------------------------------------------------------------------------------------------
                        // Make SQL script to apply changes in original GeoPackage package.
                        StringBuffer sqlStmt = new StringBuffer();

                        // remove all features is that tm_status is equal 2. See the file "gatheringconfig.xml" to more info.
                        sqlStmt.append("DELETE FROM [" + layer.getName() + "] ");
                        sqlStmt.append("WHERE " + layer.toRemoveFilter());
                        originalStatements[0] = sqlStmt.toString();

                        sqlStmt = new StringBuffer();

                        // set all sent features as send status (tm_status=3). See the file "gatheringconfig.xml" to more info.
                        sqlStmt.append("UPDATE [" + layer.getName() + "] SET " + layer.statementToSetSend() + " ");
                        sqlStmt.append("WHERE " + layer.toSendFilter());
                        originalStatements[1] = sqlStmt.toString();
                        layerStmts.add(originalStatements);
                        // end script SQL block.
                        // -------------------------------------------------------------------------------------------

                        sqlStmt = new StringBuffer();

                        // remove all unchanged features from uploadGeoPackage (obj_id is not null and tm_status=0). See the file "gatheringconfig.xml" to more info.
                        sqlStmt.append("DELETE FROM [" + layer.getName() + "] ");
                        sqlStmt.append("WHERE " + layer.toUnchangeFilter());
                        uploadStatements[u] = sqlStmt.toString();
                        // end script SQL block.
                        // -------------------------------------------------------------------------------------------

                        o+=2;
                        u++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // drop upload package
                    if (GeoPackageService.dropGPKG(tempGPKGName)) {
                        throw new TerraMobileException(ResourceHelper.getStringResource(R.string.build_upload_gpkg_exception));
                    } else {
                        throw new TerraMobileException(ResourceHelper.getStringResource(R.string.drop_gpkg_exception));
                    }
                }
            }
        }

        if(originalGPKG!=null && !layerHasData.isEmpty()) {

            if(!GeoPackageService.execStatements(uploadGeoPackage, uploadStatements)) {
                if(GeoPackageService.dropGPKG(tempGPKGName)) {
                    throw new TerraMobileException(ResourceHelper.getStringResource(R.string.update_fail_upload_gpkg_exception));
                }else {
                    throw new TerraMobileException(ResourceHelper.getStringResource(R.string.drop_gpkg_exception));
                }
            }

            // -------------------------------------------------------------------------------------------
            // Change the project_status key in tm_settings to indicate that data content was changed and uploaded to server.
            Setting setting;
            try {
                setting = SettingsService.get(context, ResourceHelper.getStringResource(R.string.project_status), project.getFilePath());
                setting.setValue("" + Project.UPLOAD);

                if (!SettingsService.update(context, setting, tempGPKGName)) {
                    // drop upload package
                    if (GeoPackageService.dropGPKG(tempGPKGName)) {
                        throw new TerraMobileException(ResourceHelper.getStringResource(R.string.build_upload_gpkg_exception));
                    } else {
                        throw new TerraMobileException(ResourceHelper.getStringResource(R.string.drop_gpkg_exception));
                    }
                }
            } catch (SettingsException e) {
                e.printStackTrace();
                // drop upload package
                if (GeoPackageService.dropGPKG(tempGPKGName)) {
                    throw new TerraMobileException(ResourceHelper.getStringResource(R.string.build_upload_gpkg_exception));
                } else {
                    throw new TerraMobileException(ResourceHelper.getStringResource(R.string.drop_gpkg_exception));
                }
            }
            // -------------------------------------------------------------------------------------------

            int i=0;
            for (GpkgLayer layer : layerHasData) {
                layer.setModified(false);
                try {

                    String[] stmts = layerStmts.get(i);
                    if(!GeoPackageService.execStatements(originalGPKG, stmts)) {
                        if(GeoPackageService.dropGPKG(tempGPKGName)) {
                            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.update_fail_original_gpkg_exception));
                        }else {
                            throw new TerraMobileException(ResourceHelper.getStringResource(R.string.drop_gpkg_exception));
                        }
                    }else{
                        if(!LayersService.updateModified(context, project, layer)) {
                            break;
                        }
                    }
                } catch (SettingsException e) {
                    e.printStackTrace();
                }
            }

            GeoPackageService.execVacuum(uploadGeoPackage);

            return tempGPKGName;
        }else {
            // drop upload package
            if (GeoPackageService.dropGPKG(tempGPKGName)) {
                throw new TerraMobileException(ResourceHelper.getStringResource(R.string.no_data_to_send_exception));
            } else {
                throw new TerraMobileException(ResourceHelper.getStringResource(R.string.drop_gpkg_exception));
            }
        }
    }

    public static boolean uploadPackageExists(Context context, Project project) throws InvalidAppConfigException, ProjectException {

        String uploadGPKGName = ProjectsService.getUploadFilePath(context, project);
        if ((new File(uploadGPKGName)).exists()) {
            return true;
        }
        return false;
    }

}
