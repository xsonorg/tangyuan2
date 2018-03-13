package org.xson.tangyuan.es.datasource;

public class EsSourceVo {

	private String			id;
	private String			host;

	private EsHttpClient	client;

	public EsSourceVo(String id, String host) {
		this.id = id;
		this.host = host;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public EsHttpClient getClient() {
		return client;
	}

	public void start() throws Throwable {
		this.client = new EsHttpClient(this);
		this.client.init();
	}

	public void stop() {
		this.client.shutdown();
	}
}
