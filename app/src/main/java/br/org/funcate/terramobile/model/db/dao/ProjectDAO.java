package br.org.funcate.terramobile.model.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
                    contentValues.put("NAME", project.getName());
                    contentValues.put("FILE_PATH", project.getFilePath());
                    contentValues.put("UPDATED", project.isUpdated());
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
                    contentValues.put("NAME", project.getName());
                    contentValues.put("FILE_PATH", project.getFilePath());
                    contentValues.put("UPDATED", project.isUpdated());
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

    public Project getByName(String name) {
        try {
            SQLiteDatabase db = dataBase.getReadableDatabase();
            Project project = null;
            if(db != null) {
                Cursor cursor = db.query("PROJECT", new String[]{"ID", "NAME", "FILE_PATH", "UPDATED"}, "NAME = ?", new String[]{String.valueOf(name)}, null, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    project = new Project();
                    project.setId(cursor.getInt(0));
                    project.setName(cursor.getString(1));
                    project.setFilePath(cursor.getString(2));
                    project.setUpdated(cursor.getInt(3));
                    cursor.close();
                }
                db.close();
            }
            return project;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean remove(int id) {
        SQLiteDatabase db = dataBase.getWritableDatabase();
        int rows = db.delete("PROJECT", "id = ?", new String[] { String.valueOf(id) });
        db.close();
        if(rows != 0)
            return true;
        return false;
    }

    public Project getFirstProject() {
        String selectQuery = "select * from project";

        SQLiteDatabase db = dataBase.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        Project project = null;
        if (cursor.moveToFirst()) {
            project = new Project();
            project.setId(cursor.getInt(0));
            project.setName(cursor.getString(1));
            project.setFilePath(cursor.getString(2));
            project.setUpdated(cursor.getInt(3));
        }
        cursor.close();
        db.close();
        return project;
    }
}