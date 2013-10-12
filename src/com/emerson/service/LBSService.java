package com.emerson.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Location Based Service
 * 
 * @author Emerson
 * */
public class LBSService {
	final static String	TAG					= LBSService.class.getSimpleName();
	LocationData		locationData		= null;
	LBSTool				lbsTool;
	GPSObserver			mGPSObserver;
	Context				mContext;
	LastKnownLocation	lastknownLocation	= LastKnownLocation.getInstance();
	Handler				mHandler			= null;

	public LBSService(Context context) {
		mContext = context;
		mGPSObserver = new GPSObserver();
		lbsTool = new LBSTool();
		lbsTool.initiLocationUtil(mContext, this.mGPSObserver);
	}

	/**
	 * refresh the location data
	 * 
	 * @param Handler
	 *            the callback handler after the new location has been updated
	 * */
	public void refreshLocation(Handler handler) {
		mHandler = handler;
		lbsTool.refreshGPS(true);
	}

	/**
	 * cancel refresh location data
	 * */
	public void cancelRefresh() {
		lbsTool.cancelRefreshGPS();
		mHandler = null;
	}

	public void clear() {
		cancelRefresh();
		mGPSObserver = null;
	}

	private class GPSObserver extends LocationObserver {
		@Override
		public void notifyChange(int arg, String des) {
			switch (arg) {
			case LBSTool.GPSTIMEOUT:
				Log.d(TAG, "timeout");
				sendMsg(LBSTool.GPSTIMEOUT);
				break;
			case LBSTool.STATUS_CHANGED:
				Log.d(TAG, "location status changed");
				lbsTool.refreshLocation();
				break;
			case LBSTool.GETLOCATION_SUCCEED:
				locationData = new LocationData();
				locationData.latitude = lbsTool.mLat;
				locationData.longitude = lbsTool.mLon;
				lastknownLocation.setLastKnownLocation(mContext, "userID", lbsTool.mLat, lbsTool.mLon);
				Log.d(TAG, "lat: " + locationData.latitude + "  lon: " + locationData.longitude);
				sendMsg(LBSTool.GETLOCATION_SUCCEED);
				break;
			case LBSTool.GETLOCATION_FAILED:
				Log.d(TAG, "get location failed");
				sendMsg(LBSTool.GETLOCATION_FAILED);
				break;
			case LBSTool.REFRESHGPS_COMPLETED:
				Log.d(TAG, "refreshgps completed");
				break;
			case LBSTool.REFRESHGPS_NOPROVIDER:
				Log.d(TAG, "no provider");
				break;
			case LBSTool.CANCELGPS_COMPLETED:
				Log.d(TAG, "cancelgps completed");
				break;
			default:
				break;
			}
		}

		private void sendMsg(int msgCode) {
			Message message = Message.obtain();
			message.what = msgCode;
			if (mHandler != null)
				mHandler.sendMessage(message);
		}
	}

}
