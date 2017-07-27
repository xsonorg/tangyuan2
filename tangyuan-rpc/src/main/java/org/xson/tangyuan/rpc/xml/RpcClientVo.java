package org.xson.tangyuan.rpc.xml;

import org.xson.tangyuan.rpc.client.AbstractClientRpc;
import org.xson.tangyuan.rpc.client.HttpRpcClient;

public class RpcClientVo {

	public enum ClientUseType {
		PIGEON, HTTP_CLIENT
	}

	private String			id;
	private ClientUseType	use;
	private String			schema;

	public RpcClientVo(String id, ClientUseType use, String schema) {
		this.id = id;
		this.use = use;
		this.schema = schema;
	}

	public String getId() {
		return id;
	}

	public String getSchema() {
		return schema;
	}

	public AbstractClientRpc create() {
		if (ClientUseType.HTTP_CLIENT == use) {
			return new HttpRpcClient(this);
		}
		// if (ClientUseType.PIGEON == use) {
		// return new PigeonRpcClient(this);
		// }
		return null;
	}
}
