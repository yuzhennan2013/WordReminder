package cn.sina.youxi.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

public class WyxUtil {

	public static final String LOG_TAG = "Weiyouxi-Util";
	public static final String BOUNDARY = "7cd4a6d158c";
	public static final String MP_BOUNDARY = "--" + BOUNDARY;
	public static final String END_MP_BOUNDARY = "--" + BOUNDARY + "--";

	private static final String WYX_PREFIX = "WYX_";

	public static String encodePostBody(Bundle parameters, String boundary) {
		if (parameters == null) return "";
		StringBuilder sb = new StringBuilder();
		for (String key : parameters.keySet()) {
			if (key.equals("pic")) {
				continue;
			}
			sb.append(MP_BOUNDARY).append("\r\n");
			sb.append("Content-Disposition: form-data; name=\"" + key
					+ "\"\r\n\r\n" + parameters.getString(key));
			sb.append("\r\n");

		}
		CV_Log.i(LOG_TAG + "encodePostBody", sb.toString());
		return sb.toString();
	}

	/**
	 * 将Key-value转换成用&号链接的URL查询参数形式。
	 * 
	 * @param parameters
	 * @return
	 */
	public static String encodeUrl(Bundle parameters) {
		if (parameters == null) { return ""; }
		List<String> list = new ArrayList<String>();
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

	public static String encodeUrl2(Bundle parameters, String appScrect) {
		if (parameters == null) { return ""; }

		List<String> list = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String key : parameters.keySet()) {
			list.add(key);
		}
		Collections.sort(list);
		for (String key : list) {
			if (first) first = false;
			else sb.append("|");
			if (parameters.getString(key) != null) {
				sb.append(key + "|" + parameters.getString(key));
			}
		}
		sb.append("|");
		sb.append(appScrect);

		return sb.toString();
	}

	static class ValuePairComparator implements Comparator<BasicNameValuePair> {

		@Override
		public int compare(BasicNameValuePair lhs, BasicNameValuePair rhs) {
			return lhs.getName().compareTo(rhs.getName());
		}
	}

	/**
	 * 将Key-value转换成用&号链接的URL查询参数形式。
	 * 
	 * @param parameters
	 * @return
	 */
	public static String encodeUrl(ArrayList<BasicNameValuePair> parameters) {
		if (parameters == null) { return ""; }
		List<String> list = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		Collections.sort(parameters, new ValuePairComparator());
		for (int i = 0; i < parameters.size(); i++) {
			if (first) first = false;
			else sb.append("&");
			String value = parameters.get(i).getValue();
			String key = parameters.get(i).getName();
			if (value != null) {
				sb.append(URLEncoder.encode(key) + "="
						+ URLEncoder.encode(value));
			}
		}
		return sb.toString();
	}

	/**
	 * 将用&号链接的URL参数转换成key-value形式。
	 * 
	 * @param s
	 * @return
	 */
	public static Bundle decodeUrl(String s) {
		Bundle params = new Bundle();
		if (s != null) {
			String array[] = s.split("&");
			for (String parameter : array) {
				String v[] = parameter.split("=");
				params.putString(URLDecoder.decode(v[0]),
						URLDecoder.decode(v[1]));
			}
		}
		return params;
	}

	/**
	 * 发送HTTP请求,得到数据
	 * 
	 * @param url
	 * @param method
	 * @param params
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 */
	public static String openUrl(String url, String method, Bundle params)
			throws MalformedURLException, ConnectTimeoutException,
			ProtocolException, IOException {
		if (method.equals("GET")) {

			if (url.contains("?")) {
				url = url + "&" + encodeUrl(params);
			}
			else {
				url = url + "?" + encodeUrl(params);
			}
		}

		String response = "";

		HttpURLConnection conn = (HttpURLConnection) new URL(url)
				.openConnection();
		conn.setConnectTimeout(6 * 1000);
		conn.setRequestProperty("User-Agent", System.getProperties()
				.getProperty("http.agent") + " WeiyouxiAndroidSDK");
		if (!method.equals("GET")) {
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.getOutputStream().write(encodeUrl(params).getBytes("UTF-8"));
		}
		response = read(conn.getInputStream());
		return response;
	}

