package br.org.funcate.terramobile.model.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class its used to create and connect the database
 */
public class ApplicationDatabase extends DatabaseHelper {
	
	/* Name of the database */
	private static final String DATABASE_NAME = "terramobile";

	public ApplicationDatabase(Context context) {
		super(context, DATABASE_NAME);
	}

    private void initSettings()
    {
        StringBuilder sBCreateSettings = new StringBuilder();
        sBCreateSettings.append("create table if not exists SETTINGS (");
        sBCreateSettings.append("ID integer primary key AUTOINCREMENT not null,");
        sBCreateSettings.append("KEY text,");
        sBCreateSettings.append("VALUE text);");

        this.getWritableDatabase().execSQL(sBCreateSettings.toString());
    }

    private void initProjects()
    {
        StringBuilder sBCreateProject = new StringBuilder();
        sBCreateProject.append("create table if not exists PROJECT (");
        sBCreateProject.append("ID integer primary key AUTOINCREMENT not null,");
        sBCreateProject.append("NAME text not null,");
        sBCreateProject.append("FILE_PATH text not null,");
        sBCreateProject.append("DOWNLOADED integer NOT NULL,");
        sBCreateProject.append("UPDATED integer);");

        this.getWritableDatabase().execSQL(sBCreateProject.toString());
    }

    protected void initDatabase()
    {
        initSettings();
        initProjects();

    }
}