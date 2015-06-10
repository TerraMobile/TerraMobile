package br.org.funcate.terramobile.model.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import br.org.funcate.terramobile.model.Settings;
import br.org.funcate.terramobile.model.db.DataBase;

/**
 * Created by marcelo on 5/26/15.
 */
public class SettingsDAO {
    private DataBase dataBase;

    public SettingsDAO(Context context) {
        this.dataBase = new DataBase(context);
    }

    public boolean insert(Settings settings) {
        try {
            SQLiteDatabase db = dataBase.getWritableDatabase();
            if (db != null) {
                if (settings != null) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("ID", settings.getId());
                    contentValues.put("USER_NAME", settings.getUserName());
                    contentValues.put("PASSWORD", settings.getPassword());
                    contentValues.put("URL", settings.getUrl());
                    if (db.insert("SETTINGS", null, contentValues) != -1) {
                        db.close();
                        return true;
                    }
                }
                db.close();
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Settings settings) {
        SQLiteDatabase db = dataBase.getWritableDatabase();
        try{
            if (db != null) {
                if (settings != null) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("ID", settings.getId());
                    contentValues.put("USER_NAME", settings.getUserName());
                    contentValues.put("PASSWORD", settings.getPassword());
                    contentValues.put("URL", settings.getUrl());
                    if (db.update("SETTINGS", contentValues, "ID=?", new String[]{String.valueOf(settings.getId())}) > 0) {
                        db.close();
                        return true;
                    }
                }
                db.close();
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Settings getById(long id) {
        try {
            SQLiteDatabase db = dataBase.getReadableDatabase();
            if(db != null) {
                Settings settings = null;
                Cursor cursor = db.query("SETTINGS", new String[]{"ID", "USER_NAME", "PASSWORD", "URL"}, "ID = ?", new String[]{String.valueOf(id)}, null, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    settings = new Settings();
                    settings.setId(cursor.getInt(0));
                    settings.setUserName(cursor.getString(1));
                    settings.setPassword(cursor.getString(2));
                    settings.setUrl(cursor.getString(3));
                    cursor.close();
                }
                db.close();
                return settings;
            }
            return null;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }
}