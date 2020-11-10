package org.xson.tangyuan.rpc.client;

import org.xson.tangyuan.rpc.TangYuanRpc;
import org.xson.tangyuan.rpc.xml.vo.RpcClientVo;

public abstract class AbstractRpcClient implements TangYuanRpc {

	protected RpcClientVo rpcClientVo;

	protected String getSchema() {
		return rpcClientVo.getSchema();
	}

	abstract public void init() throws Throwable;

	abstract public void shutdown();
}
