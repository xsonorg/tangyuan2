package org.xson.tangyuan.rpc.balance;

public class BalanceHostVo {

	private String	domain;

	private int		port;

	private int		weight;

	public BalanceHostVo(String domain, int port, int weight) {
		this.domain = domain;
		this.port = port;
		this.weight = weight;
	}

	public String getDomain() {
		return domain;
	}

	public int getWeight() {
		return weight;
	}

	public int getPort() {
		return port;
	}

}
