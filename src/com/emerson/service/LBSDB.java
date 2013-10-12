package com.emerson.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;

public class LBSDB {
	private static final String TAG = LBSDB.class.getSimpleName();
	private DatabaseHelper mDbHelper;
	private static LBSDB mDB;
	
	public static final String KEY_TABLE_NAME = "table_name";
	public static final String KEY_SELECTION_NAME = "selection";
	public static final String KEY_SELECTION_ARGS = "selection_args";
	public static final String KEY_PROJECTION = "projection";
	public static final String KEY_SORT_ORDER = "sort_order";
	public static final String KEY_GROUP_BY = "group_by";
	public static final String KEY_HAVING = "having";
	

	public synchronized static LBSDB getDBInstance(Context ctx) {
		if (mDB == null) {
			mDB = new LBSDB(ctx);
		}
		return mDB;
	}
	
	private LBSDB(Context ctx) {
		mDbHelper = DatabaseHelper.getDatabaseHelperInstance(ctx);
	}

	public SQLiteDatabase getReadableDatabase() {
		return mDbHelper.getReadableDatabase();
	}
	
	public SQLiteDatabase getWritableDatabase() {
		return mDbHelper.getWritableDatabase();
	}
	
	public int delete(String tableName, String selection, String[] selectionArgs) {
		if (TextUtils.isEmpty(tableName)) {
			return 0;
		}
		int num = 0;
		try{
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		num = db.delete(tableName, selection, selectionArgs);
		} catch (Exception e) {
			Log.e(TAG, "delete", e);
		}
		return num;
	}

	public long insert(String tableName, ContentValues values) {
		if (values == null || TextUtils.isEmpty(tableName)) {
			return 0;
		}

		try {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			long id = db.insert(tableName, null, values);
			return id;
		} catch (Exception e) {
			Log.e(TAG, "insert", e);
		}

		return 0;
	}

	//bulk insert
	public int bulkInsert(String tableName, ContentValues[] values) {
		if (TextUtils.isEmpty(tableName) || values == null) {
			return 0;
		}
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();
		long id = 0;
		int length = values.length;
		try {
			for (int i = 0; i < length; i++) {
				id = db.insert(tableName, null, values[i]);
			}
			db.setTransactionSuccessful();

		} catch (Exception e) {
			Log.e(TAG, "bulkInsert", e);
		} finally {
			db.endTransaction();
		}

		if (id > 0) {
			return length;
		}
		return 0;
	}

	public Cursor query(String tableName, String selection, String[] selectionArgs) {
		if (TextUtils.isEmpty(tableName)) {
			return null;
		}
		try {
			Cursor c = null;
			SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(tableName);
			c = qb.query(db, null, selection, selectionArgs, null, null, null);
			return c;
		} catch (Exception e) {
			Log.e(TAG, "query", e);
		}
		return null;
	}
	
	public Cursor query(String tableName, String[] projection, String selection, 
			String[] selectionArgs, String groupBy, String having, String sortOrder, String limit) {
		if (TextUtils.isEmpty(tableName)) {
			return null;
		}
		try {
			Cursor c = null;
			SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(tableName);
			c = qb.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder, limit);
			return c;
		} catch (Exception e) {
			Log.e(TAG, "query", e);
		}
		return null;
	}
	
	public int update(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
		if (TextUtils.isEmpty(tableName) || values == null) {
			return 0;
		}
		int num = 0;
		try {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			num = db.update(tableName, values, whereClause, whereArgs);
		} catch (Exception e) {
			Log.e(TAG, "update", e);
		}
		return num;
	}
	
	public void close() {
		
	}

};