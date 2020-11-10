package org.xson.tangyuan.es.datasource;

import org.xson.tangyuan.client.http.HttpClientManager;
import org.xson.tangyuan.es.util.EsHttpClient;

public class EsSourceVo {

	private String       id;
	private String       host;
	private EsHttpClient client;

	/**
	 * 共享客户端ID
	 */
	private String       usi;

	public EsSourceVo(String id, String host, String usi) {
		this.id = id;
		this.host = host;
		this.usi = usi;
	}

	public String getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public String getUsi() {
		return usi;
	}

	public EsHttpClient getClient() {
		return client;
	}

	public void start() throws Throwable {
		HttpClientManager.getInstance().checkKey(this.usi);
		this.client = new EsHttpClient(this);
		this.client.init();
	}

	public void stop() {
		this.client.shutdown();
	}
}
