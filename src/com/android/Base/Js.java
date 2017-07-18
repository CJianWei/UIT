package com.android.Base;

import java.util.HashMap;

/**
 * http://127.0.0.1:4723/wd/hub/status 的返回對象
 */
public class Js {
	private int status;
	private HashMap<String, Object> value;

	public Js() {
		status = -1;
	}

	public int getStatus() {
		return this.status;
	}
}
