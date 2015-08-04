package br.org.funcate.terramobile.model.db;

import android.content.Context;

/**
 * This class is an extends or generic database helper do be able to use different configuration while access different kinds of database, in this case each project GPKG sqlite database
 */
public class ProjectDatabase extends DatabaseHelper {

	/* Name of the database */
	public static String DATABASE_NAME = "";

	protected ProjectDatabase(Context context, String databaseName)
    {
		super(context, databaseName);
        DATABASE_NAME = databaseName;
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

    protected void initDatabase()
    {
        initSettings();
    }
}