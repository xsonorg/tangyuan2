package org.xson.tangyuan.rpc;

import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.runtime.RuntimeContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

/**
 * RPC代理服务
 */
public class RpcServiceNode extends AbstractServiceNode {

	public RpcServiceNode(String serviceURI) {
		this.serviceKey = serviceURI;
		this.serviceType = TangYuanServiceType.PRCPROXY;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		// Object result = RpcProxy.call(serviceKey, (XCO) arg);
		// context.setResult(result);
		// return true;

		boolean sr = RuntimeContext.setHeader(arg);

		context.addTrackingHeader(arg);
		try {
			Object result = RpcProxy.call(serviceKey, (XCO) arg);
			context.setResult(result);
			return true;
		} catch (Throwable e) {
			throw e;
		} finally {

			if (sr) {
				RuntimeContext.cleanHeader(arg);
			}

			context.cleanTrackingHeader(arg);
		}
	}

}
