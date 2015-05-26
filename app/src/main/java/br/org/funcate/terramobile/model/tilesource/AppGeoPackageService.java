package br.org.funcate.terramobile.model.tilesource;

/**
 * Created by Andre Carvalho on 29/04/15.
 */
import android.content.Context;

import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;

import java.io.File;
import java.util.ArrayList;

import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.gpkg.objects.AppLayer;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.util.ResourceUtil;

public class AppGeoPackageService {

    private Context context;
    private String gpkgFilePath;

    public AppGeoPackageService(Context context) {
        this.context=context;
        File appPath = ResourceUtil.getDirectory(context.getResources().getString(R.string.app_workspace_dir));

        // ------------------------------------------------------------------------
        // TODO: alter temporary file name to dynamic file name as from user action when him acquire online GeoPackege from one server.
        //gpkgFilePath=this.context.getCurrentGeoPackageName();
        gpkgFilePath=appPath+"/inpe_geoeye_2013_mosaico.gpkg";
        // ------------------------------------------------------------------------
    }

    /**
     * This method reads the layer names from GeoPackage.
     * @return ArrayList<GpkgLayer> listLayers, the list Layers
     * @throws Exception
     */
    public ArrayList<GpkgLayer> getLayers() throws Exception {

        GeoPackage gpkg = GeoPackageService.readGPKG(context, gpkgFilePath);
        if(!gpkg.isGPKGValid(true))
        {
            throw new Exception("Invalid GeoPackage file.");
        }

        String[] columns = new String[2];
        columns[0]="table_name";
        columns[1]="data_type";

        ArrayList<ArrayList<GpkgField>> fields;
        fields = GeoPackageService.getGpkgFieldsContents(gpkg,columns);
        ArrayList<GpkgLayer> listLayers=new ArrayList<GpkgLayer>();
        GpkgLayer layer;

        for (int i = 0,size = fields.size(); i < size; i++) {

            ArrayList<GpkgField> aField = fields.get(i);
            layer=new GpkgLayer(gpkg);// set geoPackage reference in this layer

            for (int j = 0,len = aField.size(); j < len; j++) {

                GpkgField field = aField.get(j);

                if(field.getFieldName().equals(columns[0]))
                    layer.setLayerName((String) field.getValue());
                else {
                    if("features".equals(field.getValue())){
                        layer.setLayerType(AppLayer.FEATURES);
                    }else if("tiles".equals(field.getValue())){
                        layer.setLayerType(AppLayer.TILES);
                    }
                }
            }
            listLayers.add(layer);
        }
        gpkg.close();
        return listLayers;
    }
/*
    public ArrayList<String> getAttributes() {

        return null;
    }*/

}
