package cn.sina.youxi.util;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;

/**
 * 类说明：   渠道号读取工具类
 * 就是文档中的fcode值
 * 
 * @author  Cundong
 * @date 	2012-3-16
 * @version 1.0
 */
public class ChannelUtils {

	private static final String TAG = "ChannelUtils";
	// 默认渠道号
	private static final String DEFAULT_CHANNEL = "100010041000";

	// 配置信息路径
	private static final String ASSET_PATH = "sina/config.properties";

	/**
	 * 获取渠道号fcode
	 * @param context
	 * @return
	 */
	public static String getChannelID(Context context) {
		InputStream in = null;

		AssetManager asset = context.getAssets();

		try {
			in = asset.open(ASSET_PATH);
		}
		catch (IOException e) {
			CV_Log.i(TAG, "use default channelID:" + DEFAULT_CHANNEL);
		}

		byte[] buffer = FileUtils.toBytes(in);
		return buffer != null && buffer.length > 0 ? new String(buffer).trim()
				: DEFAULT_CHANNEL;
	}
}
