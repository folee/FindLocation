package com.emerson.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public  class DatabaseHelper extends SQLiteOpenHelper{
    
	public static final String _DATABASE_NAME = "LBS.db";
	
	private static final int DATABASE_VERSION = 5;
	private static final String DROP_DATABASE_SQL_FORMAT = "DROP TABLE IF EXISTS %s";
	private static DatabaseHelper mDbHelper;
	
	public DatabaseHelper(Context context){
        super(context, _DATABASE_NAME, null, DATABASE_VERSION);
    }
	
	public static DatabaseHelper getDatabaseHelperInstance(Context paramContext) {
		if (mDbHelper == null) {
			mDbHelper = new DatabaseHelper(paramContext);
		}
		return mDbHelper;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db){
	    db.execSQL(LastKnownLocation.getInstance().getTableCreateString());
	}
	
	/**
	 * We should try to only use ALTER TABLE syntax onUpgrade :)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.execSQL(String.format(DROP_DATABASE_SQL_FORMAT, LastKnownLocation.TABLE_NAME));
	    onCreate(db);
	}
}