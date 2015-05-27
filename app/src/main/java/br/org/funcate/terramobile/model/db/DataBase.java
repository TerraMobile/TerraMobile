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
	

	private String dropTableSettings = "drop table if exists SETTINGS";

	/**
	 * Class constructor
	 * 
	 * @param context
	 */
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
	}
	
	/**
	 * Creates all the tables
	 */
	public void onCreate(SQLiteDatabase db) {
		if (!db.isReadOnly())
			db.execSQL("PRAGMA foreign_keys=ON;");
		createTables();
		db.execSQL(createSettings);
	}

	
	/**
	 * Drops all the tables
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(dropTableSettings);
		onCreate(db);
	}

	private void createTables(){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("create table if not exists SETTINGS (");
		stringBuilder.append("ID integer primary key,");
		stringBuilder.append("USER_NAME text,");
		stringBuilder.append("PASSWORD text,");
		stringBuilder.append("URL text);");
		createSettings = stringBuilder.toString();
	}
}
