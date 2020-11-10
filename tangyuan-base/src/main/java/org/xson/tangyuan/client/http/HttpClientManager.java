package org.xson.tangyuan.client.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.HttpClient;
import org.xson.tangyuan.TangYuanException;

public class HttpClientManager {

	private static HttpClientManager instance = null;

	public synchronized static HttpClientManager getInstance() {
		if (null != instance) {
			return instance;
		}
		instance = new HttpClientManager();
		return instance;
	}

	private Map<String, HttpClientFactory> factoryMap       = null;

	private HttpClientFactory              factory          = null;

	private String                         defaultFactoryId = null;

	private HttpClientManager() {
	}

	public void init(List<HttpClientVo> voList) {
		if (1 == voList.size()) {
			factory = new HttpClientFactory(voList.get(0));
			defaultFactoryId = voList.get(0).getKey();
			return;
		}
		if (voList.size() > 1) {
			factoryMap = new HashMap<String, HttpClientFactory>();
			for (HttpClientVo vo : voList) {
				factoryMap.put(vo.getKey(), new HttpClientFactory(vo));
			}
		}
	}

	public void checkKey(String key) {
		boolean r = false;
		if (null != factoryMap) {
			r = factoryMap.containsKey(key);
		}
		if (null != defaultFactoryId) {
			r = defaultFactoryId.equals(key);
		}
		if (!r) {
			throw new TangYuanException("HttpClient does not exist: " + key);
		}
	}

	public XHttpClient getXHttpClient() {
		return getXHttpClient(null);
	}

	public XHttpClient getXHttpClient(String clientId) {
		if (null != factory) {
			return factory.getXHttpClient();
		}

		if (null != factoryMap) {
			HttpClientFactory fcFactory = factoryMap.get(clientId);
			if (null != fcFactory) {
				return fcFactory.getXHttpClient();
			}
		}

		return null;
	}

	public HttpClient getHttpClient() {
		return getHttpClient(null);
	}

	public HttpClient getHttpClient(String clientId) {
		if (null != factory) {
			return factory.getXHttpClient().getHttpClient();
		}
		if (null != factoryMap) {
			HttpClientFactory fcFactory = factoryMap.get(clientId);
			if (null != fcFactory) {
				return factory.getXHttpClient().getHttpClient();
			}
		}

		return null;
	}

	public void shutdown() {
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
