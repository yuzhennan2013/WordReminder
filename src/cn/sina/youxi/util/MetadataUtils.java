package cn.sina.youxi.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 类说明：  Metadata工具类
 * 
 * @author  Cundong
 * @date 	2012-6-6
 * @version 1.0
 */
public class MetadataUtils {
	/**
	 * 读取Application级的Metadata
	 * @return
	 */
	public static String getMetadata(Context context, String param) {
		ApplicationInfo appInfo = null;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
		}
		catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return appInfo.metaData != null ? appInfo.metaData.getString(param)
				: null;
	}
}
