package com.android.Common;

import java.util.Date;

public class Util {
	public static final int YEAR = 1;
	public static final int MONTH = 2;
	public static final int DAY = 3;
	public static final int HOUR = 4;
	public static final int MINUTE = 5;
	public static final int SECOND = 6;
	
	public static long NowInt(){
		return new Date().getTime();
	}
	
	public static String NowFormate(String pre){
		Date date = new Date();
		String f = String.format("%s%d%d%d%d%d", pre,
				formate(date.getYear(),YEAR),
				formate(date.getMonth(),MONTH),
				formate(date.getDay(),DAY),
				formate(date.getHours(),HOUR),
				formate(date.getMinutes(),MINUTE));
		return f;
	}

	public static String formate(int i,int t){
		String str = "";
		switch (t){
		case YEAR:
			for (int index=0;index< 4-(i+"").length();index++){
				str += "0";
			}
			break;
		case MONTH:
		case DAY:
		case HOUR:
		case MINUTE:
		case SECOND:
			if(i <10){
				str += "0";
			}
			str += i;
			break;
		}
		return str;
	}
}
