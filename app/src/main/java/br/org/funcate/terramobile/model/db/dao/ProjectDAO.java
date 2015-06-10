package br.org.funcate.terramobile.model.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import br.org.funcate.terramobile.model.Project;
import br.org.funcate.terramobile.model.db.DataBase;

/**
 * Created by marcelo on 5/26/15.
 */
public class ProjectDAO {
    private DataBase dataBase;

    public ProjectDAO(Context context) {
        this.dataBase = new DataBase(context);
    }

    public boolean insert(Project project) {
        try {
            SQLiteDatabase db = dataBase.getWritableDatabase();
            if (db != null) {
                if (project != null) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("ID", project.getId());
                    contentValues.put("CURRENT", project.getCurrent());
                    contentValues.put("FILE_PATH", project.getFilePath());
                    if (db.insert("PROJECT", null, contentValues) != -1) {
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

    public boolean update(Project project) {
        SQLiteDatabase db = dataBase.getWritableDatabase();
        try{
            if (db != null) {
                if (project != null) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("ID", project.getId());
                    contentValues.put("CURRENT", project.getCurrent());
                    contentValues.put("FILE_PATH", project.getFilePath());
                    if (db.update("PROJECT", contentValues, "ID=?", new String[]{String.valueOf(project.getId())}) > 0) {
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

    public Project getByCurrent(String current) {
        try {
            SQLiteDatabase db = dataBase.getReadableDatabase();
            if(db != null) {
                Project project = null;
                Cursor cursor = db.query("PROJECT", new String[]{"ID", "CURRENT", "FILE_PATH"}, "CURRENT = ?", new String[]{String.valueOf(current)}, null, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    project = new Project();
                    project.setId(cursor.getInt(0));
                    project.setCurrent(cursor.getString(1));
                    project.setFilePath(cursor.getString(2));
                    cursor.close();
                }
                db.close();
                return project;
            }
            return null;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }
}