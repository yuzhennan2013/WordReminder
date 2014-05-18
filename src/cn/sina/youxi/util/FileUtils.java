package cn.sina.youxi.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/** 
 * 类说明：  文件读写 工具类
 * @author  Cundong
 * @date    Feb 9, 2012 6:50:25 AM 
 * @version 1.0
 */
public class FileUtils {
	/**
	 * 写文件
	 * @param buffer   
	 * @param absoluteFolderPath
	 * @param fileName
	 * @return
	 */
	public static boolean writeFile(byte[] buffer, String folderPath,
			String fileName) {
		boolean writeSucc = false;
		if (buffer == null || buffer.length == 0) { return writeSucc; }

		File fileDir = new File(folderPath);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}

		File file = new File(folderPath, fileName);
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(file);
			out.write(buffer);
			writeSucc = true;
		}
		catch (Exception e) {
			Log.e("FileUtils", "writeFile error:" + e.getStackTrace());
		}
		finally {
			try {
				out.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		return writeSucc;
	}

	/**
	 * 追加方式写文本文件
	 * @param content
	 * @param folderPath
	 * @param fileName
	 */
	public static void appendFile(String content, String folderPath,
			String fileName) {

		File folder = new File(folderPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		FileWriter writer;
		try {
			writer = new FileWriter(folderPath + fileName, true);
			writer.write(content);
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.e("FileUtils", "appendFile error:" + e.getStackTrace());
		}
	}

	/**
	 * 读缓存文件（存在SD卡则读SD卡，否则读手机内存）
	 * @param mContext
	 * @param absoluteFolderPath
	 * @param fileName
	 * @return
	 */
	public static String readString(Context mContext,
			String absoluteFolderPath, String fileName) {
		StringBuffer sb = new StringBuffer();

		File file = new File(absoluteFolderPath, fileName);
		if (file != null && file.exists() && file.length() > 0) {

			FileReader reader = null;
			try {
				// TODO SD卡可以这样读 手机内存需要换个方式
				reader = new FileReader(file.getAbsolutePath());

				BufferedReader br = new BufferedReader(reader);
				String str = br.readLine();
				while (str != null) {
					sb.append(str);
					str = br.readLine();
				}
				br.close();
				reader.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 根据文件夹名称 拼接文件夹的绝对路径
	 * @param mContext
	 * @param folderName
	 * @return
	 */
	public static String getFolderPath(Context mContext, String folderName) {
		StringBuffer folderPathBuffer = new StringBuffer();

		if (SDCardUtils.isMounted()) {
			folderPathBuffer.append(Environment.getExternalStorageDirectory())
					.append(File.separator).append(folderName)
					.append(File.separator);
		}
		else {
			folderPathBuffer.append(mContext.getFilesDir())
					.append(File.separator).append(folderName)
					.append(File.separator);
		}
		return folderPathBuffer.toString();
	}

	/**
	 * 根据文件绝对路径获取文件名
	 * @param filePath
	 * @return
	 */
	public static String getFileName(String filePath) {
		if (StringUtils.isBlank(filePath)) return "";
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
	}

	/**
	 * 获取文件扩展名
	 * @param fileName
	 * @return
	 */
	public static String getFileFormat(String fileName) {
		if (StringUtils.isBlank(fileName)) return "";

		int point = fileName.lastIndexOf('.');
		return fileName.substring(point + 1);
	}

	/**
	 * InputStream转为byte数组
	 * @param in
	 * @return
	 */
	public static byte[] toBytes(InputStream in) {
		if (in == null) return null;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int ch = -1;
		byte buffer[] = null;

		try {
			while ((ch = in.read()) != -1) {
				out.write(ch);
			}

			buffer = out.toByteArray();
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return buffer;
	}

	/**
	 * 文件删除 如果是目录则删除目录下所有文件
	 * @param filepath
	 */
	public static void deleteFiles(String filepath) {
		File file = new File(filepath);
		deleteFiles(file);
	}

	/**
	 * 文件删除 如果是目录则删除目录下所有文件
	 * @param file
	 */
	public static void deleteFiles(File file) {
		// 文件
		if (file.exists() && !file.isDirectory()) {
			file.delete();
		}
		// 目录
		else {
			File[] delFiles = file.listFiles();
			int fileCounter = delFiles != null ? delFiles.length : 0;
			for (int i = 0; i < fileCounter; i++) {
				if (delFiles[i].isDirectory()) {
					File member = new File(delFiles[i].getAbsolutePath());
					deleteFiles(member);
				}

				delFiles[i].delete();
			}
		}
	}

	/**
	 * 获取文件大小
	 * @param size 字节
	 * @return
	 */
	public static String getFileSize(long size) {
		if (size <= 0) return "0";
		java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
		float temp = (float) size / 1024;
		if (temp >= 1024) {
			return df.format(temp / 1024) + "M";
		}
		else {
			return df.format(temp) + "K";
		}
	}
}