package com.android.AUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.android.Base.ArrayItem;
import com.android.Base.Item;
import com.android.Common.Tools;
import com.android.Config.ConfUtil;
import com.android.Log.Log;

import io.appium.java_client.NetworkConnectionSetting;
import io.appium.java_client.android.AndroidDriver;

public class AndroidApp {

	private HashMap<Integer, AndroidDriver<WebElement>> driverMap = null;
	private HashMap<Integer, DesiredCapabilities> udidMap = null;
	private HashMap<Integer, ArrayList<Integer>> screenSizeMap = null;
	private HashMap<Integer, String> apkIndeedAddr = null;
	private boolean startAPP;
	private boolean resetNet;
	private int currentIndex = 0;
	private Item item = null;
	private boolean chan;
	private WebElement currentElement = null;
	private String caseName = "default";

	private static String OUT_OF_RANGE = "arry out of range when load elements";

	/**
	 * @param startAPP
	 *            true 每次通过 初始化驱动安装并且 启动app，false 的话 就是 不安装，直接启动
	 * @param resetNet
	 *            重置网络，只启动 wifi 其他的通通关闭
	 * @param caseName
	 *            用例名字
	 * 
	 */
	public AndroidApp(boolean startAPP, boolean resetNet, String caseName) throws Exception {
		this.caseName = caseName;
		this.startAPP = startAPP;
		this.resetNet = resetNet;
		this.initDrivers();
		if (this.resetNet) {
			this.netWork();
		}

	}

	public AndroidApp() throws Exception {
		this(true, false, "default");
	}

	/**
	 * 指定当前设备
	 */
	public AndroidApp setIndex(int currentIndex) {
		this.currentIndex = currentIndex;
		return this;
	}
	
	/**
	 * 获取当前设备编号
	 * 
	 */
	public int getIndex() {
		return this.currentIndex;
	}

	/**
	 * 根据下标获取设备索引
	 * 
	 */
	private String LoadSecByIndex(int index) {
		return "device_" + index;
	}

	/**
	 * 获取当前定位到的元素
	 * 
	 */
	public WebElement getWe() throws Exception {
		if (this.currentElement == null) {
			String msg = "call get the current element";
			Log.W(msg);
			throw new Exception(msg);
		}

		return this.currentElement;
	}

	/**
	 * 获取当前设备驱动
	 * 
	 */
	public AndroidDriver<WebElement> getAD() {
		Log.I("call get the current android webdriver");
		return this.driverMap.get(this.getIndex());
	}

	/**
	 * 获取当前apk的地址
	 * 
	 * @param sec
	 *            配置文件的section
	 * @param key
	 *            某个具体section的关键字
	 */
	private String getApkAddr(String sec, String key) throws Exception {
		String vs = ConfUtil.getStr(sec, key);
		if (vs.endsWith(".apk")) {
			String addr = Tools.LoadPath(new String[] { "apps", vs });
			if (new File(addr).exists()) {
				return addr;
			} else {
				String msg = String.format("do find file in addr -- %s", addr);
				Log.W(msg);
				throw new Exception(msg);
			}

		}

		String apk_name = vs + ".apk";
		String addr = Tools.LoadPath(new String[] { "apps", apk_name });
		File f = new File(addr);
		boolean file_exist = f.exists();
		boolean need_download = false;
		if (ConfUtil.getInt("own", "down_apk_every_time") > 0) {
			if (file_exist) {
				f.delete();
			}
			need_download = true;
		} else {
			if (!file_exist) {
				need_download = true;
			}
		}
		if (need_download) {
			String url = ConfUtil.getStr("own", vs);
			if (url == null || url == "") {
				String msg = "url is empty when wanna down load apk";
				Log.W(msg);
				throw new Exception(msg);
			}
			Tools.downLoadFromUrl(url, apk_name, Tools.LoadPath(new String[] { "apps" }));
		}

		return addr;
	}

