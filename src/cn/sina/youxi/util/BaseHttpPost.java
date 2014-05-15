package cn.sina.youxi.util;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

public class BaseHttpPost {

	private static final String TAG = "BaseHttpPost";

	/**
	 * @param bytes
	 * @return
	 */
	public synchronized static String post(String url, byte[] bytes) {

		try {
			HttpPost httpPost = getHttpPost(url);

			httpPost.setEntity(new ByteArrayEntity(bytes));

			HttpResponse httpResponse;

			HttpClient httpClient = getHttpClient();
			httpResponse = httpClient.execute(httpPost);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				HttpEntity httpEntity = httpResponse.getEntity();

				return EntityUtils.toString(httpEntity);
			}
			else {
				CV_Log.i("post " + url + " error.statusCode=" + statusCode);
			}
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static HttpPost getHttpPost(String url) {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Content-Type", "text/xml");
		return httpPost;
	}

	private static HttpClient getHttpClient() {
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 60 * 1000);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				60 * 1000);
		return httpClient;
	}
}