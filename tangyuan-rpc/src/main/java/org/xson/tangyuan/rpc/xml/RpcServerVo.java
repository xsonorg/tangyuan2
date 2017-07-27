package org.xson.tangyuan.rpc.xml;

public class RpcServerVo {

	public enum ServerUseType {
		PIGEON, JETTY
	}

	private ServerUseType use;

	public RpcServerVo(ServerUseType use) {
		this.use = use;
	}

	public void start() {
		if (ServerUseType.PIGEON == use) {

		} else if (ServerUseType.JETTY == use) {

		}
	}
}
