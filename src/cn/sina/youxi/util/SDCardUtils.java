package cn.sina.youxi.util;

import android.os.Environment;
import android.os.StatFs;

/**
 * 类说明： SD卡工具类
 * 
 * @author 	Cundong
 * @date 	2012-2-7
 * @version 1.0
 */
public class SDCardUtils {
	/**
	 * SD卡是否挂载
	 * 
	 * @return
	 */
	public static boolean isMounted() {
		String status = Environment.getExternalStorageState();
		return status.equals(Environment.MEDIA_MOUNTED) ? true : false;
	}

	/**
	 * 判断是否有足够的空间供下载
	 * @param downloadSize
	 * @return
	 */
	public boolean isEnoughForDownload(long downloadSize) {
		StatFs statFs = new StatFs(Environment.getExternalStorageDirectory()
				.getAbsolutePath());

		// sd卡可用分区数
		int avCounts = statFs.getAvailableBlocks();

		// 一个分区数的大小
		long blockSize = statFs.getBlockSize();

		// sd卡可用空间
		long spaceLeft = avCounts * blockSize;

		if (spaceLeft < downloadSize) { return false; }

		return true;
	}
}