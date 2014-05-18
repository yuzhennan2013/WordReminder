package cn.sina.youxi.util;

import android.content.Context;
import android.content.SharedPreferences;

public class OtherHelper {

	/**
	 * 是否允许用3G进行下载
	 */
	public static boolean canUse3GToDownload(Context mContext) {
		SharedPreferences sp = mContext.getSharedPreferences("settingSP",
				mContext.MODE_PRIVATE);
		return sp.getBoolean("downloadin3g", true);
	}
}
