package br.org.funcate.terramobile.model.tilesource;

/**
 * Created by Andre Carvalho on 29/04/15.
 */
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.augtech.geoapi.feature.SimpleFeatureImpl;
import com.augtech.geoapi.geometry.BoundingBoxImpl;
import com.augtech.geoapi.geopackage.DateUtil;
import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.geometry.BoundingBox;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.org.funcate.dynamicforms.FormUtilities;
import br.org.funcate.dynamicforms.images.ImageUtilities;
import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.extended.model.TMConfigEditableLayer;
import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.controller.activity.TreeView;
import br.org.funcate.terramobile.model.Project;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.InvalidGeopackageException;
import br.org.funcate.terramobile.model.exception.LowMemoryException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.geomsource.SFSLayer;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
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
    public static ArrayList<GpkgLayer> getLayers(Context context) throws InvalidGeopackageException, QueryException {

        Project prj=((MainActivity) context).getProject();

        GeoPackage gpkg = null;

        if(prj!=null)
             gpkg = GeoPackageService.readGPKG(context, prj.getFilePath());
        else {
            Log.i("getLayers", "Project not found");
            return null;
        }

        if(!gpkg.isGPKGValid(false))
        {
            throw new InvalidGeopackageException("Invalid GeoPackage file.");
        }

        ArrayList<ArrayList<GpkgField>> fields = GeoPackageService.getGpkgFieldsContents(gpkg, null, "");
        TMConfigEditableLayer tmConfigEditableLayer = GeoPackageService.getTMConfigEditableLayer(gpkg);
        ArrayList<GpkgLayer> listLayers=new ArrayList<GpkgLayer>();
        GpkgLayer layer;

        for (int i = 0,size = fields.size(); i < size; i++) {

            ArrayList<GpkgField> aField = fields.get(i);
            layer=new GpkgLayer(gpkg);// set geoPackage reference in this layer

            if(aField.size()!=10)
            {
                throw new InvalidGeopackageException("Invalid number of field on GPKG content table. ");
            }

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
                if(!tmConfigEditableLayer.isEditable(layerName))
                    layer.setType(GpkgLayer.Type.FEATURES);
                else {
                    layer.setType(GpkgLayer.Type.EDITABLE);
                    layer.setJSON(tmConfigEditableLayer.getConfig(layerName));
                    layer.setFields(GeoPackageService.getLayerFields(gpkg, layerName));
                    layer.setFeatureType(GeoPackageService.getLayerFeatureType(gpkg, layerName));
                }
            } else if("tiles".equals(dataTypeField.getValue()))
            {
                layer.setType(GpkgLayer.Type.TILES);
            } else
            {
                //TODO:Verify if it's necessary to stop the process or ignore the current layer iteration
                throw new InvalidGeopackageException("Invalid layer .");
            }

            BoundingBox bb =new BoundingBoxImpl(minX, maxX, minY, maxY);
            layer.setBox(bb);
            listLayers.add(layer);
        }
        gpkg.close();

        return listLayers;
    }

    public static void createGeoPackageTileSourceOverlay(GpkgLayer layer, Context context) {

        MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        mapView.setMaxZoomLevel(18);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);


        System.out.println("Overlay size:" + mapView.getOverlayManager().size());

/*        OnlineTileSourceBase mapQuestTileSource = TileSourceFactory.MAPQUESTOSM;
        String tileSourcePath = mapQuestTileSource.OSMDROID_PATH.getAbsolutePath() + "/";*/

        final MapTileProviderBasic tileProvider = new MapTileProviderBasic(context);

        final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", new String[] {"http://tile.openstreetmap.org/"});

        MapTileModuleProviderBase moduleProvider = new MapTileGeoPackageProvider(tileSource, layer.getName(), layer.getGeoPackage());
        SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(context);
        MapTileProviderArray tileProviderArray = new MapTileProviderArray(tileSource, simpleReceiver, new MapTileModuleProviderBase[] { moduleProvider });

