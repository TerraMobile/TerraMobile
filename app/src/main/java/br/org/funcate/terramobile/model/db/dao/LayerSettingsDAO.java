package br.org.funcate.terramobile.model.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.HashMap;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.db.DatabaseHelper;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.util.ResourceHelper;

public class LayerSettingsDAO {
    private DatabaseHelper database;
    private static final String TABLE_NAME="TM_LAYER_SETTINGS";

    public LayerSettingsDAO(DatabaseHelper database) throws InvalidAppConfigException, DAOException {
        if(database!=null)
        {
            this.database = database;
        }
        else
        {
            throw new DAOException(ResourceHelper.getStringResource(R.string.invalid_database_exception));
        }
    }


    public boolean update(String layerName, int visible, int position) throws InvalidAppConfigException, DAOException {

       SQLiteDatabase db = database.getWritableDatabase();
        try{
            if (db != null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("LAYER_NAME", layerName);
                contentValues.put("ENABLED", visible);
                contentValues.put("POSITION", position);
                if (db.update(TABLE_NAME, contentValues, "LAYER_NAME = ?", new String[]{layerName}) > 0) {
                    db.close();
                    return true;
                }
                db.close();
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.layer_settings_update_exception),e);
        }
    }

    public boolean updateModified(String layerName, boolean modified) throws InvalidAppConfigException, DAOException {

        SQLiteDatabase db = database.getWritableDatabase();
        try{
            if (db != null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("MODIFIED", modified);
                if (db.update(TABLE_NAME, contentValues, "LAYER_NAME = ?", new String[]{layerName}) > 0) {
                    db.close();
                    return true;
                }
                db.close();
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.layer_settings_update_exception),e);
        }
    }

       public boolean load(GpkgLayer layer) throws InvalidAppConfigException, DAOException {
        try {
            SQLiteDatabase db = database.getReadableDatabase();

            if(db != null) {

                Cursor cursor = db.query(TABLE_NAME, new String[]{"ENABLED","POSITION, MODIFIED"}, "LAYER_NAME = ?", new String[]{layer.getName()}, null, null, null, null);

                if (cursor != null && cursor.getCount() != 0) {

                    cursor.moveToFirst();

                    if(cursor.getInt(0)==1)
                    {
                        layer.setEnabled(true);
                    }
                    else
                    {
                        layer.setEnabled(false);
                    }
                    layer.setPosition(cursor.getInt(1));

                    if(cursor.getInt(2)==1)
                    {
                        layer.setModified(true);
                    }
                    else
                    {
                        layer.setModified(false);
                    }

                    cursor.close();

                    db.close();

                    return true;
                }
                else
                {
                    db.close();

                    return false;
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.layer_settings_query_exception),e);
        }
        return false;
    }

    private boolean insert(String layerName, int visible, int position, int modified) throws InvalidAppConfigException, DAOException {
        try {
            SQLiteDatabase db = database.getWritableDatabase();
            if (db != null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("LAYER_NAME", layerName);
                contentValues.put("ENABLED", visible);
                contentValues.put("POSITION", position);
                contentValues.put("MODIFIED", modified);
                if (db.insert(TABLE_NAME, null, contentValues) != -1) {
                    db.close();
                    return true;
                }
                db.close();
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.layer_settings_insert_exception),e);
        }
    }

    public boolean insertAll(ArrayList<GpkgLayer> layers) throws InvalidAppConfigException, DAOException {
        boolean success=true;
        for (int i = 0; i < layers.size(); i++) {
            success = insert(layers.get(i).getName(),layers.get(i).isEnabled()?1:0, layers.get(i).getPosition(), layers.get(i).isModified()?1:0);
            if(!success)
            {
                return false;
            }
        }
        return success;
    }

    public boolean delete(String layerName) throws InvalidAppConfigException, DAOException {
        try {
            SQLiteDatabase db = database.getWritableDatabase();
            if (db != null) {

                String[] args = {layerName};
                if (db.delete(TABLE_NAME, "LAYER_NAME=?", args) != -1) {
                    db.close();
                    return true;
                }
                db.close();
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.layer_setting_delete_exception),e);
        }
    }

    public boolean deleteAll() throws InvalidAppConfigException, DAOException {
        try {
            SQLiteDatabase db = database.getWritableDatabase();
            if (db != null) {

                if (db.delete(TABLE_NAME, null, null) != -1) {
                    db.close();
                    return true;
                }
                db.close();
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.layer_settings_insert_exception),e);
        }
    }

    public HashMap<String, String> get(String layerName) throws InvalidAppConfigException, DAOException {
        HashMap<String, String> settings = new HashMap<String, String>();

        try {
            SQLiteDatabase db = database.getReadableDatabase();

            if(db != null) {

                Cursor cursor = db.query(TABLE_NAME, new String[]{"ENABLED","POSITION, MODIFIED"}, "LAYER_NAME = ?", new String[]{layerName}, null, null, null, null);

                if (cursor != null && cursor.getCount() != 0) {

                    cursor.moveToFirst();

                    settings.put("enabled", Integer.toString(cursor.getInt(0)));
                    settings.put("position", Integer.toString(cursor.getInt(1)));
                    settings.put("modified", Integer.toString(cursor.getInt(2)));

                    cursor.close();

                    db.close();

                }
                else
                {
                    db.close();
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.layer_settings_query_exception),e);
        }
        return settings;
    }

    public boolean hasModifiedLayer() throws InvalidAppConfigException, DAOException {
        boolean hasModifiedLayer= false;

        try {
            SQLiteDatabase db = database.getReadableDatabase();

            if(db != null) {

                Cursor cursor = db.query(TABLE_NAME, new String[]{"ENABLED","POSITION, MODIFIED"}, "MODIFIED = ?", new String[]{"1"}, null, null, null, null);

                if (cursor != null && cursor.getCount() != 0) {
                    hasModifiedLayer=true;
                }
                else
                {
                    hasModifiedLayer=false;
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.layer_settings_query_exception),e);
        }
        return hasModifiedLayer;
    }
}