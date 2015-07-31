package br.org.funcate.terramobile.model.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.db.ApplicationDatabase;

/**
 * Created by marcelo on 5/26/15.
 */
public class ProjectDAO {
    private ApplicationDatabase database;

    public ProjectDAO(Context context) {
        this.database = new ApplicationDatabase(context);
    }

    public boolean insert(Project project) {
        try {
            SQLiteDatabase db = database.getWritableDatabase();
            if (db != null) {
                if (project != null) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("ID", project.getId());
                    contentValues.put("NAME", project.getName());
                    contentValues.put("FILE_PATH", project.getFilePath());
                    contentValues.put("UPDATED", project.isUpdated());
                    contentValues.put("DOWNLOADED", project.isDownloaded());
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
        SQLiteDatabase db = database.getWritableDatabase();
        try{
            if (db != null) {
                if (project != null) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("ID", project.getId());
                    contentValues.put("NAME", project.getName());
                    contentValues.put("FILE_PATH", project.getFilePath());
                    contentValues.put("UPDATED", project.isUpdated());
                    contentValues.put("DOWNLOADED", project.isDownloaded());
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
            SQLiteDatabase db = database.getReadableDatabase();
            Project project = null;
            if(db != null) {
                Cursor cursor = db.query("PROJECT", new String[]{"ID", "NAME", "FILE_PATH", "UPDATED", "DOWNLOADED"}, "NAME = ?", new String[]{String.valueOf(name)}, null, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    project = new Project();
                    project.setId(cursor.getInt(0));
                    project.setName(cursor.getString(1));
                    project.setFilePath(cursor.getString(2));
                    project.setUpdated(cursor.getInt(3));
                    project.setDownloaded(cursor.getInt(4));
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
        SQLiteDatabase db = database.getWritableDatabase();
        int rows = db.delete("PROJECT", "id = ?", new String[] { String.valueOf(id) });
        db.close();
        if(rows != 0)
            return true;
        return false;
    }

    public Project getFirstProject() {
        String selectQuery = "select * from project";

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        Project project = null;
        if (cursor.moveToFirst()) {
            project = new Project();
            project.setId(cursor.getInt(0));
            project.setName(cursor.getString(1));
            project.setFilePath(cursor.getString(2));
            project.setUpdated(cursor.getInt(3));
            project.setDownloaded(cursor.getInt(4));
        }
        cursor.close();
        db.close();
        return project;
    }
}