	/**
	 * 根据下标获取设备驱动初始化的配置
	 * 
	 */
	@SuppressWarnings("unused")
	private DesiredCapabilities LoadCapa(int index) throws Exception {
		String sec = LoadSecByIndex(index);
		DesiredCapabilities capabilities = new DesiredCapabilities();
		if (this.startAPP) {
			String apk_addr = getApkAddr(sec, "apk");
			apkIndeedAddr.put(index, apk_addr);
			capabilities.setCapability("app", getApkAddr(sec, "apk"));
		}
		capabilities.setJavascriptEnabled(true);
		capabilities.setBrowserName("");
		capabilities.setCapability("deviceName", ConfUtil.getStr(sec, "deviceName"));
		capabilities.setCapability("udid", ConfUtil.getStr(sec, "udid"));
		capabilities.setCapability("platformName", ConfUtil.getStr(sec, "platformName"));
		capabilities.setCapability("platformVersion",
				getSysetmVersion(ConfUtil.getStr(sec, "udid"), ConfUtil.getStr(sec, "platformVersion")));
		capabilities.setCapability("unicodeKeyboard", ConfUtil.getStr(sec, "unicodeKeyboard"));
		capabilities.setCapability("resetKeyboard", ConfUtil.getStr(sec, "resetKeyboard"));
		capabilities.setCapability("appPackage", ConfUtil.getStr(sec, "appPackage"));
		capabilities.setCapability("appActivity", ConfUtil.getStr(sec, "appActivity"));
		capabilities.setCapability("newCommandTimeout",300);
		return capabilities;
	}

	/**
	 * 根据设备编号 和 配置参数连接 nodejs 服务端
	 * 
	 */
	@SuppressWarnings("unused")
	private AndroidDriver<WebElement> ConnecSever(int index, DesiredCapabilities capa) throws MalformedURLException {
		String sec = LoadSecByIndex(index);
		String url = "http://127.0.0.1:" + ConfUtil.getStr(sec, "servevrPort") + "/wd/hub";
		Log.I("DesiredCapabilities are " + capa.toString());
		Log.I("url are " + url);
		AndroidDriver<WebElement> androidDriver = new AndroidDriver<WebElement>(new URL(url), capa);
		return androidDriver;
	}
	
	
	/**
	 * 初始化所有的设备驱动
	 * 
	 */
	private void initDrivers() throws Exception {
		driverMap = new HashMap<Integer, AndroidDriver<WebElement>>();
		udidMap = new HashMap<Integer, DesiredCapabilities>();
		screenSizeMap = new HashMap<Integer, ArrayList<Integer>>();
		apkIndeedAddr = new HashMap<Integer, String>();
		int count = ConfUtil.getInt("own", "device_count");
		for (int i = 0; i < count; i++) {
			DesiredCapabilities capa = LoadCapa(i);
			AndroidDriver<WebElement> driver = ConnecSever(i, capa);
			udidMap.put(i, capa);
			driverMap.put(i, driver);
		}
	}

	/**
	 * 校验 activity
	 * 
	 * @param delay
	 *            表示秒
	 */
	public boolean vertifyActivity(String currentActivity, int delay) {
		for (int i = 0; i < delay; i++) {
			String activity = driverMap.get(this.getIndex()).currentActivity();
			Log.I(String.format("the current activity is %s eager is %s", activity, currentActivity));
			if (activity.trim().equalsIgnoreCase(currentActivity.trim())) {
				return true;
			}
			Tools.Wait(1000);
		}
		return false;
	}

	/**
	 * 加载所有的控件
	 * @param sc
	 *            设备驱动对象
	 * @param we
	 *            具体元素对象
	 * @param type
	 *            定位类型
	 * @param locatorMsg
	 *            定位的具体信息
	 */
	public List<WebElement> LoadAllElements(AndroidDriver<WebElement> sc, WebElement we, String type,
			String locatorMsg) {
		if (sc == null && we == null) {
			sc = this.getAD();
		}
		List<WebElement> list = null;
		if (sc != null) {
			switch (type) {
			case "id":
				list = sc.findElementsById(locatorMsg);
				break;
			case "linkText":
				list = sc.findElementsByLinkText(locatorMsg);
				break;
			case "partialLinkText":
				list = sc.findElementsByPartialLinkText(locatorMsg);
				break;
			case "tagName":
				list = sc.findElementsByTagName(locatorMsg);
				break;
			case "name":
				list = sc.findElementsByName(locatorMsg);
				break;
			case "className":
				list = sc.findElementsByClassName(locatorMsg);
				break;
			case "xpath":
				list = sc.findElementsByXPath(locatorMsg);
				break;
			case "accessibilityId":
				list = sc.findElementsByAccessibilityId(locatorMsg);
				break;
			case "android":
				list = sc.findElementsByAndroidUIAutomator(locatorMsg);
				break;
			case "css":
				list = sc.findElementsByCssSelector(locatorMsg);
				break;
			default:
				Log.W("do not exist this kind of type when the body is android<WebElement>: " + type);
			}
		}
		if (we != null) {
			switch (type) {
			case "id":
				list = we.findElements(By.id(locatorMsg));
				break;
			case "linkText":
				list = we.findElements(By.linkText(locatorMsg));
				break;
			case "partialLinkText":
				list = we.findElements(By.partialLinkText(locatorMsg));
				break;
			case "tagName":
				list = we.findElements(By.tagName(locatorMsg));
				break;
			case "name":
				list = we.findElements(By.name(locatorMsg));
				break;
			case "className":
				list = we.findElements(By.className(locatorMsg));
				break;
			case "xpath":
				list = we.findElements(By.xpath(locatorMsg));
				break;
			case "css":
				list = we.findElements(By.cssSelector(locatorMsg));
				break;
			default:
				Log.W("do not exist this kind of type when the body is WebElement: " + type);
			}
		}

		for (int i = list.size() - 1; i >= 0; i--) {
			if (list.get(i).isDisplayed() == false) {
				list.remove(i);
			}
		}

		Log.I(String.format("type:(%s)   locatorMsg(%s)  size(%d)", type, locatorMsg, list.size()));
		return list;
	}

