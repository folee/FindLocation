package com.emerson.service;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * The location tool
 * 
 * @author Emerson
 */
public class LBSTool {
	public final String				TAG								= LBSTool.class.getSimpleName();
	public final static int			GPSTIMEOUT						= 0;
	public final static int			GETLOCATION_FAILED				= 1;
	public final static int			GETLOCATION_SUCCEED				= 2;
	public final static int			STATUS_CHANGED					= 3;
	public final static int			CANCELGPS_COMPLETED				= 4;
	public final static int			REFRESHGPS_COMPLETED			= 5;
	public final static int			REFRESHGPS_NOPROVIDER			= 6;

	public final static int			SELECT_LOCATION					= 7;
	public final static int			SERVICE_DISABLE					= 8;
	public final static int			SELECT_LOCATION_COMPLETED		= 9;
	public final static int			GET_LOCATIONBUILDINGLIST_FAILED	= 10;
	public final static int			DEFAULT_LOCATION_COMPLETED		= 11;

	private Location				mCurLocation					= null;
	private LocationObserver		mLocationObsever				= null;

	/** location services */
	private LocationManager			mLocationManager				= null;

	private Context					mContext						= null;

	/** location services */
	private MyLocationListener		mLocationListener				= null;

	/** latitude and longitude of current location */
	public static volatile double	mLat;
	public static volatile double	mLon;

	protected static String			sLastLocation					= "";

	/** time out for GPS location update */
	private Timer					mGpsTimer						= new Timer();
	/** TimerTask for time out of GPS location update */
	private GpsTimeOutTask			mGpsTimeOutTask					= new GpsTimeOutTask();
	/** GPS location update time out in milliseconds */
	private long					mGpsTimeOut						= 180000;							// 3
																										// minutes
	/** handler for time out of GPS location update */
	private Handler					mGpsTimerHandler;

	public void initiLocationUtil(Context context, LocationObserver locationobsever) {
		procHandler();
		mLocationObsever = locationobsever;
		mContext = context;
		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		mLocationListener = new MyLocationListener();
	}

	public void setObserver(LocationObserver locationobsever) {
		mLocationObsever = locationobsever;
	}

	public void removeObserver() {
		mLocationObsever = null;
	}

	/**
	 * location listener
	 */
	private class MyLocationListener implements LocationListener {

		// private boolean mLocationReceived = false;
		public void onLocationChanged(Location location) {
			if (location != null) {// && !mLocationReceived){
				// get location building only once
				mCurLocation = location;
				// mLocationReceived = true;
				Log.d(TAG, "location " + location.getAltitude());
				double lon = location.getLongitude();
				double lat = location.getLatitude();
				setLocation(lat, lon);
				if (mLocationObsever != null) {
					mLocationObsever.notifyChange(GETLOCATION_SUCCEED, null);
				}
			}
			else if (location == null) {
				if (mLocationObsever != null) {
					mLocationObsever.notifyChange(GETLOCATION_FAILED, null);
				}
			}
		}

		public void onProviderDisabled(String provider) {

		}

		public void onProviderEnabled(String provider) {

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

			// if GPS provider is not accessible, try network provider
			if (provider.equals(LocationManager.GPS_PROVIDER) && status != LocationProvider.AVAILABLE) {
				boolean bEnabled = false;
				try {
					bEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (bEnabled) {
					mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
				}
				else {
					mLocationManager.removeUpdates(mLocationListener);

					// mLocationObsever.notifyChange(SETADDLOCATIONBUTTONSTATE_1_SETLOCATIONDES_1,null);
					if (mLocationObsever != null) {
						mLocationObsever.notifyChange(STATUS_CHANGED, null);
					}
				}
			}
		}
	}

