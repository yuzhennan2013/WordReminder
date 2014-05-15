package cn.sina.youxi.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 类说明： Stream 工具类
 * 
 * @author  Cundong
 * @date 	2012-2-17
 * @version 1.0
 */
public class StreamUtils {

	/**
	 * InputStream转化为byte数组
	 * @param is
	 * @return
	 */
	public static byte[] toByte(InputStream is) throws IOException {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		int ch;
		while ((ch = is.read()) != -1) {
			bytestream.write(ch);
		}
		byte imgdata[] = bytestream.toByteArray();
		bytestream.close();
		return imgdata;
	}

	public static void copy(final InputStream bis, final OutputStream baos)
			throws IOException {
		final int buffer_size = 1024;
		byte[] buffer = new byte[buffer_size];

		int count = 0;
		while ((count = bis.read(buffer)) >= 0)
			baos.write(buffer, 0, count);
	}

	public static void close(InputStream in) {
		try {
			if (in != null) {
				in.close();
				in = null;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void close(OutputStream out) {
		try {
			if (out != null) {
				out.close();
				out = null;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void close(BufferedReader in) {
		try {
			if (in != null) {
				in.close();
				in = null;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}