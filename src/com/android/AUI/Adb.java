package com.android.AUI;

import java.util.HashMap;

import com.android.Common.Tools;
import com.android.Config.ConfUtil;


public class Adb {

	private HashMap<String, String> deviceMap = null;

	public Adb() {
		deviceMap = new HashMap<String, String>();
	}

	/**
	 * 启动 adb
	 */
	public boolean startAdb() {
		String cmd = "adb start-server";
		String msg = Tools.ExecCmd(cmd);
		if (msg.contains("successfully")) {
			return true;
		}
		return false;
	}

	/**
	 * 关闭 adb
	 */
	public void stopAdb() {
		String cmd = "adb kill-server";
		Tools.ExecCmd(cmd);
	}

	/**
	 * 重启 adb
	 */
	public boolean reStartAdb() {
		stopAdb();
		return startAdb();
	}

	/**
	 * 获取所有设备的udid 状态
	 */
	public void LoadDeviceMsg() {
		deviceMap = new HashMap<String, String>();
		String cmd = "adb devices";
		String msg = Tools.ExecCmd(cmd);
		String[] lines = msg.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String tmp = lines[i].trim();
			if (tmp.startsWith("*") || tmp.startsWith("List")) {
				continue;
			}
			String[] ary_tmps = tmp.split("\t");
			if (ary_tmps.length != 2) {
				continue;
			}
			deviceMap.put(ary_tmps[0], ary_tmps[1]);
		}
	}

	/**
	 * 校验配置文件中的udid是否为device
	 */
	public boolean checkDeviceStatus() {
		LoadDeviceMsg();
		int count = ConfUtil.getInt("own", "device_count");
		for (int i = 0; i < count; i++) {
			String udid = ConfUtil.getStr("device_" + i, "udid");
			String v = deviceMap.get(udid);
			if ((v == null) || (!v.equals("device"))) {
				return false;
			}
		}
		return true;
	}

}

