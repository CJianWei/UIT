package com.android.AUI;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.Base.Item;


public class Watch {
	public static ArrayList<ArrayList<Item>> record = null ;
	
	public static void addWatch( Item item){
		ArrayList<Item> tmp = new ArrayList<Item>();
		tmp.add(item);
		addWatch(tmp);
	}
	
	public static void addWatch(ArrayList<Item> tmp){
		if (record == null){
			record = new ArrayList<ArrayList<Item>>();
		}
		record.add(tmp);
	}
	
	public static ArrayList<ArrayList<Item>> LoadWatch(){
		if (record == null){
			record = new ArrayList<ArrayList<Item>>();
		}
		return record;
	}
	
	public static void initWatch(){
		HashMap<String,Object> tmp = new HashMap<String,Object>();
		tmp.put("click", null);
		addWatch(new Item("id","android:id/button1",0,tmp));
	}
	
}