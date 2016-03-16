package br.org.funcate.terramobile.model.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.db.DatabaseHelper;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.util.ResourceHelper;

/**
 * Created by marcelo on 5/26/15.
 */
public class SettingsDAO {
    private DatabaseHelper database;
    private static final String TABLE_NAME="TM_SETTINGS";

    public SettingsDAO(DatabaseHelper database) throws InvalidAppConfigException, DAOException {
        if(database!=null)
        {
            this.database = database;
        }
        else
        {
            throw new DAOException(ResourceHelper.getStringResource(R.string.invalid_database_exception));
        }
    }

    public boolean insert(Setting setting) throws InvalidAppConfigException, DAOException {
        try {
            SQLiteDatabase db = database.getWritableDatabase();
            if (db != null) {
                if (setting != null) {
                    ContentValues contentValues = new ContentValues();
//                    contentValues.put("ID", setting.getId()); AUTOINCREMENT
                    contentValues.put("KEY", setting.getKey());
                    contentValues.put("VALUE", setting.getValue());
                    if (db.insert(TABLE_NAME, null, contentValues) != -1) {
                        db.close();
                        return true;
                    }
                }
                db.close();
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.settings_insert_exception),e);
        }
    }

    public boolean update(Setting setting) throws InvalidAppConfigException, DAOException {

        String clause=null;
        String clauseValue=null;

        if(setting.getId()!=null)
        {
            clause = "ID = ?";
            clauseValue = Long.toString(setting.getId());
        } else if(setting.getKey()!=null)
        {
            clause = "KEY = ?";
            clauseValue = setting.getKey();
        }

        SQLiteDatabase db = database.getWritableDatabase();
        try{
            if (db != null) {
                if (setting != null) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("KEY", setting.getKey());
                    contentValues.put("VALUE", setting.getValue());
                    if (db.update(TABLE_NAME, contentValues, clause, new String[]{clauseValue}) > 0) {
                        db.close();
                        return true;
                    }
                }
                db.close();
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.settings_update_exception),e);
        }
    }
    @Deprecated
    public Setting getById(long id) throws InvalidAppConfigException, DAOException {
        try {
            SQLiteDatabase db = database.getReadableDatabase();
            if(db != null) {
                Setting setting = null;
                Cursor cursor = db.query(TABLE_NAME, new String[]{"ID", "KEY", "VALUE"}, "ID = ?", new String[]{String.valueOf(id)}, null, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    setting = new Setting(cursor.getLong(0), cursor.getString(1), cursor.getString(2));
                    cursor.close();
                }
                db.close();
                return setting;
            }
            return null;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.settings_query_exception),e);
        }
    }

    /**
     * Get settings by ID or by KEY in this order priority. Uses the same reference of the parameters
     * @param setting Setting id OR key to search (reference will be used as return if valid)
     * @return
     * @throws InvalidAppConfigException
     * @throws DAOException
     */
    public Setting get(Setting setting) throws InvalidAppConfigException, DAOException {

        String clause=null;
        String clauseValue=null;

        if(setting.getId()!=null)
        {
            clause = "ID = ?";
            clauseValue = Long.toString(setting.getId());
        } else if(setting.getKey()!=null)
        {
            clause = "KEY = ?";
            clauseValue = setting.getKey();
        }


        try {
            SQLiteDatabase db = database.getReadableDatabase();
            if(db != null) {
                Cursor cursor = db.query(TABLE_NAME, new String[]{"ID", "KEY", "VALUE"}, clause, new String[]{clauseValue}, null, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    setting = new Setting(cursor.getLong(0), cursor.getString(1), cursor.getString(2));
                    cursor.close();
                }
                else
                {
                    db.close();
                    return null;
                }
                db.close();
                return setting;
            }
            return null;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.settings_query_exception),e);
        }
    }
}