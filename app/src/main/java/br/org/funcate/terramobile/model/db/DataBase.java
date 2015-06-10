package br.org.funcate.terramobile.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class its used to create and connect the database
 */
public class DataBase extends SQLiteOpenHelper {
	
	/* Name of the database */
	private static final String DATABASE_NAME = "terramobile";
	
	/* Version of the database */
	private static final int DATABASE_VERSION = 1;
	
	/* String to create the table settings */
	private String createSettings;
	private String createProjects;

	private String dropTableSettings = "drop table if exists SETTINGS";
	private String dropTableProject = "drop table if exists PROJECT";

	public DataBase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Creates all the tables
	 */
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		createTables();
		db.execSQL(createSettings);
		db.execSQL(createProjects);
	}
	
	/**
	 * Creates all the tables
	 */
	public void onCreate(SQLiteDatabase db) {
		if (!db.isReadOnly())
			db.execSQL("PRAGMA foreign_keys=ON;");
		createTables();
		db.execSQL(createSettings);
		db.execSQL(createProjects);
	}

	/**
	 * Drops all the tables
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(dropTableSettings);
		db.execSQL(dropTableProject);
		onCreate(db);
	}

	private void createTables(){
		StringBuilder sBCreateSettings = new StringBuilder();
		sBCreateSettings.append("create table if not exists SETTINGS (");
		sBCreateSettings.append("ID integer primary key not null,");
		sBCreateSettings.append("USER_NAME text,");
		sBCreateSettings.append("PASSWORD text,");
		sBCreateSettings.append("URL text);");
		createSettings = sBCreateSettings.toString();

		StringBuilder sBCreateProject = new StringBuilder();
		sBCreateProject.append("create table if not exists PROJECT (");
		sBCreateProject.append("ID integer primary key not null,");
		sBCreateProject.append("CURRENT text not null,");
		sBCreateProject.append("FILE_PATH text not null);");
		createProjects = sBCreateProject.toString();
	}
}