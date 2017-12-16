package org.xson.tangyuan.es.datasource;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;

public class EsHttpClient {

	private static Log	log			= LogFactory.getLog(EsHttpClient.class);

	private String		contentType	= "application/json; charset=UTF-8";

	public String get(String url) throws Throwable {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			URI uri = new URI(url);
			HttpGet httpGet = new HttpGet(uri);
			httpGet.addHeader(HTTP.CONTENT_TYPE, contentType);
			CloseableHttpResponse response = httpclient.execute(httpGet);
			try {
				return getResponseString(response);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

	public String delete(String url) throws Throwable {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			URI uri = new URI(url);
			HttpDelete httpGet = new HttpDelete(uri);
			httpGet.addHeader(HTTP.CONTENT_TYPE, contentType);
			CloseableHttpResponse response = httpclient.execute(httpGet);
			try {
				return getResponseString(response);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

	public String post(String url, String body) throws Throwable {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			URI uri = new URI(url);
			HttpPost httpost = new HttpPost(uri);
			StringEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
			httpost.setEntity(entity);
			CloseableHttpResponse response = httpclient.execute(httpost);
			try {
				return getResponseString(response);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

	public String put(String url, String body) throws Throwable {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			URI uri = new URI(url);
			HttpPut httpost = new HttpPut(uri);
			StringEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
			httpost.setEntity(entity);
			CloseableHttpResponse response = httpclient.execute(httpost);
			try {
				return getResponseString(response);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

	private String getResponseString(CloseableHttpResponse response) throws Throwable {
		int status = response.getStatusLine().getStatusCode();
		if (status != 200) {
			printResponseError(response);
			throw new Exception("Unexpected response status: " + status);
		}
		HttpEntity entity = response.getEntity();
		return EntityUtils.toString(entity, "UTF-8");
	}

	private void printResponseError(CloseableHttpResponse response) {
		try {
			HttpEntity entity = response.getEntity();
			String errorInfo = EntityUtils.toString(entity, "UTF-8");
			log.error(errorInfo);
		} catch (Throwable e) {
		}
	}

}
