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
        StringBuilder sBCreate = new StringBuilder();
        sBCreate.append("create table if not exists TM_SETTINGS (");
        sBCreate.append("ID integer primary key AUTOINCREMENT not null,");
        sBCreate.append("KEY text,");
        sBCreate.append("VALUE text);");

        this.getWritableDatabase().execSQL(sBCreate.toString());
    }

    private void initStyleTable()
    {
        StringBuilder sBCreate = new StringBuilder();
        sBCreate.append("create table if not exists TM_STYLE (");
        sBCreate.append("LAYER_NAME text primary key not null,");
        sBCreate.append("SLD_XML text,");
        sBCreate.append("CONSTRAINT fk_layer_name FOREIGN KEY (LAYER_NAME) REFERENCES gpkg_contents(table_name));");


        this.getWritableDatabase().execSQL(sBCreate.toString());
    }

    private void initLayerSettingsTable()
    {
        StringBuilder sBCreate = new StringBuilder();
        sBCreate.append("CREATE TABLE IF NOT EXISTS TM_LAYER_SETTINGS (");
        sBCreate.append("LAYER_NAME text primary key not null,");
        sBCreate.append("ENABLED boolean not null,");
        sBCreate.append("POSITION integer not null unique,");
        sBCreate.append("CONSTRAINT fk_layer_name FOREIGN KEY (LAYER_NAME) REFERENCES gpkg_contents(table_name));");

        this.getWritableDatabase().execSQL(sBCreate.toString());
    }

    protected void initDatabase()
    {
        initSettings();
        initStyleTable();
        initLayerSettingsTable();
    }
}