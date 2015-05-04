package br.org.funcate.jgpkg.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;

import android.content.Context;
import android.os.Environment;
import android.support.v4.util.ArrayMap;

import com.augtech.geoapi.geopackage.GeoPackage;
import br.org.funcate.geopackage.AndroidSQLDatabase;


import com.augtech.geoapi.geopackage.GpkgField;

import com.augtech.geoapi.geopackage.ICursor;
import com.augtech.geoapi.geopackage.ISQLDatabase;
import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgTable;
import com.augtech.geoapi.geopackage.table.GpkgContents;

public class GeoPackageService {

    static Logger log = Logger.getAnonymousLogger();

    private static GeoPackage connect(ISQLDatabase database, boolean overwrite) {

        log.log(Level.INFO, "Connecting to GeoPackage...");

        GeoPackage geoPackage = new GeoPackage(database, overwrite);

        // Quick test to get the current contents
        if (geoPackage != null) {
            int numExist = geoPackage.getUserTables(GpkgTable.TABLE_TYPE_FEATURES).size();
            log.log(Level.INFO, "" + numExist + " feature tables in the GeoPackage");

            numExist = geoPackage.getUserTables(GpkgTable.TABLE_TYPE_TILES).size();
            log.log(Level.INFO, "" + numExist + " tile tables in the GeoPackage");
        }
        return geoPackage;

    }





    public static GeoPackage readGPKG(Context context, String gpkgFilePath) throws Exception
    {
        AndroidSQLDatabase gpkgDB = new AndroidSQLDatabase(context, new File(gpkgFilePath));

        GeoPackage geoPackage = connect(gpkgDB, false);

        return geoPackage;

    }


    public static List<SimpleFeature> getGeometries(GeoPackage gpkg, String tableName) throws Exception
    {
        if(!gpkg.isGPKGValid(true))
        {
            throw new Exception("Invalid GeoPackage file.");
        }

        List<SimpleFeature> features = gpkg.getFeatures("focosqueimadas");

        return features;
    }

    public static List<SimpleFeature> getTiles(GeoPackage gpkg, String tableName) throws Exception
    {
        if(!gpkg.isGPKGValid(true))
        {
            throw new Exception("Invalid GeoPackage file.");
        }

        List<SimpleFeature> features = gpkg.getTiles(tableName,"");

        return features;
    }

    public static List<SimpleFeature> getTiles(GeoPackage gpkg, BoundingBox box, String tableName, int zoomLevel) throws Exception
    {
        if(!gpkg.isGPKGValid(true))
        {
            throw new Exception("Invalid GeoPackage file.");
        }

        List<SimpleFeature> features = gpkg.getTiles(tableName, box, zoomLevel);

        return features;
    }

    public static byte[] getTile(GeoPackage gpkg, String tableName, int col, int row, int zoomLevel) throws Exception
    {
        if(!gpkg.isGPKGValid(true))
        {
            throw new Exception("Invalid GeoPackage file.");
        }

        byte[] tile = gpkg.getTile(tableName, col, row, zoomLevel);

        return tile;
    }

    public static Map<String, Integer> getTileTableMaxMin(GeoPackage gpkg, String tableName, String tableType) throws Exception {
        if (!gpkg.isGPKGValid(true)) {
            throw new Exception("Invalid GeoPackage file.");
        }

        ICursor icursor = gpkg.getUserTable(tableName, tableType).query(gpkg, new String[]{"max(zoom_level), min(zoom_level), max(tile_row), min(tile_row), max(tile_column), min(tile_column)"}, "");

        Map<String, Integer> ranges = new HashMap<String, Integer>();

        if (icursor.moveToNext()) {
            int minZoomLevel = icursor.getInt(icursor.getColumnIndex("min(zoom_level)"));
            int maxZoomLevel = icursor.getInt(icursor.getColumnIndex("max(zoom_level)"));
            int minTileRow = icursor.getInt(icursor.getColumnIndex("min(tile_row)"));
            int maxTileRow = icursor.getInt(icursor.getColumnIndex("max(tile_row)"));
            int minTileColumn = icursor.getInt(icursor.getColumnIndex("min(tile_column)"));
            int maxTileColumn = icursor.getInt(icursor.getColumnIndex("max(tile_column)"));
            ranges.put("minZoomLevel", minZoomLevel);
            ranges.put("maxZoomLevel", maxZoomLevel);
            ranges.put("minTileRow", minTileRow);
            ranges.put("maxTileRow", maxTileRow);
            ranges.put("minTileColumn", minTileColumn);
            ranges.put("maxTileColumn", maxTileColumn);
        }


        return ranges;
    }

    public static GpkgTable getGpkgTable(GeoPackage gpkg, String gpkgTableName) throws Exception {

        GpkgTable systemTable = gpkg.getSystemTable(gpkgTableName);

        return systemTable;
    }

    public static GpkgContents getGpkgContents(GeoPackage gpkg) throws Exception {

        String gpkgTableName = "gpkg_contents";

        return (GpkgContents) getGpkgTable(gpkg,gpkgTableName);
    }

    public static ArrayList<ArrayList<GpkgField>> getGpkgFieldsContents(GeoPackage gpkg, String[] columns) throws Exception {

        GpkgContents contents = getGpkgContents(gpkg);
        ICursor c = contents.query(gpkg, columns, null);
        GpkgField field;
        int len= columns.length;
        ArrayList<ArrayList<GpkgField>> records=new ArrayList<ArrayList<GpkgField>>();

        while (c.moveToNext()){
            ArrayList<GpkgField> aRecord=new ArrayList<GpkgField>(len);
            for (int i = 0; i < len; i++) {
                field = contents.getField(columns[i]);
                field.setValue(c.getString(i));
                aRecord.add(field);
            }
            records.add(aRecord);
        }
        return records;
    }
}
