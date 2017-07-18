package com.android.Base;

import java.util.HashMap;

public class Cd {
	private int code;
	private HashMap<String, Object> data;
	
	public Cd(){
		code = -1;
	}
	
	public HashMap<String, Object> getData(){
		return data;
	}
	
	public int getCode(){
		return code;
	}
}
