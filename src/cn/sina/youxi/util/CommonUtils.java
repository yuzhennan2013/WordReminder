package cn.sina.youxi.util;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
/**
 * 类说明： 公用工具类
 * 
 * @author Cundong
 * @date 2012-2-7
 * @version 1.0
 */
public class CommonUtils {

	// 单机游戏，packageName的后缀
	public static final String SINA_SINGLE_FALG = "_sina";

	// 网游，packageName的后缀
	public static final String SINA_OL_FALG = "_wyx";

	/**
	 * 将Key-value转换成用&号链接的URL查询参数形式
	 * 
	 * @param parameters
	 * @return
	 */
	public static String encodeUrl(Bundle parameters) {
		if (parameters == null) { return ""; }

		ArrayList<String> list = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String key : parameters.keySet()) {
			list.add(key);
		}
		Collections.sort(list);
		for (String key : list) {
			if (first) first = false;
			else sb.append("&");
			if (parameters.getString(key) != null) {
				sb.append(URLEncoder.encode(key) + "="
						+ URLEncoder.encode(parameters.getString(key)));
			}
		}
		return sb.toString();
	}

	/**
	 * 获取某值在数组中的下标位置
	 * 
	 * @param array
	 * @param value
	 */
	public static int getIndex(int[] array, int valaue) {
		if (array == null || array.length == 0) return 0;

		for (int i = 0; i < array.length; i++) {
			if (valaue == array[i]) return i;
		}
		return 0;
	}

	/**
	 * 判断一个数组中是否有某个数值
	 * 
	 * @param list
	 * @param value
	 * @return
	 */
	public static boolean contains(ArrayList<Integer> list, int value) {
		if (list == null) return false;

		for (int i = 0; i < list.size(); i++) {
			if (value == list.get(i)) return true;
		}
		return false;
	}

	/**
	 * 格式化文件大小，只保留一位小数
	 * 
	 * @param input
	 * @return
	 */
	public static String getFormatSize(String input) {
		if (input.contains(".")) {
			return input.substring(0, input.lastIndexOf('.') + 2);
		}
		else {
			return input;
		}
	}
	
	/**
	 * 获取当前应用的名称
	 * 
	 * @param instance
	 * @return
	 */
	public static String getAppName(Activity instance) {
		final PackageManager pm = instance.getApplicationContext()
				.getPackageManager();
		ApplicationInfo ai;
		try {
			ai = pm.getApplicationInfo(instance.getPackageName(), 0);
		}
		catch (final NameNotFoundException e) {
			ai = null;
		}
		return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
	}

	/**
	 * 打开设置界面
	 * 
	 * @param instance
	 */
	public static void openSetting(Activity instance) {
		Intent intent = null;

		// 判断手机系统的版本 即API大于10 就是3.0或以上版本
		if (android.os.Build.VERSION.SDK_INT > 10) {
			intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
		}
		else {
			intent = new Intent();
			ComponentName component = new ComponentName("com.android.settings",
					"com.android.settings.Settings");
			intent.setComponent(component);
			intent.setAction("android.intent.action.VIEW");
		}
		instance.startActivity(intent);
	}

	/**
	 * 获取当前App的图标
	 * 
	 * @param
	 * @return
	 */
	public static Drawable getAppIcon(Activity instance) {
		PackageManager pManager = instance.getPackageManager();
		return pManager.getApplicationIcon(instance.getApplicationInfo());
	}

	/**
	 * 获取当前App的名字
	 * 
	 * @param
	 * @return
	 */
	public static String getPackageName(Activity instance) {
		PackageManager pManager = instance.getPackageManager();
		return pManager.getApplicationLabel(instance.getApplicationInfo())
				.toString();
	}

	/**
	 * 获取资源ID
	 * 
	 * @param context
	 * @param name
	 * @param defType
	 * @return
	 */
	private static int getId(Context context, String name, String defType) {
		return context.getResources().getIdentifier(name, defType,
				context.getPackageName());
	}

	/**
	 * 获取图片id
	 * 
	 * @param context
	 * @param name
	 * @return
	 */
	public static int getDrawable(Context context, String name) {
		return getId(context, name, "drawable");
	}

	/**
	 * 获取View中控件Id
	 * 
	 * @param context
	 * @param name
	 * @return
	 */
	public static int getId(Context context, String name) {
		return getId(context, name, "id");
	}

	/**
	 * 获取layout
	 * 
	 * @param context
	 * @param name
	 * @return
	 */
	public static int getLayout(Context context, String name) {
		return getId(context, name, "layout");
	}

	/**
	 * 获取id
	 * 
	 * @param context
	 * @param name
	 * @return
	 */
	public static int getStringID(Context context, String name) {
		return getStringID(context, name, "string");
	}

	/**
	 * 获取资源ID
	 * 
	 * @param context
	 * @param name
	 * @param defType
	 * @return
	 */
	private static int getStringID(Context context, String name, String defType) {
		return context.getResources().getIdentifier(name, defType,
				context.getPackageName());
	}

	/**
	 * 获取字符串
	 * 
	 * @param context
	 * @param name
	 * @return
	 */
	public static String getString(Context context, String name) {
		return context.getString(getId(context, name, "string"));
	}

	/**
	 * 获取字符串
	 * 
	 * @param context
	 * @param name
	 * @return
	 */
	public static String getString(Context context, String name,
			String formatArgs) {
		return context.getString(getId(context, name, "string"), formatArgs);
	}

	/**
	 * 获取样式
	 * @param context
	 * @param name
	 * @return
	 */
	public static int getStyle(Context context, String name) {
		return getId(context, name, "style");
	}

	/**
	 * 获取dimen
	 * @param context
	 * @param name
	 * @return
	 */
	public static int getDimen(Context context, String name) {
		return getId(context, name, "dimen");
	}

	/**
	 * 获取color
	 * @param context
	 * @param name
	 * @return
	 */
	public static int getColor(Context context, String name) {
		return context.getResources().getColor(getId(context, name, "color"));
	}

	/**
	 * 获取attr
	 * @param context
	 * @param name
	 */
	public static int getAttr(Context context, String name) {
		return getId(context, name, "attr");
	}
}