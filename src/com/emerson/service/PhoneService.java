package com.emerson.service;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

//必须添加权限 <uses-permission android:name="android.permission.READ_PHONE_STATE" />
public class PhoneService  {
	Context context;
	TelephonyManager phoneMgr;
	
	public PhoneService(Context context){
		this.context = context;
		phoneMgr=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	}
	//获取本机号码。
	public String getNumber(){
		String number = phoneMgr.getLine1Number();
		return number;
	}
	
	//手机型号 
	public String getModel(){
		String model = Build.MODEL;
		return model;
	}
	
	//SDK版本号 
	public String getSDKVersion(){
		String sdk_version = Build.VERSION.SDK;
		return sdk_version;
	}
	
	//Firmware/OS 版本号
	public String getOSVersion(){
		String os_version = Build.VERSION.RELEASE;
		return os_version;
	}
	
	// 手机IMEI
	public String getIMEI(){
		String imei = phoneMgr.getDeviceId();
		return imei;
	}
	
	//手机IMSI
	public String getIMSI(){
		String imsi = phoneMgr.getSimSerialNumber();
		return imsi;
	}
	

	//一个字符串，它唯一标识此版本。
	public String getFingerPrint(){
		String fingerPrint = Build.FINGERPRINT;
		return fingerPrint;
	}
	
	//主板信息
	public String getBorad(){
		String borad = Build.BOARD;
		return borad;
	}
	
	//cup
	public String getCPU_ABI(){
		String cpu = Build.CPU_ABI;
		return cpu;
	}
	
}
