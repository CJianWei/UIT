package com.android.Base;

import java.util.ArrayList;

public class ArrayItem {
	private ArrayList<Item> items = null;
	
	public ArrayItem(){
		items = new ArrayList<Item>();
	}
	
	public ArrayItem add(Item item){
		items.add(item);
		return this;
	}
	
	public ArrayList<Item> getItems(){
		return items;
	}
	
	public String toString(){
		String str ="";
		for (int i=0;i<items.size();i++){
			str += String.format("{type:%s,location:%s}  ", items.get(i).getType(),items.get(i).getLocatorStr());
		}
		return str;
		
	}
}
