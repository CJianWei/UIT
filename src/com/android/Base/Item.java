package com.android.Base;

import java.util.HashMap;

public class Item {
	private String type;//定位類型
	private String locatorStr; //定位元素内容
	private int  index; //元素下標
	private HashMap<String,Object> map;
	
	public Item(String type,String locatorStr,int index){
		this.type = type;
		this.locatorStr = locatorStr;
		this.index = index;
	}
	
	public Item(String type,String locatorStr,int index,HashMap<String,Object> map){
		this.type = type;
		this.locatorStr = locatorStr;
		this.index = index;
		this.map = map;
	}
	
	public Item(String type,String locatorStr){
		this(type,locatorStr,0);
	}

	public Item(String locatorStr,int index){
		this("id",locatorStr,index);
	}
	
	public Item(String locatorStr){
		this("id",locatorStr,0);
	}
	
	
	public void setType(String type){
		this.type = type;
	}
	
	public void setLocatorStr(String locatorStr){
		this.locatorStr = locatorStr;
	}
	

	public void setIndex(int index){
		this.index = index;
	}
	
	public String getType(){
		return this.type;
	}
	
	public String getLocatorStr(){
		return this.locatorStr;
	}
	
	public int getIndex(){
		return this.index;
	}
	
	public HashMap<String,Object> getMap(){
		return this.map;
	}
	
	public String toString(){
		return String.format("%s_%s_%d",getType(),getLocatorStr().replaceAll("/", "_").replace(".", "_").replace(":", "_"),getIndex());
	}
}