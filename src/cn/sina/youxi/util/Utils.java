package cn.sina.youxi.util;

/**
 *  This file is part of GogoDroid.
 *  http://code.google.com/p/gogodroid
 *
 *  GogoDroid is open source software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  GogoDroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GogoDroid.  If not, see .
 *
 *  @author Mariotaku Lee (mariotaku)
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class Utils {

	/**
	 * common function to exeucte a cmd
	 * 
	 * @param cmd
	 */
	public static void executeCMD(String cmd) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmd);
			new MyThread(process.getErrorStream()).start();
			new MyThread(process.getInputStream()).start();
			process.waitFor();
			process.destroy();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
	}
	
	public static class MyThread extends Thread {
		BufferedReader bf;

		public MyThread(InputStream input) {
			try {
				bf = new BufferedReader(new InputStreamReader(input, "GBK"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			String line;
			try {
				line = bf.readLine();
				while (line != null) {
					System.out.println(line);
					line = bf.readLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
