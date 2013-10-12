package com.emerson.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * 
 * @author Emerson
 * 
 *         store the last known location for a user
 */
public class LastKnownLocation implements IModel {
	public static final String			TAG				= LastKnownLocation.class.getSimpleName();
	public static final String			TABLE_NAME		= TAG;

	// attributes
	public static final String			INTERNAL_ID		= "_id";
	public static final String			USER_ID			= "user_id";
	public static final String			LATITUDE		= "latitude";
	public static final String			LONGITUDE		= "longitude";

	protected static final String		TABLE_CREATE	= "CREATE TABLE " + TABLE_NAME + " (" + INTERNAL_ID
																+ " INTEGER primary key autoincrement," + USER_ID
																+ " VARCHAR(36) NOT NULL, " + LATITUDE + " DOUBLE, "
																+ LONGITUDE + " DOUBLE " + ");";

	private static LastKnownLocation	instance		= null;

	private LastKnownLocation() {}

	public static synchronized LastKnownLocation getInstance() {
		if (instance == null) {
			instance = new LastKnownLocation();
		}
		return instance;
	}

	public void setLastKnownLocation(Context context, String userId, double lat, double lon) {
		if (existsByUUID(context, userId)) {
			this.update(context, userId, lat, lon);
		}
		else {
			this.save(context, userId, lat, lon);
		}
	}

	public LocationData getLastKnownLocation(Context context, String userID) {
		if (!existsByUUID(context, userID)) {
			return null;
		}
		LocationData ld = new LocationData();
		LBSDB db = LBSDB.getDBInstance(context);;
		String selection = String.format("%s = '%s'", USER_ID, userID);
		Cursor cursor = db.query(TAG, selection, null);
		try {
			if (cursor.moveToFirst()) {
				ld.latitude = cursor.getDouble(cursor.getColumnIndex(LATITUDE));
				ld.longitude = cursor.getDouble(cursor.getColumnIndex(LONGITUDE));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return ld;

	}

	private long save(Context context, String uuid, double lat, double lon) {
		LBSDB db = LBSDB.getDBInstance(context);
		ContentValues cv = new ContentValues();
		cv.put(USER_ID, uuid);
		cv.put(LATITUDE, lat);
		cv.put(LONGITUDE, lon);
		try {
			long internalId = db.insert(TAG, cv);
			return internalId;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	private int update(Context context, String uuid, double lat, double lon) {
		LBSDB db = LBSDB.getDBInstance(context);
		ContentValues cv = new ContentValues();
		cv.put(LATITUDE, lat);
		cv.put(LONGITUDE, lon);
		int updated = -1;
		try {
			updated = db.update(TAG, cv, String.format("%s = ?", USER_ID), new String[] { uuid });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return updated;
	}

	/**
	 * clear the table
	 * 
	 * @return deleted boolean
	 */
	private int deleteAll(Context context) {
		LBSDB db = LBSDB.getDBInstance(context);
		int deleted = db.delete(TAG, null, null);
		return deleted;
	}

	private boolean existsByUUID(Context context, String UUId) {
		LBSDB db = LBSDB.getDBInstance(context);
		final String SELECTION = USER_ID + "=?";
		final String[] SELECTION_ARGS = { UUId };
		Cursor c = db.query(TAG, SELECTION, SELECTION_ARGS);
		boolean exists = false;
		if (c != null) {
			int count = c.getCount();
			exists = (count > 0) ? true : false;
			c.close();
		}
		return exists;
	}

	public String getTableCreateString() {
		return TABLE_CREATE;
	}

	public String getTableName() {
		return TABLE_NAME;
	}

}
