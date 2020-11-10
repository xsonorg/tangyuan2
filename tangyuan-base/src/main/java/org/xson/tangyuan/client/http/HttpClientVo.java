package org.xson.tangyuan.client.http;

import java.util.Properties;

public class HttpClientVo {

	private String		key			= null;

	private Properties	properties	= null;

	public HttpClientVo(String key, Properties properties) {
		this.key = key;
		this.properties = properties;
	}

	public Properties getProperties() {
		return properties;
	}

	public String getKey() {
		return key;
	}
}
