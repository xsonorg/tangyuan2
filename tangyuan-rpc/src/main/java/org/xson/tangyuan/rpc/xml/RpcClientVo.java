package org.xson.tangyuan.rpc.xml;

import org.xson.tangyuan.rpc.client.AbstractRpcClient;
import org.xson.tangyuan.rpc.client.HttpRpcClient;

public class RpcClientVo {

	public enum ClientUseType {
		PIGEON, HTTP_CLIENT
	}

	private String			id;
	private ClientUseType	use;
	private String			schema;
	private String			resource;

	public RpcClientVo(String id, ClientUseType use, String schema, String resource) {
		this.id = id;
		this.use = use;
		this.schema = schema;
		this.resource = resource;
	}

	public String getId() {
		return id;
	}

	public String getSchema() {
		return schema;
	}

	public String getResource() {
		return resource;
	}

	public AbstractRpcClient create() throws Throwable {

		AbstractRpcClient client = null;

		if (ClientUseType.HTTP_CLIENT == use) {
			client = new HttpRpcClient(this);
			client.init();
		}
		// if (ClientUseType.PIGEON == use) {
		// return new PigeonRpcClient(this);
		// }
		return client;
	}
}