/*        tileProvider.setTileSource(tileSource);*/
        final TilesOverlay tilesOverlay = new TilesOverlay(tileProviderArray, context);
        tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        mapView.getOverlays().add(tilesOverlay);

        //mapView.getOverlayManager().overlaysReversed();
        //mapView.getTileProvider().clearTileCache();
        tileProvider.setTileRequestCompleteHandler(new SimpleInvalidationHandler(mapView));
        mapView.setTileSource(tileSource);
        mapView.setUseDataConnection(false); //  letting osmdroid know you would use it in offline mode, keeps the mapView from loading online tiles using network connection.*/
        mapView.invalidate();
    }

    public static void storeData(Context context, Bundle formData) throws TerraMobileException, QueryException {
        ArrayList<String> keys = formData.getStringArrayList(LibraryConstants.FORM_KEYS);
        TreeView tv = ((MainActivity)context).getTreeView();
        if(keys==null || keys.isEmpty()){
            throw new TerraMobileException(context.getString(R.string.missing_form_data));
        }else {
            try {
                SimpleFeature feature = makeSimpleFeature(formData, tv);
                GeoPackageService.writeLayerFeature(tv.getSelectedEditableLayer().getGeoPackage(), feature);
            }catch (Exception e) {
                int flags = context.getApplicationInfo().flags;
                if((flags & context.getApplicationInfo().FLAG_DEBUGGABLE) != 0) {
                    throw new TerraMobileException(e.getMessage());
                }else {
                    throw new TerraMobileException(context.getString(R.string.error_while_storing_form_data));
                }
            }
        }
    }

    private static SimpleFeature makeSimpleFeature(Bundle formData, TreeView tv) {


        ArrayList<GpkgField> fields = tv.getSelectedEditableLayer().getFields();
        SimpleFeatureType ft = tv.getSelectedEditableLayer().getFeatureType();
        GeometryType geometryType = ft.getGeometryDescriptor().getType();
        List<Object> attributeValues = new ArrayList<Object>();
        SimpleFeatureImpl feature = new SimpleFeatureImpl(null, attributeValues, ft);

        GeometryFactory factory=new GeometryFactory();

        if(geometryType.getName().toString().equalsIgnoreCase(FormUtilities.GEOJSON_TYPE_POINT) &&
                formData.getString(FormUtilities.GEOJSON_TAG_TYPE).equalsIgnoreCase(FormUtilities.GEOJSON_TYPE_POINT)) {
            double[] c = formData.getDoubleArray(FormUtilities.GEOJSON_TYPE_POINT);
            Coordinate coordinate = new Coordinate(c[0],c[1]);
            Point point = factory.createPoint(coordinate);
            feature.setDefaultGeometry(point);
        }

        ArrayList<String> formKeys = formData.getStringArrayList(LibraryConstants.FORM_KEYS);
        ArrayList<String> formTypes = formData.getStringArrayList(LibraryConstants.FORM_TYPES);

        for (int i = 0, len = formKeys.size(); i < len; i++) {
            String key = formKeys.get(i);
            GpkgField field = getFieldByName(fields, key);
            if(field==null) continue;
            String dbType = field.getFieldType();
            String formType = formTypes.get(i);

            if ("DOUBLE".equalsIgnoreCase(dbType)) {
                Double d = formData.getDouble(key);
                feature.setAttribute(key,d);
            } else if ("TEXT".equalsIgnoreCase(dbType)) {
                String s = formData.getString(key);
                feature.setAttribute(key,s);
            } else if ("INTEGER".equalsIgnoreCase(dbType)) {

                if(formType.equalsIgnoreCase(dbType)) {
                    Integer in = formData.getInt(key);
                    feature.setAttribute(key, in);
                }else {
                    Boolean b = formData.getBoolean(key);
                    feature.setAttribute(key, b);
                }
            } else if ("BLOB".equalsIgnoreCase(dbType)) {
                if( !key.equals(feature.getFeatureType().getGeometryDescriptor().getName())) {
                    String path = formData.getString(key);
                    if (path!=null && ImageUtilities.isImagePath(path)) {
                        byte[] blob = ImageUtilities.getImageFromPath(path, 1);
                        feature.setAttribute(key, blob);
                    }else{
                        feature.setAttribute(key,null);
                    }
                }
            } else if ("DATE".equalsIgnoreCase(dbType)) {
                String date = formData.getString(key);
                Date dt = DateUtil.deserializeDate(date);
                if (dt == null) dt = new Date();
                feature.setAttribute(key,dt);
            } else if ("TIME".equalsIgnoreCase(dbType)) {
                String date = formData.getString(key);
                if (!date.equals("")) {
                    date = "0000-00-00T" + date;
                }
                Time dt = DateUtil.deserializeSqlTime(date);
                if (dt == null) {
                    dt = new Time(Long.decode(date));
                }
                feature.setAttribute(key,dt);
            } else if ("DATETIME".equalsIgnoreCase(dbType)) {
                String date = formData.getString(key);
                Date dt = DateUtil.deserializeDateTime(date);
                if (dt == null) dt = new Date();
                feature.setAttribute(key,dt);
            }
        }
        return feature;
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

            SFSLayer l = new SFSLayer(features);

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

}
