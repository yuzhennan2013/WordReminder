package cn.sina.youxi.util;

import java.util.ArrayList;
import java.util.HashMap;

/** 
 * 类说明：   Map工具类 
 * @author  Cundong
 * @date    Feb 15, 2012 5:30:51 AM 
 * @version 1.0
 */
public class MapUtils {

	/**
	 * 从HashMap中获取字符串属性
	 * @param  map
	 * @param  key
	 * @return
	 */
	public static String getString(HashMap<String, ?> map, String key) {
		String value = "";
		if (map != null && map.containsKey(key)) {
			value = null != map.get(key) ? map.get(key).toString() : "";
		}
		return value;
	}

	/**
	 * 从HashMap中获取整型属性
	 * @param map
	 * @param key
	 * @return
	 */
	public static int getInteger(HashMap<String, ?> map, String key) {
		int value = 0;
		if (map != null && map.containsKey(key)) {
			value = null != map.get(key) ? Integer.parseInt(map.get(key)
					.toString()) : 0;
		}
		return value;
	}

	/**
	 * 从HashMap中获取Boolean属性
	 * @param map
	 * @param key
	 * @return
	 */
	public static boolean getBoolean(HashMap<String, ?> map, String key) {
		boolean value = false;
		if (map != null && map.containsKey(key)) {
			value = null != map.get(key) ? Boolean.parseBoolean(map.get(key)
					.toString()) : false;
		}
		return value;
	}

	/**
	 * 从HashMap中获取List
	 * @param map
	 * @param key
	 * @return
	 */
	public static ArrayList<HashMap<String, String>> getList(
			HashMap<String, ArrayList<HashMap<String, String>>> map, String key) {
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		if (map != null && map.containsKey(key)) {
			list = map.get(key);
		}
		return list;
	}
}
