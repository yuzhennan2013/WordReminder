package cn.sina.youxi.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** 
 * 类说明：          日期时间工具类 
 * @author  Cundong
 * @date    Feb 15, 2012 5:30:51 AM 
 * @version 1.0
 */
public class DateUtils {

	/**
	 * 
	 * 默认日期格式
	 */
	private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	/**
	 * 获取当前时间
	 * @return
	 */
	public static String getCurrentTime() {

		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sf.format(date);
	}

	/**
	 * @param pubtime 样例：2011-06-20T17:23:11Z
	 * @return 样例：05.10 17:11
	 */
	public static String getFormatTime(String pubtime) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		Date date = null;
		try {
			date = df.parse(pubtime.replace("Z", ""));
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

		return (null == date) ? null : (new SimpleDateFormat("MM.dd HH:mm"))
				.format(date);
	}

	/**
	 * @param pubtime 样例：2011-06-20T17:23:11Z
	 * @param format
	 * @return
	 */
	public static String getFormatTime(String pubtime, String format) {
		SimpleDateFormat df = new SimpleDateFormat(format);

		Date date = null;
		try {
			date = df.parse(pubtime.replace("Z", ""));
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

		return (null == date) ? null : (new SimpleDateFormat(format))
				.format(date);
	}

	/** 字符串转换时间戳
	 * @param str
	 * @return
	*/
	public static Timestamp str2Timestamp(String str, String fromat) {
		Date date = str2Date(str, fromat);
		return new Timestamp(date.getTime());
	}

	/**
	 * 获取两个时间戳之间差值,并且返回小时
	 */
	public static int subTimeStamp2Hour(Timestamp one, Timestamp two) {
		int minute = subTimeStamp2Minute(one, two);
		return minute / 60;
	}

	/**
	 * 获取两个时间戳之间差值,并且返回分钟
	 */
	public static int subTimeStamp2Minute(Timestamp one, Timestamp two) {
		return (int) (two.getTime() - one.getTime()) / (1000 * 60);
	}

	/**
	 * 获取两个时间戳之间差值,并且返回秒
	 */
	public static int subTimeStamp2Second(Timestamp one, Timestamp two) {
		int minute = subTimeStamp2Minute(one, two);
		return minute * 60;
	}

	/**
	 * Date转Timestamp
	 * @param pubtime
	 * @return
	 */
	public static Timestamp string2Timestamp(String pubtime, String format) {
		Timestamp timestamp = null;

		SimpleDateFormat df = new SimpleDateFormat(format);

		Date date = null;
		try {
			date = df.parse(pubtime.replace("Z", ""));
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

		if (null != date) {
			SimpleDateFormat df1 = new SimpleDateFormat(format);
			String time = df1.format(date);
			timestamp = Timestamp.valueOf(time);
		}

		return timestamp;
	}

	/**
	 * 获取用于显示的时间
	 * @param pubtime
	 * @return
	 */
	public static String getTime(String pubtime, String format) {
		String displayTime = getFormatTime(pubtime, format);
		Timestamp current = new Timestamp(System.currentTimeMillis());
		Timestamp pubTimestamp = DateUtils.string2Timestamp(pubtime, format);

		if (null != pubTimestamp) {
			int second = subTimeStamp2Second(pubTimestamp, current);
			if (second < 60 && second > 0) {
				displayTime = second + "秒前";
			}
			else {
				int minute = subTimeStamp2Minute(pubTimestamp, current);
				if (minute < 60 && minute > 0) {
					displayTime = minute + "分钟前";
				}
				else {
					int hour = subTimeStamp2Hour(pubTimestamp, current);

					if (hour < 24 && hour > 0) {
						displayTime = hour + "小时前";
					}
					else if (hour < 48 && hour > 24) {
						displayTime = "1天前";
					}
					else if (hour < 72 && hour > 48) {
						displayTime = "2天前";
					}
					else {
						displayTime = "很久以前";
					}
				}
			}
		}

		return displayTime;
	}

	/**
	* 字符串转换成日期 如果转换格式为空，则利用默认格式进行转换操作
	* 
	* @param str
	*            字符串
	* @param format
	*            日期格式
	* @return 日期
	* @throws java.text.ParseException
	*/
	public static Date str2Date(String str, String format) {
		Date date = null;

		if (!StringUtils.isBlank(str)) {
			// 如果没有指定字符串转换的格式，则用默认格式进行转换
			if (null == format || "".equals(format)) {
				format = DEFAULT_FORMAT;
			}

			SimpleDateFormat sdf = new SimpleDateFormat(format);
			try {
				date = sdf.parse(str);
			}
			catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return date;
	}
}