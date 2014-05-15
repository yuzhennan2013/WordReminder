package cn.sina.youxi.util;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

/**
 * 类说明： 屏幕分辨率工具类
 * 
 * @author Cundong
 * @date 2012-3-5
 * @version 1.0
 */
public class ScreenUtils {
	private static HashMap<String, Integer> SCREEN_ID_MAP = new HashMap<String, Integer>();
	private static int SCREEN_HEIGHT_ARR[] = { 320, 480, 800, 854, 960, 1024,
			1280 };

	static {
		// 小
		SCREEN_ID_MAP.put("320X240", 1);

		// 中
		SCREEN_ID_MAP.put("480X320", 2);

		// 大
		SCREEN_ID_MAP.put("800X480", 3);
		SCREEN_ID_MAP.put("854X480", 4);

		SCREEN_ID_MAP.put("960X640", 4);
		SCREEN_ID_MAP.put("960X540", 4);

		SCREEN_ID_MAP.put("1024X600", 5);
		SCREEN_ID_MAP.put("1024X768", 5);

		SCREEN_ID_MAP.put("1280X720", 6);
		SCREEN_ID_MAP.put("1280X800", 6);
	}

	/**
	 * 根据分辨率获取屏幕尺寸ID
	 * 
	 * @param height
	 * @param width
	 * @return
	 */
	public static int getScreenID(int height, int width) {
		String key = height + "X" + width;
		if (SCREEN_ID_MAP.containsKey(key)) {
			return SCREEN_ID_MAP.get(key);
		} else {
			if (height <= SCREEN_HEIGHT_ARR[0]) {
				return SCREEN_ID_MAP.put("320X240", 1);
			} else if (height > SCREEN_HEIGHT_ARR[0]
					&& height <= SCREEN_HEIGHT_ARR[1]) {
				return SCREEN_ID_MAP.put("320X240", 1);
			} else if (height > SCREEN_HEIGHT_ARR[1]
					&& height <= SCREEN_HEIGHT_ARR[2]) {
				SCREEN_ID_MAP.put("480X320", 2);
			} else if (height > SCREEN_HEIGHT_ARR[2]
					&& height <= SCREEN_HEIGHT_ARR[3]) {
				SCREEN_ID_MAP.put("800X480", 3);
			} else if (height > SCREEN_HEIGHT_ARR[3]
					&& height <= SCREEN_HEIGHT_ARR[4]) {
				SCREEN_ID_MAP.put("854X480", 4);
			} else if (height > SCREEN_HEIGHT_ARR[4]
					&& height <= SCREEN_HEIGHT_ARR[5]) {
				SCREEN_ID_MAP.put("960X540", 4);
			} else if (height > SCREEN_HEIGHT_ARR[5]
					&& height <= SCREEN_HEIGHT_ARR[6]) {
				SCREEN_ID_MAP.put("1024X768", 5);
			} else if (height >= SCREEN_HEIGHT_ARR[6]) {
				SCREEN_ID_MAP.put("1280X720", 5);
			}
		}
		return -1;
	}

	public static Point getScreenDimen(Activity activity) {
		Point size = new Point();
		WindowManager w = activity.getWindowManager();
		w.getDefaultDisplay().getSize(size);
		return size;
	}
}