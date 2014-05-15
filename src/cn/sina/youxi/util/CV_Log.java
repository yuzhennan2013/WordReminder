package cn.sina.youxi.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;

public class CV_Log {
	
	/**
	 * init the log context
	 * @param context, when context is provided
	 * TAG will be set to package name
	 */
	public static void init(Context context) {

		TAG = context.getPackageName();	
		TAG_DEBUG = TAG + ".debug";
		
		HashMap<String, String> config = get_CV_Log_config();
        SHOW_LOG = Boolean.valueOf(config.get(KEY_SHOW_LOG) == null? "true": config.get(KEY_SHOW_LOG));
        SHOW_LOG_DEBUG = Boolean.valueOf(config.get(KEY_SHOW_LOG_DEBUG) == null? "false": config.get(KEY_SHOW_LOG_DEBUG));
	}

	private static HashMap<String, String> get_CV_Log_config() {
		
		HashMap<String, String> hashMap = new HashMap<String, String>();
		
		File file = new File(Environment.getExternalStorageDirectory(), "cv_log.config");
		FileReader fileReader;
		BufferedReader bufferedReader;
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			
	        String s;
	        while ((s = bufferedReader.readLine())!=null) {
	            String [] key_value = s.split("=");
	            hashMap.put(key_value[0], key_value[1]);
	         }
	        bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hashMap;
	}
	
	/**
	 * you should modify this depending on your project's nick name
	 */
	private static String TAG = "";
	private static String TAG_DEBUG = ""; 
	
	private static boolean SHOW_LOG = true;
	private static boolean SHOW_LOG_DEBUG  = false;
	
	private static String KEY_SHOW_LOG = "SHOW_LOG";
	private static String KEY_SHOW_LOG_DEBUG  = "SHOW_LOG_DEBUG";
	
	public static void i_debug(String paramString2) {
		if (SHOW_LOG_DEBUG) {
			i(TAG_DEBUG, paramString2);	
		}
	}
	
	public static void i_debug(String paramString2, long ... times) {
		if (SHOW_LOG_DEBUG) {
			i(TAG_DEBUG, paramString2, times);	
		}
	}
	
	public static void i(String paramString1, String paramString2) {
		if (SHOW_LOG)
		{
			if (paramString1 == null || paramString1.length() == 0) {
				return;
			}
			
			if (paramString2 == null || paramString2.length() == 0) {
				return;
			}
			SimpleDateFormat sdf;
			sdf = new SimpleDateFormat("", Locale.ENGLISH);
			sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(sdf.format(System.currentTimeMillis()));
			stringBuilder.append(" -> ");
			stringBuilder.append(paramString2);
			android.util.Log.i(paramString1, stringBuilder.toString());
		}
	}
	
	public static void i(String paramString2, long ... times) {
		CV_Log.i(TAG, paramString2, times);
	}
	
	public static void i(String paramString1, String paramString2, long ... times) {
		SimpleDateFormat sdf;
		sdf = new SimpleDateFormat("", Locale.ENGLISH);
		sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
		
		for (int i = 0; i < times.length; i++) { 
			paramString2 = paramString2.replace("?", sdf.format(times[i]));
		}
		CV_Log.i(paramString1, paramString2);
	}

	public static void i(String paramString2) {
		i(TAG, paramString2);
	}
}