	public boolean isGPSProviderEnabled() {
		return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	/**
	 * refresh GPS data. try GPS provider firstly, if it is not accessible, try
	 * NETWORK provider
	 */
	public void refreshGPS(boolean calledByCreate) {
		Log.d(TAG, "begin refresh GPS, calledByCreate=" + calledByCreate);
		mLocationManager.removeUpdates(mLocationListener);
		boolean providerEnable = true;
		boolean showLocationServiceDisableNotice = true;

		boolean bEnabled = false;
		try {
			bEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			Log.d(TAG, "GPS_PROVIDER isProviderEnabled=" + bEnabled);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (bEnabled) {
			Log.d(TAG, "requestLocationUpdates by GPS");
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
			showLocationServiceDisableNotice = false;

			Log.d(TAG, "start GpsTimeOutTask");
			// start time out timer
			mGpsTimer = new Timer();
			mGpsTimeOutTask = new GpsTimeOutTask();
			mGpsTimer.schedule(mGpsTimeOutTask, mGpsTimeOut);

		}
		else {
			// no enabled location provider
			providerEnable = false;
			if (calledByCreate && getIncludeLocationPreference() == false) {
				showLocationServiceDisableNotice = false;
			}
		}

		bEnabled = false;
		try {
			bEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			Log.d(TAG, "NETWORK_PROVIDER isProviderEnabled=" + bEnabled);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (bEnabled) {
			Log.d(TAG, "requestLocationUpdates by NETWORK_PROVIDER");
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
			showLocationServiceDisableNotice = false;
			providerEnable = true;
		}

		if (providerEnable) {
			if (mLocationObsever != null) {
				Log.d(TAG, "notifyChange: REFRESHGPS_COMPLETED");
				mLocationObsever.notifyChange(REFRESHGPS_COMPLETED, null);
			}

		}
		else {
			if (mLocationObsever != null) {
				Log.d(TAG, "notifyChange: REFRESHGPS_NOPROVIDER");
				mLocationObsever.notifyChange(REFRESHGPS_NOPROVIDER, null);
			}

		}

		if (showLocationServiceDisableNotice) {
			// 1 mLocationObsever.notifyChange(REFRESHGPS_SHOWNOTICE, null);
			Toast.makeText(mContext, "LBS service has been disabled!", Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * quick refresh GPS data.
	 */
	public void quickRefreshGPS() {
		mLocationManager.removeUpdates(mLocationListener);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(true);
		/**
		 * when network and gps are both enabled, then network is returned.
		 */
		String bestProvider = mLocationManager.getBestProvider(criteria, true);

		if (null == bestProvider) {
			if (mLocationObsever != null) {
				mLocationObsever.notifyChange(REFRESHGPS_NOPROVIDER, null);
			}
		}
		else {
			mLocationManager.requestLocationUpdates(bestProvider, 0, 0, mLocationListener);
			if (bestProvider.equals(LocationManager.GPS_PROVIDER)) {
				// start time out timer
				mGpsTimer = new Timer();
				mGpsTimeOutTask = new GpsTimeOutTask();
				mGpsTimer.schedule(mGpsTimeOutTask, mGpsTimeOut);
			}

			if (mLocationObsever != null) {
				mLocationObsever.notifyChange(REFRESHGPS_COMPLETED, null);
			}
		}
	}

	/**
	 * save preference boolean value which indicates if location is included in
	 * the diary last time
	 */
	public boolean getIncludeLocationPreference() {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(mContext);
		return p.getBoolean("IncludeLocation", true);
	}

	/**
	 * cancel operations of refreshing GPS
	 */
	public void cancelRefreshGPS() {
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(mLocationListener);
		}

		if (mLocationObsever != null) {
			mLocationObsever.notifyChange(CANCELGPS_COMPLETED, null);
		}
	}

	public void destroy() {
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(mLocationListener);
		}

		if (mGpsTimer != null) {
			mGpsTimer.cancel();
		}

		cancelRefreshGPS();

		mContext = null;
		mLocationObsever = null;
	}

	/**
	 * this class is TimerTask for time out of GPS location update
	 */
	private class GpsTimeOutTask extends TimerTask {
		public void run() {
			Message message = new Message();
			message.what = 1;
			mGpsTimerHandler.sendMessage(message);
		}
	}

	public void refreshLocation() {
		if (mCurLocation != null) {
			// Location location =
			// mLocationManager.getLastKnownLocation(mLocationProvider);
			double lat = mCurLocation.getLatitude();
			double lon = mCurLocation.getLongitude();
			// String lon = String.valueOf(116.2829);
			// String lat = String.valueOf(39.9577);
			// Log.i("--location---", lat + "|||" + lon);
			setLocation(lat, lon);
			mLocationObsever.notifyChange(GETLOCATION_SUCCEED, null);
		}
		else {
			mLocationObsever.notifyChange(GETLOCATION_FAILED, null);
		}
	}

	private static void setLocation(double lat, double lon) {
		mLat = lat;
		mLon = lon;
	}

	public static void setLastLocation(String strLocation) {
		sLastLocation = strLocation;
	}

	private void procHandler() {
		mGpsTimerHandler = new Handler() {
			public void handleMessage(Message msg) {

				if (mLocationManager == null) {
					return;
				}

				boolean bEnabled = false;
				try {
					bEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (bEnabled) {
					mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
				}
				else {
					mLocationManager.removeUpdates(mLocationListener);

					if (mLocationObsever != null) {
						mLocationObsever.notifyChange(GPSTIMEOUT, null);
					}
				}
			}
		};
	}
}