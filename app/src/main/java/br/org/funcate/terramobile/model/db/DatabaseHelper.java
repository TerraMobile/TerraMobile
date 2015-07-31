package br.org.funcate.terramobile.model.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by bogo on 31/07/15.
 */
public abstract class DatabaseHelper extends SQLiteOpenHelper {

    /* Version of the database */
    private static final int DATABASE_VERSION = 1;

    protected abstract void initDatabase();

    protected DatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
        initDatabase();
    }

    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    public void onCreate(SQLiteDatabase db) {
        if (!db.isReadOnly())
            db.execSQL("PRAGMA foreign_keys=ON;");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public boolean tableExists(String tableName, boolean openDb) {
        SQLiteDatabase database= getReadableDatabase();

        Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }
}
