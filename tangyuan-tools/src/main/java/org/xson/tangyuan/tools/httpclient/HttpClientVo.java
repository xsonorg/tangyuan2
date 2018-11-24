package org.xson.tangyuan.tools.httpclient;

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

	// public HttpClientVo() {
	// this(null);
	// }
	//
	// public HttpClientVo(Properties properties) {
	// this(properties, null);
	// }
	//
	// public HttpClientVo(Properties properties, String key) {
	// if (null == properties) {
	// this.properties = new Properties();
	// } else {
	// this.properties = properties;
	// }
	// initKey(key);
	// }
	//
	// private void initKey(String key) {
	// if (null == key) {
	// this.key = this.properties.toString();
	// } else {
	// this.key = key;
	// }
	// }
}
