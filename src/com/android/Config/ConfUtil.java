package com.android.Config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;


public class ConfUtil {

	private static HashMap<String, Properties> sections = new HashMap<String, Properties>();
	private static String currentSecion;
	private static Properties current;

	/**
	 * 初始化配置文件
	 */
	public static void initConf(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		read(reader);
		reader.close();
	}

	private static void read(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			parseLine(line);
		}
	}

	/**
	 * 解析配置文件 当出现#的时候表示备注 会忽略 当以[]的默认为section 当a= k的表示键值对
	 * 
	 */
	private static void parseLine(String line) {
		line = line.trim();
		if (line.startsWith("#")) {

		} else if (line.matches("\\[.*\\]")) {
			currentSecion = line.replaceFirst("\\[(.*)\\]", "$1");
			current = new Properties();
			sections.put(currentSecion, current);
		} else if (line.matches(".*=.*")) {
			if (current != null) {
				int i = line.indexOf('=');
				String name = line.substring(0, i);
				String value = line.substring(i + 1);
				current.setProperty(name, value);
			}
		}
	}

	/**
	 * 获取某个section下的key值
	 * 
	 */
	public static String getStr(String section, String name) {
		Properties p = (Properties) sections.get(section);
		if (p == null) {
			return null;
		}
		String value = p.getProperty(name);
		return value;
	}

	public static int getInt(String section, String name) {
		Integer it = new Integer(getStr(section, name));
		return it.intValue();
	}

}
