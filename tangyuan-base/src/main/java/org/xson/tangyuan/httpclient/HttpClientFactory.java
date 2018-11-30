package org.xson.tangyuan.httpclient;

import java.util.Properties;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientFactory {

	private HttpClientVo		vo					= null;
	private RequestConfig		customRequestConfig	= null;
	private XPoolingHttpClient	singleton			= null;

	public HttpClientFactory(HttpClientVo vo) {
		this.vo = vo;
	}

	private XHttpClient getPoolingHttpClient0() {
		Properties properties = vo.getProperties();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

		int maxTotal = 200;// TODO
		if (properties.containsKey("maxTotal".toUpperCase())) {
			maxTotal = Integer.parseInt(properties.getProperty("maxTotal".toUpperCase()));
		}
		cm.setMaxTotal(maxTotal);// Increase max total connection to 200

		int defaultMaxPerRoute = maxTotal;
		cm.setDefaultMaxPerRoute(defaultMaxPerRoute);

		if (properties.containsKey("validateAfterInactivity".toUpperCase())) {
			int validateAfterInactivity = Integer.parseInt(properties.getProperty("validateAfterInactivity".toUpperCase()));
			cm.setValidateAfterInactivity(validateAfterInactivity);
		}

		// HttpHost httpHost = new HttpHost(hostname, port);
		// 将目标主机的最大连接数增加
		// cm.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);
		// cm.setDefaultSocketConfig(defaultSocketConfig);
		// cm.setDefaultConnectionConfig(defaultConnectionConfig); //设置默认的连接配置,不需要
		// cm.setConnectionConfig(host, connectionConfig);//设置某个Host下的连接配置,不需要

		HttpClientBuilder builder = HttpClients.custom().setConnectionManager(cm);

		// parse RequestConfig

		// RequestConfig defaultRequestConfig = null;
		RequestConfig.Builder rcBuilder = RequestConfig.custom();
		int rcCount = 0;

		// 连接超时时间
		if (properties.containsKey("connectTimeout".toUpperCase())) {
			int connectTimeout = Integer.parseInt(properties.getProperty("connectTimeout".toUpperCase()));
			rcBuilder.setConnectTimeout(connectTimeout);
			rcCount++;
		}

		// 读超时时间（等待数据超时时间）
		if (properties.containsKey("socketTimeout".toUpperCase())) {
			int socketTimeout = Integer.parseInt(properties.getProperty("socketTimeout".toUpperCase()));
			rcBuilder.setSocketTimeout(socketTimeout);
			rcCount++;
		}

		// 从池中获取连接超时时间
		if (properties.containsKey("connectionRequestTimeout".toUpperCase())) {
			int connectionRequestTimeout = Integer.parseInt(properties.getProperty("connectionRequestTimeout".toUpperCase()));
			rcBuilder.setConnectionRequestTimeout(connectionRequestTimeout);
			rcCount++;
		}

		if (rcCount > 0) {
			customRequestConfig = rcBuilder.build();
			builder.setDefaultRequestConfig(customRequestConfig);
		}

		// SocketConfig.custom().build()
		// builder.setDefaultSocketConfig(config);

		boolean requestSentRetryEnabled = false;
		int retryCount = 3;
		if (properties.containsKey("requestSentRetryEnabled".toUpperCase())) {
			requestSentRetryEnabled = Boolean.parseBoolean(properties.getProperty("requestSentRetryEnabled".toUpperCase()));
			if (requestSentRetryEnabled) {
				if (properties.containsKey("retryCount".toUpperCase())) {
					retryCount = Integer.parseInt(properties.getProperty("retryCount".toUpperCase()));
				}
				builder.setRetryHandler(new StandardHttpRequestRetryHandler(retryCount, requestSentRetryEnabled));
			}
		}

		XPoolingHttpClient poolingHttpClient = new XPoolingHttpClient(builder, cm);

		return poolingHttpClient;
	}

	/**
	 * 获取一个池化的HttpClient
	 */
	public XHttpClient getPoolingHttpClient() {
		if (null != singleton) {
			return singleton;
		}
		synchronized (this) {
			if (null == singleton) {
				singleton = (XPoolingHttpClient) getPoolingHttpClient0();
			}
			return singleton;
		}
	}

	public HttpClient getHttpClient() {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		return httpclient;
	}

	public RequestConfig getCustomRequestConfig() {
		return customRequestConfig;
	}

	public void shutdown() {
		if (null != singleton) {
			singleton.shutdown();
			singleton = null;
		}
	}

}
