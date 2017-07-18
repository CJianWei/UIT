package com.android.AUI;

import com.android.Common.Tools;
import com.android.Config.ConfUtil;
import com.android.Log.Log;

public class StartTest {
	public static boolean recordInitConf = false;
	public static boolean recordInitLog = false;
	public static AppiumServer appium = null;
	public static Adb adb = null;
	public static boolean recordInitWatch = false;
	
	
	public static void initAll() throws Exception{
		if (recordInitConf == false){
			ConfUtil.initConf(Tools.LoadPath(new String[]{"conf","conf.ini"}));
			recordInitConf = true;
		}
		if (recordInitLog == false){
			Log.initLog();
			recordInitLog = true;
		}
		if (adb== null){
			adb = new Adb();
			boolean b = adb.reStartAdb();
			if (b == false){
				adb = null;
				throw new Exception("init err");
			}
			b = adb.checkDeviceStatus();
			if (b == false){
				adb = null;
				throw new Exception("adb check err");
			}
		}
		
		if (appium == null){
			appium = new AppiumServer();
			boolean b = appium.startServer();
			if (b ==false){
				appium = null;
				throw new Exception("adb check err");
			}
			appium.waitAppiumServerFinish();
		}
		
		if (recordInitWatch == false){
			Watch.initWatch();
			recordInitWatch = true;
		}
		
		Log.I("call initAll come to an end");
	}
	
	
	public static void recover(){
		Log.I("start to call recover");
		if (appium != null){
			appium.destoryServer();
		}
		Log.I("call recover come to an end");
	}
	
}
