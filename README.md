# UIT
UI Automator test for android 


### Features
* you can click by choose Element or location with (x,y)
* pic similar/compared is included for assert
* interactive case is supported thus you can test chat with differ user

##how to use?
* initAll:
    *  the follow codes will init conf 、log、adb、appium,thus reduce duplicate code
```
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
```

* AndroidApp:
    *  func desc will show in class AndroidApp
```
    AndroidApp ui = new AndroidApp(false, false, "Test_A");
    ui.LoadElement("a").click();
    ui.LoadElement("com.dy.kuxiao:id/ed_content").ClearAndSend(msg)
    ui.waitConditions(new Item("a"), 10000)
```