package org.xson.tangyuan.rpc.xml.vo;

import org.xson.tangyuan.client.http.HttpClientManager;
import org.xson.tangyuan.client.http.XHttpClient;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.rpc.balance.BalanceManager;
import org.xson.tangyuan.rpc.client.AbstractRpcClient;
import org.xson.tangyuan.rpc.client.HttpRpcClient;
import org.xson.tangyuan.rpc.host.RemoteHostManager;
import org.xson.tangyuan.xml.XmlParseException;

public class RpcClientVo {

	public enum ClientType {
		PIGEON, HTTP_CLIENT
	}

	private String     id;
	private String     schema;
	private String     resource;
	/** 共享客户端ID */
	private String     usi;
	private ClientType type = ClientType.HTTP_CLIENT;

	/** 默认的客户端 */
	private boolean    defaultClient;

	public RpcClientVo(String id, String schema, String resource, String usi, boolean defaultClient) {
		this.id = id;
		this.schema = schema;
		this.resource = resource;
		this.usi = usi;
		this.defaultClient = defaultClient;
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

	public String getUsi() {
		return usi;
	}

	public boolean isDefaultClient() {
		return defaultClient;
	}

	public AbstractRpcClient create(BalanceManager balanceManager, RemoteHostManager remoteHostManager) throws Throwable {

		AbstractRpcClient client = null;

		if (ClientType.HTTP_CLIENT == type) {
			XHttpClient xHttpClient = HttpClientManager.getInstance().getXHttpClient(this.usi);
			if (null == xHttpClient) {
				throw new XmlParseException(TangYuanLang.get("xml.tag.attribute.reference.invalid.http-client", this.usi));
			}
			client = new HttpRpcClient(this, xHttpClient, balanceManager, remoteHostManager);
			client.init();
		}

		return client;
	}

	////////////////////////////////////////////////////////////////////////////////////
	//	public RpcClientVo(String id, String schema, String resource, String usi) {
	//		this.id = id;
	//		this.schema = schema;
	//		this.resource = resource;
	//		this.usi = usi;
	//	}
	// if (ClientUseType.PIGEON == use) {
	// return new PigeonRpcClient(this);
	// }
	//	public enum ClientUseType {
	//		PIGEON, HTTP_CLIENT
	//	}
	//	private ClientUseType	use;
	//	public RpcClientVo(String id, ClientUseType use, String schema, String resource, String usi) {
	//		this.id = id;
	//		this.use = use;
	//		this.schema = schema;
	//		this.resource = resource;
	//		this.usi = usi;
	//	}
}
