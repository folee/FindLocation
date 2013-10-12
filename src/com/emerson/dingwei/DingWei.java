package com.emerson.dingwei;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.mapapi.geocoder.Geocoder;
import com.amap.mapapi.location.LocationManagerProxy;

/**
 * 获取定位信息。
 */

// 定位用户的位置
// 第二次修改
public class DingWei extends Activity implements LocationListener {
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		finish();
		return super.onMenuItemSelected(featureId, item);
	}

	double							mLat			= 21.158548;
	double							mLon			= 113.355678;
	private Button					btn, send, getInfo;
	private ProgressDialog			dialog			= null;
	private Geocoder				coder;
	private LocationManagerProxy	locationManager	= null;
	private TextView				showLocation;
	private String					location		= "";

	private Handler					handler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		procHandler();
		
//		// 植入酷果广告
//		KuguoAdsManager.getInstance().receivePushMessage(DingWei.this, true);
//		/**
//		 * 加入精品推荐的酷仔，传入0表示显示酷仔
//		 */
//		KuguoAdsManager.getInstance().showKuguoSprite(this, KuguoAdsManager.STYLE_KUZAI);
//		/**
//		 * 加入精品推荐的酷悬，传入1表示显示酷悬
//		 */
//		KuguoAdsManager.getInstance().showKuguoSprite(this, KuguoAdsManager.STYLE_KUXUAN);

		showLocation = (TextView) findViewById(R.id.text);
		locationManager = LocationManagerProxy.getInstance(this);

		btn = (Button) this.findViewById(R.id.geobtn);
		send = (Button) findViewById(R.id.send);

		coder = new Geocoder(this);
		dialog = new ProgressDialog(this);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getLocation(mLat, mLon);
			}
		});

		send.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"));
				intent.putExtra("sms_body", "我现在在" + location);
				startActivity(intent);
			}
		});
		getInfo = (Button) findViewById(R.id.getInfo);
		getInfo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getInfo();
			}
		});
	}

	private void getInfo() {
		Intent intent = new Intent();
		intent.setClass(this, PhoneInfo.class);
		startActivity(intent);
	}

	// 注册监听
	public boolean enableMyLocation() {
		boolean result = true;
		Criteria cri = new Criteria();
		cri.setAccuracy(Criteria.ACCURACY_COARSE);// 表示所需的经度和纬度的精度。
		cri.setAltitudeRequired(false);// 指示是否提供者必须提供高度的信息。
		cri.setBearingRequired(false);// 指示是否提供者必须提供承载的信息。
		cri.setCostAllowed(false);// 是否允许付费
		String bestProvider = locationManager.getBestProvider(cri, false);
		System.out.println(bestProvider);
		/**
		 * provider - 注册监听的provider名称 minTime - 位置变化的通知时间，单位为毫秒，实际时间有可能长于或短于设定值
		 * minDistance - 位置变化通知距离，单位为米 listener - 监听listener
		 */
		locationManager.requestLocationUpdates(bestProvider, 2000, 10, this);
		return result;
	}

	// 重新获得焦点
	@Override
	protected void onResume() {
		super.onResume();
		enableMyLocation();
	}

	// 暂停
	@Override
	protected void onPause() {
		// 移除给定的listener位置更新
		locationManager.removeUpdates(this);
		super.onPause();
	}

	// 活动销毁时
	@Override
	protected void onDestroy() {
		if (locationManager != null) {
			// 移除给定的listener位置更新，并且销毁locationManager
			locationManager.removeUpdates(this);
			locationManager.destory();
		}
		locationManager = null;
		super.onDestroy();
	}

	// 位置变化时
	public void onLocationChanged(Location location) {
		if (location != null) {
			mLat = location.getLatitude();
			mLon = location.getLongitude();
			System.out.println("" + mLat + "  " + mLon);
		}
	}

	public void onProviderDisabled(String provider) {

	}

	// 当供应商由用户启用调用
	public void onProviderEnabled(String provider) {

	}

	// 当provider的状态发生改变时调用
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	private void getLocation(final double mlat, final double mLon) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + mlat + "," + mLon
						+ "&sensor=true&language=zh-cn";
				// 创建一个HttpClint请求；
				HttpClient httpClient = new DefaultHttpClient();
				try {
					// 向指定的URL发送http请求
					HttpResponse response = httpClient.execute(new HttpGet(url));
					// 取得服务器的响应
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
						handler.sendMessage(Message.obtain(handler, 1));
					}
					else {
						location = "";
					}
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendMessage(Message.obtain(handler, 0));
				}
			}
		});

		// ProgressDialog
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setIndeterminate(false);
		dialog.setCancelable(true);
		dialog.setMessage("正在寻找你~~");
		dialog.show();
		t.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void procHandler() {
		handler = new Handler() {
			public void handleMessage(Message msg) {
				if (location.length() == 0) {
					Toast.makeText(DingWei.this, "定位错误，请重试！", 1000).show();
				}
				else {
					showLocation.setText("您在" + location + "附近");
					if (msg.what == 1) {
						Toast.makeText(getApplicationContext(), location, Toast.LENGTH_LONG).show();
					}
					else if (msg.what == 0) {
						Toast.makeText(getApplicationContext(), "定位错误！请检查网络或重试！", Toast.LENGTH_SHORT).show();
					}
				}
				dialog.dismiss();
			}
		};
	}

}
