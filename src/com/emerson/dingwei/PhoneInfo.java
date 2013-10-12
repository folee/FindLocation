package com.emerson.dingwei;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.emerson.service.LBSService;
import com.emerson.service.LBSTool;
import com.emerson.service.LastKnownLocation;
import com.emerson.service.LocationData;
import com.emerson.service.PhoneService;

public class PhoneInfo extends ListActivity {
	private final String			TAG			= PhoneInfo.class.getSimpleName();
	ArrayList<Map<String, String>>	list;
	ArrayList<String>				data;
	PhoneService					service;
	String[]						strs;
	private LBSService				lbsService	= null;
	private Handler					mHandler;
	private String					location		= "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		service = new PhoneService(this);
		String[] strs = { "本机号码：" + service.getNumber(), "手机型号：" + service.getModel(), "手机IMEI：" + service.getIMEI(),
				"手机CUP：" + service.getCPU_ABI(), "SDK版本号： " + service.getSDKVersion() };

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strs);
		setListAdapter(adapter);
		procHandler();
		
		lbsService = new LBSService(this);
		lbsService.refreshLocation(mHandler);
		
	}

	private void procHandler() {
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case LBSTool.GETLOCATION_SUCCEED:
					LocationData location = LastKnownLocation.getInstance().getLastKnownLocation(PhoneInfo.this, "userID");
					Log.d(TAG, "latitude = " + location.latitude + "; longitude = " + location.longitude);
					getLocation(location.latitude, location.longitude);
					lbsService.cancelRefresh();
					break;
				case LBSTool.GETLOCATION_FAILED:
					Log.d(TAG,"get location failed");
					break;
				case LBSTool.GPSTIMEOUT:
					Log.d(TAG,"get location timeout");
					break;
				case LBSTool.REFRESHGPS_COMPLETED:
					Log.d(TAG,"refreshgps completed");
					break;
				case LBSTool.REFRESHGPS_NOPROVIDER:
					Log.d(TAG,"no provider");
					break;
				case LBSTool.CANCELGPS_COMPLETED:
					Log.d(TAG,"cancelgps completed");
					break;

				default:
					break;
				}
			}
		};
	}
	
	
	private void getLocation(final double mlat, final double mLon) {
		new Thread(new Runnable() {
			public void run() {
				String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + mlat + "," + mLon
						+ "&sensor=true&language=zh-cn";
				HttpClient httpClient = new DefaultHttpClient();
				try {
					HttpResponse response = httpClient.execute(new HttpGet(url));
					HttpEntity entity = response.getEntity();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent()));
					String line = "";
					while ((line = bufferedReader.readLine()) != null) {
						location = location + line;
					}

					if (location.indexOf("formatted_address") != -1) {
						location = location.substring(location.indexOf("formatted_address") + 22,
								location.indexOf("geometry"));
						location = location.replaceAll(" ", "");
						location = location.replaceAll("\"", "");
						location = location.replaceAll("\"", "");
						location = location.replaceAll(",", "");
					}
					else {
						location = "";
					}
					Log.v(TAG, "location  = " + location);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

}
