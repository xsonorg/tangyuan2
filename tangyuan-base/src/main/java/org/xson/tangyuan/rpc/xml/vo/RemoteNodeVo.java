package org.xson.tangyuan.rpc.xml.vo;

public class RemoteNodeVo {

	// <remote-node id="service" domain="newspaper.gatherlife.service" client="" />

	private String id;
	private String domain;
	private String client;

	public RemoteNodeVo(String id, String domain, String client) {
		this.id = id;
		this.domain = domain;
		this.client = client;
	}

	public String getDomain() {
		return domain;
	}

	public String getClient() {
		return client;
	}

	public String getId() {
		return id;
	}
}
