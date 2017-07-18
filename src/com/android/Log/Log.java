package com.android.Log;

import java.util.logging.Logger;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class Log {
	private static Logger logger = null;
	private static int Level = 1;

	public static void initLog(String name) throws SecurityException, IOException {
		logger = Logger.getLogger("Log");
		FileHandler fileHandler = new FileHandler(name);
		fileHandler.setFormatter(new LogFormatter());
		logger.addHandler(fileHandler);
	}

	/**
	 * 
	 * 初始化日志模块
	 */
	public static void initLog() throws SecurityException, IOException {
		initLog(Util.Now("yyyy-MM-dd-HH-mm")+ ".log");
	}

	
	public static boolean initLogIfNecessary(){
		if (logger==null){
			try {
				initLog();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	public static void I(String str) {
		if (initLogIfNecessary()){
			logger.info(str);
		}
		
	}


	public static void W(String str) {
		if (initLogIfNecessary()){
			logger.warning(str);
		}
		
	}

}

class Util{
	public static String Now(String formate){
		if (formate.equals("")) {
			formate = "yyyy-MM-dd HH:mm:ss";
		}
		return new SimpleDateFormat(formate).format(new Date());
	}
}

class LogFormatter extends Formatter {
	@Override
	public String format(LogRecord record) {
		return String.format("[ %s ]--> %s\n", Util.Now(""),record.getMessage());
	}

}