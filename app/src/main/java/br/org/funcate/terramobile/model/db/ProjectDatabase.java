package br.org.funcate.terramobile.model.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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
        sBCreate.append("datasource_uri text,");
        sBCreate.append("modified INTEGER NOT NULL DEFAULT (0),");
        sBCreate.append("CONSTRAINT fk_layer_name FOREIGN KEY (LAYER_NAME) REFERENCES gpkg_contents(table_name));");

        this.getWritableDatabase().execSQL(sBCreate.toString());
    }

    private void initGPKGSysTables()
    {
        String sCreate =  "CREATE TABLE IF NOT EXISTS gpkg_data_columns (" +
                " table_name TEXT NOT NULL," +
                " column_name TEXT NOT NULL," +
                " name TEXT, title TEXT," +
                " description TEXT," +
                " mime_type TEXT," +
                " constraint_name TEXT," +
                " CONSTRAINT pk_gdc PRIMARY KEY (table_name, column_name)," +
                " CONSTRAINT fk_gdc_tn FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name));";
        this.getWritableDatabase().execSQL(sCreate);

        sCreate =  "CREATE TABLE IF NOT EXISTS gpkg_data_column_constraints (" +
                " constraint_name TEXT NOT NULL," +
                " constraint_type TEXT NOT NULL," +
                " value TEXT, min NUMERIC," +
                " minIsInclusive BOOLEAN," +
                " max NUMERIC," +
                " maxIsInclusive BOOLEAN," +
                " CONSTRAINT gdcc_ntv UNIQUE (constraint_name, constraint_type, value));";

        this.getWritableDatabase().execSQL(sCreate);
    }

    private void initLayerFormTable()
    {
        StringBuilder sBCreate = new StringBuilder();
        sBCreate.append("CREATE TABLE IF NOT EXISTS tm_layer_form (");
        sBCreate.append("tm_conf_id INTEGER PRIMARY KEY AUTOINCREMENT,");
        sBCreate.append("gpkg_layer_identify TEXT NOT NULL,");
        sBCreate.append("tm_form TEXT,");
        sBCreate.append("tm_media_table TEXT,");
        sBCreate.append("CONSTRAINT fk_layer_identify_id FOREIGN KEY (gpkg_layer_identify) REFERENCES gpkg_contents(table_name));");

        this.getWritableDatabase().execSQL(sBCreate.toString());
    }

    private String exportTable(String tableName, String pkName, String[] args)
    {
        StringBuilder sBSelect = new StringBuilder();
        StringBuilder sBInto = new StringBuilder();
        StringBuilder sBValues = new StringBuilder();
        sBSelect.append("SELECT ");
        sBSelect.append("\"INSERT INTO ");
        sBSelect.append(tableName);
        sBSelect.append(" ");
        sBInto.append("(");
        sBInto.append(pkName);
        sBValues.append(" VALUES ('\" || ");
        sBValues.append(pkName);
        sBValues.append(" || \"'\" ");

        for (String arg : args) {
            sBInto.append(", ");
            sBInto.append(arg);
            sBValues.append(" || \",\" || ifnull(\"'\" || ");
            sBValues.append(arg);
            sBValues.append(" ||\"'\",\"NULL\") ");
        }

        sBInto.append(")");
        sBValues.append(" || \"); \" as scpt ");
        sBSelect.append(sBInto.toString());
        sBSelect.append(sBValues.toString());
        sBSelect.append(" FROM ");
        sBSelect.append(tableName);

        Cursor c = this.getWritableDatabase().rawQuery(sBSelect.toString(), null);
        StringBuilder sqlOutput = new StringBuilder();

        if(c.moveToFirst()) {
            do {
                sqlOutput.append(c.getString(0));
            }while (c.moveToNext());
        }
        c.close();

        return sqlOutput.toString();
    }

    /**
     * Export data from tm_layer_form to SQL INSERT script fragment.
     */
    private String exportLayerFormTable()
    {
        String tableName="tm_layer_form";
        String[] args=new String[3];
        args[0] = "gpkg_layer_identify";
        args[1] = "tm_form";
        args[2] = "tm_media_table";
        String pkName="tm_conf_id";
        return this.exportTable(tableName, pkName, args);
    }

    /**
     * Export data from TM_SETTINGS to SQL INSERT script fragment.
     */
    private String exportSettingsTable()
    {
        String tableName="TM_SETTINGS";
        String[] args=new String[2];
        args[0] = "key";
        args[1] = "value";
        String pkName="id";
        return this.exportTable(tableName, pkName, args);
    }

    /**
     * Export data from TM_STYLE to SQL INSERT script fragment.
     */
    private String exportStyleTable()
    {
        String tableName="TM_STYLE";
        String[] args=new String[1];
        args[0] = "SLD_XML";
        String pkName="LAYER_NAME";
        return this.exportTable(tableName, pkName, args);
    }

     /**
     * Export data from TM_LAYER_SETTINGS to SQL INSERT script fragment.
     */
    private String exportLayerSettingsTable()
    {
        String tableName="TM_LAYER_SETTINGS";
        String[] args=new String[3];
        args[0] = "ENABLED";
        args[1] = "POSITION";
        args[2] = "datasource_uri";
        String pkName="LAYER_NAME";
        return this.exportTable(tableName, pkName, args);
    }


    public String exportSettings() {

        StringBuilder script = new StringBuilder();

        script.append(this.exportLayerFormTable());
        script.append(this.exportLayerSettingsTable());
        script.append(this.exportSettingsTable());
        script.append(this.exportStyleTable());

        return script.toString();
    }

    public boolean importSettings(String sqlScript) {
        if(sqlScript==null && sqlScript.isEmpty()) {
            return false;
        }
        String[] statements = sqlScript.split(";");
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        try {
            sqlDB.beginTransaction();
            for (String sql : statements) {
                if(!sql.isEmpty()) sqlDB.execSQL( sql );
            }
            sqlDB.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }finally {
            sqlDB.endTransaction();
        }
        return true;
    }


    protected void initDatabase()
    {
        initSettings();
        initStyleTable();
        initLayerSettingsTable();
        initGPKGSysTables();
        initLayerFormTable();
    }


}