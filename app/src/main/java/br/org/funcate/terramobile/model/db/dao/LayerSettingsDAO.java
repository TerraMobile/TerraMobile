package br.org.funcate.terramobile.model.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

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
                contentValues.put("ENABLE", visible);
                contentValues.put("POSITION", position);
                if (db.update(TABLE_NAME, contentValues, "LAYER_NAME = ?", new String[]{layerName}) > 0) {
                    db.close();
                    return true;
                }
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.style_update_exception),e);
        }
        finally {
            if (db != null && db.isOpen())
            {
                db.close();
            }
        }
    }

    public boolean load(GpkgLayer layer) throws InvalidAppConfigException, DAOException {
        SQLiteDatabase db = database.getReadableDatabase();
        try {

            if(db != null) {

                Cursor cursor = db.query(TABLE_NAME, new String[]{"ENABLED","POSITION"}, "LAYER_NAME = ?", new String[]{layer.getName()}, null, null, null, null);

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
            throw new DAOException(ResourceHelper.getStringResource(R.string.style_query_exception),e);
        }
        finally {
            if (db != null && db.isOpen())
            {
                db.close();
            }
        }
        return false;
    }

    public boolean insert(String layerName, int visible, int position) throws InvalidAppConfigException, DAOException {
        SQLiteDatabase db = database.getWritableDatabase();
        try {
           if (db != null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("LAYER_NAME", layerName);
                contentValues.put("ENABLE", visible);
                contentValues.put("POSITION", position);
                if (db.insert(TABLE_NAME, null, contentValues) != -1) {
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
        finally {
            if (db != null && db.isOpen())
            {
                db.close();
            }
        }
    }
}