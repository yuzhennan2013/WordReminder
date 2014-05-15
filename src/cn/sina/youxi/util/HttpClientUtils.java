package cn.sina.youxi.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 类说明： HttpClient工具类
 * 
 * @author Cundong
 * @date 2012-2-7
 * @version 1.0
 */
public class HttpClientUtils {
	private static final String CHARSET = HTTP.UTF_8;
	private static final String TAG = "CustomerHttpClient";

	private static HttpClient customerHttpClient = null;

	private HttpClientUtils() {
	}

	private static synchronized HttpClient getHttpClient(Context context) {
		if (null == customerHttpClient) {
			HttpParams params = new BasicHttpParams();

			// 设置一些基本参数
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, CHARSET);
			HttpProtocolParams.setUseExpectContinue(params, true);
			HttpProtocolParams
					.setUserAgent(
							params,
							"Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) "
									+ "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
			// 超时设置
			/* 从连接池中取连接的超时时间 */
			ConnManagerParams.setTimeout(params, 6 * 1000);

			/* 连接超时 */
			HttpConnectionParams.setConnectionTimeout(params, 6 * 1000);

			/* 请求超时 */
			HttpConnectionParams.setSoTimeout(params, 6 * 1000);

			// 设置我们的HttpClient支持HTTP和HTTPS两种模式
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			schReg.register(new Scheme("https", SSLSocketFactory
					.getSocketFactory(), 443));

			// 使用线程安全的连接管理来创建HttpClient
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
					params, schReg);

			customerHttpClient = new DefaultHttpClient(conMgr, params);

			ConnectivityManager manager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkinfo = manager.getActiveNetworkInfo();
			String net = networkinfo != null ? networkinfo.getExtraInfo()
					: null;

			// wifi的值为空
			if (net != null) {
				String proxyHost = getDefaultHost();

				if (!StringUtils.isBlank(proxyHost)) {
					HttpHost proxy = new HttpHost(proxyHost, getDefaultPort(),
							"http");
					customerHttpClient.getParams().setParameter(
							ConnRoutePNames.DEFAULT_PROXY, proxy);
				}
			}
		}
		return customerHttpClient;
	}

	public static HashMap<String, String> request(Context context,
			String channelID, String token, String url) {
		HashMap<String, String> resMap = new HashMap<String, String>();
		HttpGet request = new HttpGet(url);
		HttpClient httpClient = getHttpClient(context);

		// 解决：HttpClient WARNING: Cookie rejected: Illegal domain attribute
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);

		// channelID：versionCode + packageName
		request.addHeader("channel_id", channelID);
		request.addHeader("token", token);

		String response = "";

		try {
			response = httpClient.execute(request, new BasicResponseHandler());
		} catch (ClientProtocolException e) {
			resMap.put("errorCode", "-1");
			resMap.put("errorMsg", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			resMap.put("errorCode", "-1");
			resMap.put("errorMsg", e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// Bug http://code.google.com/p/android/issues/detail?id=5255
			// Do whatever you want retry / cancel / fail silently
			resMap.put("errorCode", "-1");
			resMap.put("errorMsg", e.getMessage());
			e.printStackTrace();
		}

		resMap.put("response", response);

		return resMap;
	}

	public static HashMap<String, String> request(Context context, String url) {
		HashMap<String, String> resMap = new HashMap<String, String>();
		HttpGet request = new HttpGet(url);
		HttpClient httpClient = getHttpClient(context);

		// channelID：versionCode + packageName

		String response = "";

		try {
			response = httpClient.execute(request, new BasicResponseHandler());
		} catch (ClientProtocolException e) {
			resMap.put("errorCode", "-1");
			resMap.put("errorMsg", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			resMap.put("errorCode", "-1");
			resMap.put("errorMsg", e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// Bug http://code.google.com/p/android/issues/detail?id=5255
			// Do whatever you want retry / cancel / fail silently
			resMap.put("errorCode", "-1");
			resMap.put("errorMsg", e.getMessage());
			e.printStackTrace();
		}

		resMap.put("response", response);

		return resMap;
	}

	public static HashMap<String, String> request(Context context,
			String channelID, String url) {
		HashMap<String, String> resMap = new HashMap<String, String>();
		HttpGet request = new HttpGet(url);
		HttpClient httpClient = getHttpClient(context);

		// channelID：versionCode + packageName
		request.addHeader("channel_id", channelID);

		String response = "";

		try {
			response = httpClient.execute(request, new BasicResponseHandler());
		} catch (ClientProtocolException e) {
			resMap.put("errorCode", "-1");
			resMap.put("errorMsg", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			resMap.put("errorCode", "-1");
			resMap.put("errorMsg", e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// Bug http://code.google.com/p/android/issues/detail?id=5255
			// Do whatever you want retry / cancel / fail silently
			resMap.put("errorCode", "-1");
			resMap.put("errorMsg", e.getMessage());
			e.printStackTrace();
		}

		resMap.put("response", response);

		return resMap;
	}

	public static String post(Context context,
			HashMap<String, String> headerMap, String url,
			ArrayList<NameValuePair> params) {
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params,
					CHARSET);

			// 创建POST请求
			HttpPost request = new HttpPost(url);

			Set<HashMap.Entry<String, String>> set = headerMap.entrySet();
			for (Iterator<HashMap.Entry<String, String>> i = set.iterator(); i
					.hasNext();) {
				HashMap.Entry<String, String> item = i.next();

				request.setHeader(item.getKey(), item.getValue());
			}

			request.setEntity(entity);

			// 发送请求
			HttpClient client = getHttpClient(context);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new RuntimeException("请求失败");
			}
			HttpEntity resEntity = response.getEntity();
			return (resEntity == null) ? null : EntityUtils.toString(resEntity,
					CHARSET);
		} catch (UnsupportedEncodingException e) {
			Log.w(TAG, "error:" + e.getMessage());
			return null;
		} catch (ClientProtocolException e) {
			Log.w(TAG, "error:" + e.getMessage());
			return null;
		} catch (IOException e) {
			throw new RuntimeException("连接失败", e);
		} catch (Exception e) {
			// Bug http://code.google.com/p/android/issues/detail?id=5255
			// Do whatever you want retry / cancel / fail silently
			throw new RuntimeException("连接失败", e);
		}
	}

	private static String getDefaultHost() {
		return android.net.Proxy.getDefaultHost();
	}

	private static int getDefaultPort() {
		return android.net.Proxy.getDefaultPort();
	}

	/**
	 * 获取Http响应头字段
	 * 
	 * @param http
	 * @return
	 */
	public static LinkedHashMap<String, String> getHttpResponseHeader(
			HttpURLConnection http) {
		LinkedHashMap<String, String> header = new LinkedHashMap<String, String>();
		for (int i = 0;; i++) {
			String mine = http.getHeaderField(i);
			if (mine == null)
				break;
			header.put(http.getHeaderFieldKey(i), mine);
		}
		return header;
	}

	/**
	 * 从Http响应头字段中获取ETag
	 * 
	 * @param http
	 * @return
	 */
	public static String getETag(HttpURLConnection http) {
		LinkedHashMap<String, String> headerMap = getHttpResponseHeader(http);
		for (LinkedHashMap.Entry<String, String> entry : headerMap.entrySet()) {
			String key = entry.getKey() != null ? entry.getKey() : "";
			if (key.equals("ETag")) {
				return entry.getValue();
			}
		}
		return "";
	}

	/**
	 * 判断是否有网络连接
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo mInfo = connectivity.getActiveNetworkInfo();
		if (mInfo != null) {
			return mInfo.isAvailable();
		}
		return false;
	}
}