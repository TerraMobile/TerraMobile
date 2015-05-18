package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.augtech.geoapi.geopackage.GeoPackage;

import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.util.List;

import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.util.ResourceUtil;
import br.org.funcate.terramobile.view.TerraMobileMenuToolItem;

/**
 * Created by Andre Carvalho on 27/04/15.
 */
public class MenuToolController implements View.OnClickListener {

    private TerraMobileMenuToolItem menuToolItem;
    private Context context;
    private File appPath;
    private String tempURL;

    public MenuToolController(Context context, TerraMobileMenuToolItem menuToolItem) {
        this.menuToolItem=menuToolItem;
        this.context=context;
        initResources();
    }

    public MenuToolController(Context context) {
        this.menuToolItem=null;
        this.context=context;
        initResources();
    }

    private void initResources() {
        appPath = ResourceUtil.getDirectory(context.getResources().getString(R.string.app_workspace_dir));
        tempURL = context.getResources().getString(R.string.gpkg_url);
    }

    @Override
    public void onClick(View v) {

        selectedItem(v);
        exec();
        Toast.makeText(context, "Tool : " + this.menuToolItem.getLabel(),
                Toast.LENGTH_SHORT).show();
        return;
    }

    private void selectedItem(View v) {
        try {
            if (v.isSelected()) {
                v.setSelected(false);
                v.setBackgroundColor(Color.BLACK);
                ((TextView) v).setTextColor(Color.WHITE);
            } else {
                v.setSelected(true);
                v.setBackgroundColor(Color.WHITE);
                ((TextView) v).setTextColor(Color.BLACK);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void exec() {
        switch (this.menuToolItem.getType()) {
            case TerraMobileMenuToolItem.DOWNLOAD_GPKG: {//Download GeoPackage
                //downloadGeoPackage();
                break;
            }
            case TerraMobileMenuToolItem.CREATE_GPKG:{//Create GeoPackage
                createGeoPackage();
                break;
            }
            case TerraMobileMenuToolItem.READ_GEOM:{//Read Geometries
                readGeometries();
                break;
            }
            case TerraMobileMenuToolItem.INSERT_DATA:{// Insert Data
                insertData();
                break;
            }
        }

    }

    public void readGeometries() {
        try {
            GeoPackage gpkg = GeoPackageService.readGPKG(context,appPath.getPath()+"/test.gpkg");

            List<SimpleFeature> features = GeoPackageService.getGeometries(gpkg, "municipios_2005");

            Toast.makeText(context, ""+features.size()+" features on the file", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(context, "Error reading gpkg file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return;
    }

    public void createGeoPackage() {
        GeoPackageService.createGPKG(context, appPath.getPath() + "/test.gpkg");
        Toast.makeText(context, "GeoPackage file successfully created", Toast.LENGTH_SHORT).show();
    }

    public void insertData() {
        try {
            //GeoPackageService.insertDataGPKG(thisActivity,"/GeoPackageTest/test.gpkg");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error insert GML on device: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return;
    }

}