	/**
	 * 获取指定的控件
	 * @param type
	 *            定位类型
	 * @param locatorMsg
	 *            定位的具体信息
	 * @param index
	 *            第n个元素
	 */
	public AndroidApp LoadElement(String type, String locatorMsg, int index) throws Exception {
		this.item = new Item(type, locatorMsg, index);
		AndroidDriver<WebElement> sc = null;
		WebElement we = null;
		if (chan) {
			we = this.currentElement;
		} else {
			sc = this.driverMap.get(this.getIndex());
		}
		List<WebElement> list = this.LoadAllElements(sc, we, type, locatorMsg);
		// 当下标越界的时候 抛出异常
		if (list.size() < index + 1) {
			Log.W(OUT_OF_RANGE);
			String file_addr = Tools.LoadPath(new String[] { "screenshot" }, true) + File.separator + this.caseName
					+ "_" + this.item.toString() + ".png";
			this.ScreenShot(file_addr);
			throw new Exception(OUT_OF_RANGE);
		} else {
			this.currentElement = list.get(index);
		}
		return this;
	}

	/**
	 * 重置链式 和 当前的元素
	 */
	private void reSetCACE() {
		this.chan = false;
		this.currentElement = null;
	}

	public AndroidApp LoadElement(String locatorMsg) throws Exception {
		this.reSetCACE();
		return LoadElement("id", locatorMsg, 0);
	}

	public AndroidApp LoadElement(String type, String locatorMsg) throws Exception {
		this.reSetCACE();
		return LoadElement(type, locatorMsg, 0);
	}

	public AndroidApp LoadElement(String locatorMsg, int index) throws Exception {
		this.reSetCACE();
		return LoadElement("id", locatorMsg, index);
	}

	/**
	 * 通过动态数组的方式链式定位
	 */
	public AndroidApp LoadElement(ArrayList<Item> items) throws Exception {
		for (int i = 0; i < items.size(); i++) {
			if (i <= 0) {
				this.reSetCACE();
			}
			Item it = items.get(i);
			LoadElement(it.getType(), it.getLocatorStr(), it.getIndex());
		}
		return this;
	}
	
	
	public AndroidApp LoadElement(ArrayItem ai) throws Exception{
		return LoadElement(ai.getItems());
	}
	
	/**
	 * 通过数组的方式链式定位
	 */
	public AndroidApp LoadElement(Item[] items) throws Exception {
		ArrayList<Item> tmps = new ArrayList<Item>();
		for (int i = 0; i < items.length; i++) {
			tmps.add(items[i]);
		}
		return LoadElement(tmps);
	}

	/**
	 * 设置网络状态
	 */
	public void netWork() {
		String udid = (String) udidMap.get(this.getIndex()).asMap().get("udid");
		AndroidDriver<WebElement> driver = driverMap.get(this.getIndex());
		int status = driver.getNetworkConnection().value;
		Log.I(udid + " the current network status is " + status);
		// 只开启wifi
		driver.setNetworkConnection(new NetworkConnectionSetting(false, true, false));
		Log.I(udid + " call to set the wifi on");
	}

