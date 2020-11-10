package org.xson.tangyuan.client.http;

import java.util.Properties;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.TangYuanUtil;

public class HttpClientFactory {

	protected Log			log			= LogFactory.getLog(getClass());

	private HttpClientVo	vo			= null;

	private XHttpClient		singleton	= null;

	public HttpClientFactory(HttpClientVo vo) {
		this.vo = vo;
	}

	private SSLConnectionSocketFactory parseSSLConnectionSocketFactory(String className) {
		CustomSSLSocketFactory customSSLSocketFactory = null;
		try {
			if (null == className) {
				return null;
			}
			Class<?> clazz = ClassUtils.forName(className);
			customSSLSocketFactory = (CustomSSLSocketFactory) TangYuanUtil.newInstance(clazz);
			return customSSLSocketFactory.create();
		} catch (Throwable e) {
			log.error(e);
		}
		return null;
	}

	private PoolingHttpClientConnectionManager parsePooling(HttpClientBuilder builder, Properties properties) {
		// ssl
		SSLConnectionSocketFactory sslsf = parseSSLConnectionSocketFactory(properties.getProperty("CustomSSLSocketFactory".toUpperCase()));

		PoolingHttpClientConnectionManager cm = null;
		if (null != sslsf) {
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf)
					.register("http", new PlainConnectionSocketFactory()).build();
			cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			builder.setSSLSocketFactory(sslsf);
		} else {
			cm = new PoolingHttpClientConnectionManager();
		}

		int maxTotal = 10;
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

		return cm;
	}

	private RequestConfig parseRequestConfig(Properties properties) {
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
			return rcBuilder.build();
		}

		return null;
	}

	private XHttpClient getXHttpClient0() {

		Properties properties = vo.getProperties();

		RequestConfig customRequestConfig = null;
		HttpClientBuilder builder = null;
		builder = HttpClients.custom();

		// Pooling
		PoolingHttpClientConnectionManager cm = parsePooling(builder, properties);
		if (null != cm) {
			builder.setConnectionManager(cm);
		}

		customRequestConfig = parseRequestConfig(properties);
		if (null != customRequestConfig) {
			builder.setDefaultRequestConfig(customRequestConfig);
		}

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

		AbstractHttpClient abstractHttpClient = null;
		abstractHttpClient = new XPoolingHttpClient(builder, cm);

		return abstractHttpClient;
	}

	public XHttpClient getXHttpClient() {
		if (null != singleton) {
			return singleton;
		}
		synchronized (this) {
			if (null == singleton) {
				singleton = getXHttpClient0();
			}
			return singleton;
		}
	}

	public void shutdown() {
		if (null != singleton) {
			singleton.shutdown();
			singleton = null;
		}
	}

}
