package org.xson.tangyuan.tools.httpclient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.HttpClient;

public class HttpClientManager {

	private static Map<String, HttpClientFactory>	factoryMap	= null;

	private static HttpClientFactory				factory		= null;

	public static void init(List<HttpClientVo> voList) {
		if (1 == voList.size()) {
			factory = new HttpClientFactory(voList.get(0));
			return;
		}
		factoryMap = new HashMap<String, HttpClientFactory>();
		for (HttpClientVo vo : voList) {
			factoryMap.put(vo.getKey(), new HttpClientFactory(vo));
		}
	}

	public static XHttpClient getXHttpClient() {
		return getXHttpClient(null);
	}

	public static XHttpClient getXHttpClient(String clientId) {
		if (null != factory) {
			return factory.getPoolingHttpClient();
		}

		if (null != factoryMap) {
			HttpClientFactory fcFactory = factoryMap.get(clientId);
			if (null != fcFactory) {
				return fcFactory.getPoolingHttpClient();
			}
		}

		return null;
	}

	public static HttpClient getHttpClient() {
		return getHttpClient(null);
	}

	public static HttpClient getHttpClient(String clientId) {
		if (null != factory) {
			return factory.getHttpClient();
		}
		if (null != factoryMap) {
			HttpClientFactory fcFactory = factoryMap.get(clientId);
			if (null != fcFactory) {
				return fcFactory.getHttpClient();
			}
		}

		return null;
	}

	public static void shutdown() {
		if (null != factory) {
			factory.shutdown();
		}

		if (null != factoryMap) {
			for (Entry<String, HttpClientFactory> entry : factoryMap.entrySet()) {
				entry.getValue().shutdown();
			}
		}
	}
}