	/**
	 * 打开 指定的 activity
	 */
	public void startActivity(String udid, String pack, String activity) {
		String cmd = "adb -s " + udid + " shell su root am start -n " + pack + "/" + activity;
		Tools.ExecCmd(cmd);
	}

	public void startActivity(String activity) {
		this.startActivity(LoadUdid(), LoadPackage(), activity);
	}

	public void startActivity() {
		this.startActivity(LoadUdid(), LoadPackage(), LoadActivity());
	}

	/**
	 * 获取 系统版本
	 */
	public String getSysetmVersion(String udid, String defau) {
		String cmd = "adb -s " + udid + " shell getprop ro.build.version.release";
		String ResMsg = Tools.ExecCmd(cmd);
		if (ResMsg.length() <= 0) {
			return defau;
		}
		return ResMsg;
	}

	public String getSysetmVersion(String udid) {
		return getSysetmVersion(udid, "5.0.0");
	}

	/**
	 * 卸载 app
	 */
	public boolean uninstallApp(String udid, String pack) {
		String cmd = "adb -s " + udid + " uninstall " + pack;
		String ResMsg = Tools.ExecCmd(cmd);
		if (ResMsg.contains("Success") == true) {
			return true;
		}
		return false;
	}

	public boolean uninstallApp() {
		return this.uninstallApp(LoadUdid(), LoadPackage());
	}

	/**
	 * 安装 app
	 */
	public boolean installApp(String udid, String app_addr) {
		String cmd = "adb -s " + udid + " install " + app_addr;
		String ResMsg = Tools.ExecCmd(cmd);
		if (ResMsg.contains("Success") == true) {
			return true;
		}
		return false;
	}

	public boolean installApp() throws Exception {
		return this.installApp(LoadUdid(), LoadApk());
	}

	/**
	 * 校验 app 是否已经安装
	 */
	public boolean checkAppIsInstall(String udid, String pack) {
		String cmd = "adb -s " + udid + " shell pm list packages";
		String ResMsg = Tools.ExecCmd(cmd);
		String[] strs = ResMsg.split("\n");
		for (int i = 0; i < strs.length; i++) {
			if (strs[i].contains(pack)) {
				Log.I(pack + " is search by pm list packages");
				return true;
			}
		}
		return false;
	}

	public boolean checkAppIsInstall() {
		return this.checkAppIsInstall(LoadUdid(), LoadPackage());
	}

	/**
	 * 强制安装
	 */
	public boolean forceInstall() throws Exception {
		if (checkAppIsInstall()) {
			uninstallApp();
		}
		return installApp();
	}

	private String LoadUdid() {
		return (String) udidMap.get(this.getIndex()).asMap().get("udid");
	}

	private String LoadPackage() {
		return (String) udidMap.get(this.getIndex()).asMap().get("appPackage");
	}

	private String LoadActivity() {
		return (String) udidMap.get(this.getIndex()).asMap().get("appActivity");
	}

	private String LoadApk() throws Exception {
		if (apkIndeedAddr.keySet().contains(this.getIndex()) == false) {
			apkIndeedAddr.put(this.getIndex(), getApkAddr(LoadSecByIndex(this.getIndex()), "apk"));
		}
		return apkIndeedAddr.get(this.getIndex());
	}

	public static final int UP = 1;
	public static final int RIGHT = 2;
	public static final int DOWN = 3;
	public static final int LEFT = 4;

	/**
	 * 整个屏幕的滑动
	 * 
	 * @param duration
	 *            这个滑动总的耗时
	 * @param t
	 *            滑动类型        UP   RIGHT	DOWN	LEFT
	 */
	public void Swipe(int duration, int t) throws Exception {
		ArrayList<Integer> tmp = LoadScreenSize(LoadUdid());
		Swip(0, 0, tmp.get(0), tmp.get(1), t, duration, LoadUdid());
	}
	
	/**
	 * 指定元素的滑动
	 * 
	 * @param web
	 *            指定待滑动的元素
	 * @param duration
	 *            这个滑动总的耗时
	 * @param t
	 *            滑动类型        UP   RIGHT	DOWN	LEFT
	 */
	public void Swipe(WebElement web, int duration, int t) throws Exception {
		Swip(web.getLocation().getX(), web.getLocation().getY(), web.getSize().getWidth(), web.getSize().getHeight(), t, duration, LoadUdid());
	}

