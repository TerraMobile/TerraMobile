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

import java.util.ArrayList;
import java.util.List;

import br.org.funcate.extended.model.TMConfigEditableLayer;
import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.InvalidGeopackageException;
import br.org.funcate.terramobile.model.exception.LowMemoryException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.geomsource.SFSLayer;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.osmbonuspack.overlays.SFSEditableMarker;
import br.org.funcate.terramobile.util.ResourceHelper;

public class AppGeoPackageService {

    private AppGeoPackageService()
    {

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
