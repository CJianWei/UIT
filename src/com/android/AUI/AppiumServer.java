package com.android.AUI;

import java.util.HashMap;

import com.android.Base.Js;
import com.android.Common.Tools;
import com.android.Config.ConfUtil;

public class AppiumServer {
	

	private HashMap<Integer, Process> deviceMap = null;

	public AppiumServer() {
		deviceMap = new HashMap<Integer, Process>();
	}

	/**
	 * 开启单个服务
	 */
	public Process startSingleServer(int index) {
		String servevrPort = ConfUtil.getStr("device_" + index, "servevrPort");
		String bsPort = ConfUtil.getStr("device_" + index, "bsPort");
		String format = "appium.cmd --address 127.0.0.1 --port %s -bp %s --platform-name Android"
				+ " --platform-version 23 --automation-name Appium --log-no-color --session-override";
		// --full-reset
		String cmd = String.format(format, servevrPort, bsPort);
		appiumThread thread_tmp = new appiumThread(cmd);
		thread_tmp.start();
		Tools.Wait(1000);
		return Tools.loadProcess();
	}

	/**
	 * 开启所有的服务
	 */
	public boolean startServer() {
		Tools.ExecCmd("taskkill /F /IM node.exe");
		Tools.Wait(3000);
		int count = ConfUtil.getInt("own", "device_count");
		for (int i = 0; i < count; i++) {
			Process p = startSingleServer(i);
			if (p == null) {
				return false;
			}
			deviceMap.put(i, p);
		}
		return true;
	}

	/**
	 * 通过http请求，获取status 为0 则是服务启动成功
	 */
	public boolean checkSingleServer(int index) {
		String servevrPort = ConfUtil.getStr("device_" + index, "servevrPort");
		String url = String.format("http://127.0.0.1:%s/wd/hub/status?", servevrPort);
		Js js = Tools.HGet(url, Js.class);
		if (js.getStatus() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 校验所有的服务启动状态
	 */
	public boolean checkServer() {
		int count = ConfUtil.getInt("own", "device_count");
		for (int i = 0; i < count; i++) {
			if (!checkSingleServer(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 等待所有的服务启动成功之后才退出
	 */
	public void waitAppiumServerFinish() throws Exception {
		for (int i = 0;; i++) {
			if (i > 50) {
				String msg = "wait 50s but the appium do not finish";
				throw new Exception(msg);
			}
			Tools.Wait(1000);
			if (checkServer()) {
				return;
			}
		}
	}

	/**
	 * 关闭单个appium服务
	 */
	public void destorySingleServer(int index) {
		Process p = deviceMap.get(index);
		if (p != null) {
			p.destroy();
		}
		String servevrPort = ConfUtil.getStr("device_" + index, "servevrPort");
	}

	/**
	 * 关闭appium服务
	 */
	public void destoryServer() {
		int count = ConfUtil.getInt("own", "device_count");
		for (int i = 0; i < count; i++) {
			destorySingleServer(i);
		}
		Tools.ExecCmd("taskkill /F /IM node.exe");
	}

	/**
	 * appium 线程，为防止阻塞主线程
	 */
	class appiumThread extends Thread {
		private String cmd;

		public appiumThread(String cmd) {
			this.cmd = cmd;
		}

		@Override
		public void run() {
			Tools.ExecCmd(cmd, false);
		}
	}

}