	/**
	 * 获取当前设备的分辨率 
	 * 
	 */
	private ArrayList<Integer> ScreenSize(String udid) {
		ArrayList<Integer> res = new ArrayList<Integer>();
		String cmd = String.format("adb -s %s shell wm size", udid);
		String msg = Tools.ExecCmd(cmd);
		if (msg.contains("Physical size")) {
			msg = msg.replace("Physical size", "").replace(":", "").trim();
			String[] tmps = msg.split("x");
			if (tmps.length == 2) {
				res.add(Integer.parseInt(tmps[0]));
				res.add(Integer.parseInt(tmps[1]));
			}
		}
		return res;
	}

	private ArrayList<Integer> LoadScreenSize(String udid) throws Exception {
		if ((screenSizeMap.keySet().contains(this.getIndex()) == false)
				|| (screenSizeMap.get(this.getIndex()).size() < 2)) {
			screenSizeMap.put(this.getIndex(), ScreenSize(udid));
		}
		ArrayList<Integer> tmp = screenSizeMap.get(this.getIndex());
		if (tmp == null) {
			String msg = "do not load the sereenSize from adb";
			Log.W(msg);
			throw new Exception(msg);
		}
		return tmp;
	}

	
	/**
	 * 
	 * 	*********************************************
	 * 	*											*
	 * 	*								**			*
	 * 	*								 **			*
	 * 	*		****************************		*
	 * 	*								 **			*
	 * 	*								**			*
	 * 	*											*
	 * 	*********************************************
	 */
	
	/**
	 * 滑动
	 * 
	 * @param x
	 *            滑动区域的x坐标
	 * @param y
	 *            滑动区域的y坐标
	 * @param width
	 *            滑动区域的宽度
	 * @param height
	 *            滑动区域的高度
	 * @param duration
	 *            这个滑动总的耗时
	 * @param t
	 *            滑动类型        UP   RIGHT	DOWN	LEFT
	 */
	public void Swip(int x, int y, int width, int height, int t, int duration, String udid) {
		int x2 = x + width;
		int y2 = y + height;
		int begin_x, begin_y;
		int end_x, end_y;
		switch (t) {
		case UP:
			begin_x = x + width / 2;
			end_x = x + width / 2;
			begin_y = y + height * 4 / 5;
			end_y = y + height * 1 / 5;
			break;
		case RIGHT:
			begin_y = y + height / 2;
			end_y = y + height / 2;
			begin_x = x + width * 1 / 5;
			end_x = x + width * 4 / 5;
			break;
		case DOWN:
			begin_x = x + width / 2;
			end_x = x + width / 2;
			begin_y = y + height * 1 / 5;
			end_y = y + height * 4 / 5;
			break;
		default:
			begin_y = y + height / 2;
			end_y = y + height / 2;
			begin_x = x + width * 4 / 5;
			end_x = x + width * 1 / 5;
		}
		String cmd = String.format("adb -s %s shell input swipe %d %d %d %d %d", udid, begin_x, begin_y, end_x, end_y,
				duration);
		Tools.ExecCmd(cmd);
	}
	
	/**
	 * 返回操作
	 */
	public void back() {
		String cmd = String.format("adb -s %s shell input keyevent 4", LoadUdid());
		Tools.ExecCmd(cmd);
	}

	/**
	 * 给光标所在处 发送文本，不支持中文
	 */
	public void sendKeyByAdb(String msg) throws Exception {
		this.getWe().click();
		String cmd = String.format("adb -s %s shell input text %s", LoadUdid(), msg);
		Tools.ExecCmd(cmd);
	}

	/**
	 * 发送文本，支持中文
	 */
	public void ClearAndSend(String msg) throws Exception {
		this.getWe().clear();
		this.getWe().sendKeys(msg);
	}

	/**
	 * 点击元素
	 */
	public void click() throws Exception {
		this.getWe().click();
	}
	
	/**
	 * 点击元素并隐式等待
	 */
	public void clickAndWait(int wait) throws Exception {
		click();
		Tools.Wait(wait);
	}
	
	/**
	 * 截图
	 * @param file_addr
	 *            图片路径
	 */
	@SuppressWarnings("finally")
	public boolean ScreenShot(String file_addr) {
		Log.I(String.format("screen file Addr is %s", file_addr));
		File screenshot = this.getAD().getScreenshotAs(OutputType.FILE);
		FileInputStream imgIs;
		boolean success = false;
		try {
			imgIs = new FileInputStream(screenshot);
			FileOutputStream imageOs = new FileOutputStream(new File(file_addr));
			FileChannel imgCin = imgIs.getChannel();
			FileChannel imgCout = imageOs.getChannel();
			imgCin.transferTo(0, imgCin.size(), imgCout);
			imgCin.close();
			imgCout.close();
			imgIs.close();
			imageOs.close();
			success = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.W("截图失败");
		} finally{
			return success;
		}
	}

