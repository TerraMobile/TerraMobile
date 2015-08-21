package br.org.funcate.terramobile.model.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.osmdroid.bonuspack.kml.Style;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.db.DatabaseHelper;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.util.ResourceHelper;

public class StyleDAO {
    private DatabaseHelper database;

    public StyleDAO(DatabaseHelper database) throws InvalidAppConfigException, DAOException {
        if(database!=null)
        {
            this.database = database;
        }
        else
        {
            throw new DAOException(ResourceHelper.getStringResource(R.string.invalid_database_exception));
        }
    }

    public boolean insert(String layerName, String sldXML) throws InvalidAppConfigException, DAOException {
        try {
            SQLiteDatabase db = database.getWritableDatabase();
            if (db != null) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("LAYER_NAME", layerName);
                    contentValues.put("SLD_XML", sldXML);
                    if (db.insert("TM_STYLE", null, contentValues) != -1) {
                        db.close();
                        return true;
                }
                db.close();
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.style_insert_exception),e);
        }
    }

    public boolean update(String layerName, String sldXML) throws InvalidAppConfigException, DAOException {

        SQLiteDatabase db = database.getWritableDatabase();
        try{
            if (db != null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("LAYER_NAME", layerName);
                contentValues.put("SLD_XML", sldXML);
                if (db.update("TM_STYLE", contentValues, "LAYER_NAME = ?", new String[]{layerName}) > 0) {
                    db.close();
                    return true;
                }
                db.close();
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.style_update_exception),e);
        }
    }

    public String get(String layerName) throws InvalidAppConfigException, DAOException {
        String sldXML = null;
        try {
            SQLiteDatabase db = database.getReadableDatabase();
            if(db != null) {
                Cursor cursor = db.query("TM_STYLE", new String[]{"SLD_XML"}, "LAYER_NAME = ?", new String[]{layerName}, null, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    sldXML = cursor.getString(0);
                    cursor.close();
                }
                else
                {
                    db.close();
                    return null;
                }
                db.close();
                return sldXML;
            }
            return null;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.style_query_exception),e);
        }
    }
}