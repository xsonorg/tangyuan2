package org.xson.tangyuan.rpc.client;

import org.xson.tangyuan.rpc.TangYuanRpc;
import org.xson.tangyuan.rpc.xml.RpcClientVo;

public abstract class AbstractClientRpc implements TangYuanRpc {

	protected RpcClientVo rpcClientVo;

	protected String getSchema() {
		return rpcClientVo.getSchema();
	}
}