	public static String upLoadPhoto(String url, String method, Bundle params) {
		OutputStream os;
		if (method.equals("GET")) {
			url = url + "?" + encodeUrl(params);
		}
		HttpURLConnection conn;
		String response = "";
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestProperty("User-Agent", System.getProperties()
					.getProperty("http.agent") + "WeiyouxiAndroidSDK");
			if (!method.equals("GET")) {
				Bundle dataparams = new Bundle();
				for (String key : params.keySet()) {
					if (key.equals("pic")) {
						dataparams.putByteArray(key, params.getByteArray(key));
					}
				}
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Charset", "UTF-8");
				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + BOUNDARY);
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setUseCaches(false);
				conn.setRequestProperty("Connection", "keep-alive");
				conn.connect();
				os = new BufferedOutputStream(conn.getOutputStream());
				os.write((encodePostBody(params, BOUNDARY)).getBytes());
				if (!dataparams.isEmpty()) {
					for (String key : dataparams.keySet()) {
						StringBuilder temp = new StringBuilder();
						temp.append(MP_BOUNDARY).append("\r\n");
						temp.append(
								"Content-Disposition: form-data; name=\"pic\"; filename=\"")
								.append("news_image").append("\"\r\n");
						temp.append("Content-Type:image/png")
								.append("\r\n\r\n");

						byte[] res = temp.toString().getBytes();
						os.write(res);
						os.write(dataparams.getByteArray(key));
						os.write("\r\n".getBytes());
						os.write(("\r\n" + END_MP_BOUNDARY).getBytes());
					}
				}
				os.flush();
			}
			response = read(conn.getInputStream());
			CV_Log.i(LOG_TAG, "response=" + response);
			return response;
		}
		catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	private static String read(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line);
		}
		in.close();
		return sb.toString();
	}

	/**
	 * 正则表达式判断字符串是否纯数字
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) { return false; }
		return true;
	}

	/**
	 * 判断字符串中是否包含@
	 * @param str
	 * @return
	 */
	public static boolean isEmail(String str) {
		return str.contains("@");
	}

	/**
	 * 登录，合法性检测
	 * @param context
	 * @param userNameEdit
	 * @param pwEditText
	 * @return
	 */
	public static boolean checkLogin(Context context, EditText userNameEdit,
			EditText pwEditText) {

		String userName = userNameEdit.getText().toString();
		String pwd = pwEditText.getText().toString();

		boolean result = true;
		if (TextUtils.isEmpty(userName)) {
			userNameEdit.requestFocus();
			Toast.makeText(context, "登录帐号不能为空", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (TextUtils.isEmpty(pwd)) {
			pwEditText.requestFocus();
			Toast.makeText(context, "登录密码不能为空", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (pwd.length() < 6 || pwd.length() > 16) {
			pwEditText.requestFocus();
			Toast.makeText(context, "请输入6-16位密码", Toast.LENGTH_SHORT).show();
			result = false;
		}

		return result;
	}

	/**
	 * 邮箱注册，数据合法性检测
	 * @param context
	 * @param userNameEdit
	 * @param pwEditText
	 */
	public static boolean checkEmailRegister(Context context,
			EditText userNameEdit, EditText pwEditText) {

		String userName = userNameEdit.getText().toString();
		String pwd = pwEditText.getText().toString();

		boolean result = true;
		if (TextUtils.isEmpty(userName)) {
			userNameEdit.requestFocus();
			Toast.makeText(context, "用户名不能为空", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (userName.length() < 4 || userName.length() > 16) {
			userNameEdit.requestFocus();
			Toast.makeText(context, "请输入4-16位用户名", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (TextUtils.isEmpty(pwd)) {
			pwEditText.requestFocus();
			Toast.makeText(context, "密码不能为空", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (userName.startsWith("_")) {
			userNameEdit.requestFocus();
			Toast.makeText(context, "用户名不能以下划线开头", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (userName.endsWith("_")) {
			userNameEdit.requestFocus();
			Toast.makeText(context, "用户名不能以下划线结尾", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (userName.equals(pwd)) {
			pwEditText.requestFocus();
			Toast.makeText(context, "用户名和密码不能相同", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (pwd.length() < 6 || pwd.length() > 16) {
			pwEditText.requestFocus();
			Toast.makeText(context, "请输入6-16位密码", Toast.LENGTH_SHORT).show();
			result = false;
		}
		return result;
	}

	/**
	 * 手机注册，数据合法性检测
	 * @param context
	 * @param userNameEdit
	 * @param pwEditText
	 */
	public static boolean checkPhoneRegister(Context context,
			EditText userNameEdit, EditText pwEditText) {

		boolean result = true;

		String phoneNumber = userNameEdit.getText().toString();
		String phonePwd = pwEditText.getText().toString();

		if (StringUtils.isBlank(phoneNumber)) {
			userNameEdit.requestFocus();
			Toast.makeText(context, "手机号不能为空", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (!WyxUtil.isNumeric(phoneNumber)) {
			userNameEdit.requestFocus();
			Toast.makeText(context, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (StringUtils.isBlank(phonePwd)) {
			Toast.makeText(context, "密码不能为空", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (phoneNumber.equals(phonePwd)) {
			pwEditText.requestFocus();
			Toast.makeText(context, "手机号和密码不能相同", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (phoneNumber.contains(phonePwd)) {
			pwEditText.requestFocus();
			Toast.makeText(context, "密码不能是手机号的某几位", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (phonePwd.endsWith("_")) {
			pwEditText.requestFocus();
			Toast.makeText(context, "密码不能以下划线结尾", Toast.LENGTH_SHORT).show();
			result = false;
		}
		else if (phonePwd.length() < 6 || phonePwd.length() > 16) {
			pwEditText.requestFocus();
			Toast.makeText(context, "请输入6-16位密码", Toast.LENGTH_SHORT).show();
			result = false;
		}

		return result;
	}
}