	/**
	 * 点击
	 * @param x,y
	 *            点击坐标
	 * @param duration
	 *           点击时长
	 */
	public void tap(int x, int y, int duration) {
		AndroidDriver<WebElement> driver = driverMap.get(this.getIndex());
		driver.tap(1, x, y, duration);
	}

	/**
	 * 点击,也可以用于长按
	 * @param x,y
	 *            点击坐标
	 * @param duration
	 *           点击时长
	 * @param udid
	 *           设备id
	 */
	public void tapByAdb(int x, int y, int duration, String udid) {
		String cmd = String.format("adb -s %s shell input swipe %d %d %d %d %d", udid, x, y, x, y, duration);
		Tools.ExecCmd(cmd);
	}
	
	/**
	 * 点击
	 * @param x,y
	 *            点击坐标
	 * @param duration
	 *           点击时长
	 */
	public void tapByAdb(int x, int y, int duration) {
		tapByAdb( x,  y,  duration, LoadUdid());
	}

	/**
	 * 点击屏幕中间
	 */
	public void tapCenter() throws Exception {
		ArrayList<Integer> tmp = LoadScreenSize(LoadUdid());
		tapByAdb(tmp.get(0) / 2, tmp.get(1) / 2, 10);
	}
	
	/**
	 * 仅仅用于点击
	 * @param x,y
	 *            点击坐标
	 */
	public void tapOnly(int x,int y){
		String cmd = String.format("adb -s %s shell input tap %d %d", LoadUdid(), x, y);
		Tools.ExecCmd(cmd);
	}

	
	/**
	 * 在执行测试用例之前可能会出现的 意外情况的处理
	 * 前提是要在 Watch 中先注册意外情况
	 * 
	 */
	public AndroidApp check() throws Exception {
		ArrayList<ArrayList<Item>> record = Watch.LoadWatch();
		for (int i = 0; i < record.size(); i++) {
			ArrayList<Item> tmp = record.get(i);
			WebElement we_ = null;
			for (int j = 0; j < tmp.size(); j++) {
				if (j > 0) {
					we_ = getWe();
				}
				List<WebElement> list = LoadAllElements(null, we_, tmp.get(j).getType(), tmp.get(j).getLocatorStr());
				if (list.size() > tmp.get(j).getIndex()) {
					this.currentElement = list.get(tmp.get(j).getIndex());
					HashMap<String, Object> tmm = tmp.get(j).getMap();
					if (tmm.keySet().contains("click")) {
						this.getWe().click();
					}
				}
			}
		}
		return this;
	}
	
	/**
	 * 显式等待元素
	 * 
	 */
	public AndroidApp waitElement(String type,String locatorMsg,int ts){
		for (int i=0;i<ts;i++){
			if (LoadAllElements(null, null, type, locatorMsg).size() > 0){
				return this;
			}
			Tools.Wait(1000);
		}
		Log.W(String.format("wait for type(%s) locatorMsg(%s) with 20s but still can not find it", type,locatorMsg));
		return this;
	}
	
	/**
	 * 处理事件之前先行校验元素，默认超时为20s
	 * 
	 */
	private int checktimes = 20;	
	
	/**
	 * 设置元素等待超时时间
	 * 
	 */
	public AndroidApp setCheckTimes(int i ){
		checktimes = i;
		return this;
	}
	/**
	 * 获取元素超时时间
	 * 
	 */
	public int LoadChecktimes(){
		return checktimes;
	}
	
	public AndroidApp WaitAndLoadElement(String type,String locatorMsg,int index) throws Exception{
		waitElement(type,locatorMsg,LoadChecktimes());
		return LoadElement(type,locatorMsg,index);
	}
	
	

	public AndroidApp WaitAndLoadElement(String locatorMsg) throws Exception {
		waitElement("id",locatorMsg,LoadChecktimes());
		return LoadElement(locatorMsg);
	}
	
	public AndroidApp WaitAndLoadElement(String type,String locatorMsg) throws Exception {
		waitElement(type,locatorMsg,LoadChecktimes());
		return LoadElement(type,locatorMsg);
	}
	
	
	public AndroidApp WaitAndLoadElement(String locatorMsg,int index) throws Exception {
		waitElement("id",locatorMsg,LoadChecktimes());
		return LoadElement(locatorMsg,index);
	}
	
	

	public AndroidApp WaitAndLoadElement(ArrayList<Item> items) throws Exception {
		waitElement(items.get(0).getType(),items.get(0).getLocatorStr(),LoadChecktimes());
		return LoadElement(items);
	}

	
	
	public AndroidApp WaitAndLoadElement(Item[] items) throws Exception {
		ArrayList<Item> tmps = new ArrayList<Item>();
		for (int i = 0; i < items.length; i++) {
			tmps.add(items[i]);
		}
		return WaitAndLoadElement(tmps);
	}

	/**
	 * 强制性等待
	 * 
	 */
	public AndroidApp WaitOnly(int wait){
		Tools.Wait(wait);
		return this;
	}
	
	/**
	 * 获取元素的文本内容
	 * 
	 */
	public String getText() throws Exception{
		return this.getWe().getText();
	}
	
	/**
	 * 根据页面等待情况
	 * 
	 * @param item
	 *            等待元素的条件数组对象
	 * @param time_out
	 *            超时 单位毫秒
	 */
	public boolean waitConditions(ArrayItem ar,int time_out){
		ArrayList<Item> items = ar.getItems();
		for(int i=0;i<time_out/1000;i++){
			for (int j=0;j<items.size();j++){
				if (LoadAllElements(null,null,items.get(j).getType(),items.get(j).getLocatorStr()).size() > 0){
					return true;
				}
			}
			WaitOnly(1000);
		}
		Log.W(String.format("%s wait for %d conditions for more than %s ms", ar.toString(),items.size(),time_out));
		return false;
	}
	
	/**
	 * 根据页面等待情况
	 * 
	 * @param item
	 *            等待元素的条件对象
	 * @param time_out
	 *            超时 单位毫秒
	 */
	public boolean waitConditions(Item item,int time_out){
		return waitConditions(new ArrayItem().add(item),time_out);
	}
	
	
	private String[] composeAddr;
	
	/**
	 * 设置测试中用于校验的文件放置的位置
	 * 
	 */
	public AndroidApp setCompose(String[] compose){
		composeAddr = compose;
		return this;
	}
	
	/**
	 * 获取校验文件的路劲
	 * 
	 */
	public String[] getCompose(){
		return composeAddr;
	}
	
	/**
	 * 获取校验文件的绝对路径
	 * 
	 */
	public String getActualAddr(String name){
		String[] arryAddr = new String[getCompose().length + 1];
		for(int i=0;i<getCompose().length;i++){
			arryAddr[i] = getCompose()[i];
		}
		arryAddr[arryAddr.length-1] = name;
		return Tools.LoadPath(arryAddr);
	}
	
	
	/**
	 * 根据图片位置来触发点击事件
	 * 
	 * @param from
	 *            原图片
	 * @param tos
	 *            目标图片数组
	 * @param rate
	 *            图片相似度 百分比
	 * @param upsidedown
	 *            是否颠倒 屏幕的宽和高
	 * 
	 */
	public void clickBySearchPic(String from,String[] tos,double rate,boolean upsidedown) throws Exception{
		String[] tos_tmp = new String[tos.length];
		for(int i=0;i<tos.length;i++){
			tos_tmp[i] = getActualAddr(tos[i]);
		}
		ArrayList<Double> ars = Tools.searchLocationFromImage(getActualAddr(from),tos_tmp,rate);
		if (ars.size() != 4){
			String msg = String.format("do not search any simalar from %s to %s",from,tos.toString());
			Log.W(msg);
			throw new Exception(msg);
		}
		ArrayList<Integer> tmp = LoadScreenSize(LoadUdid());
		int x,y;
		
		if (upsidedown){
			x = (int)(((double)tmp.get(1)) * (ars.get(0) + ars.get(2)/2));
			y = (int)(((double)tmp.get(0)) * (ars.get(1) + ars.get(3)/2));
		}else{
			x = (int)(((double)tmp.get(0)) * (ars.get(0) + ars.get(2)/2));
			y = (int)(((double)tmp.get(1)) * (ars.get(1) + ars.get(3)/2));
		}
		tapOnly(x,  y);
	}
	
	
	
	
	
}