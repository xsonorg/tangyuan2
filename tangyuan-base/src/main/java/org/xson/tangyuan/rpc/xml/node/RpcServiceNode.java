package org.xson.tangyuan.rpc.xml.node;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.TangYuanManager;
import org.xson.tangyuan.rpc.RpcComponent;
import org.xson.tangyuan.rpc.TangYuanRpc;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

/**
 * RPC代理服务
 */
public class RpcServiceNode extends AbstractServiceNode {

	private static TangYuanRpc     rpc             = null;
	private static TangYuanManager tangYuanManager = null;

	//	private String                 serviceURI      = null;

	static {
		rpc = RpcComponent.getInstance().getRpcClient();
		tangYuanManager = TangYuanManager.getInstance();
	}

	//	public RpcServiceNode(String serviceKey, String serviceURI) {
	//		// 支持URL参数: http://www.baidu.com/a/b?x=c
	//		this.serviceType = TangYuanServiceType.PRCPROXY;
	//		this.serviceKey = serviceKey;
	//		this.serviceURI = serviceURI;// TODO有问题
	//	}

	public RpcServiceNode(String serviceURI) {
		this.serviceType = TangYuanServiceType.PRCPROXY;
		this.serviceKey = serviceURI;
	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object acArg) throws Throwable {
		if (null == rpc) {
			// throw new TangYuanException("Missing RPC component support.");// 缺少RPC组件的支持
			throw new TangYuanException(TangYuanLang.get("component.missing", "RPC"));
		}
		boolean appendHeader = false;
		try {
			if (null != tangYuanManager) {
				appendHeader = tangYuanManager.appendTrackingHeader(arg);
			}
			Object result = rpc.call(serviceKey, arg, null);
			//			Object result = rpc.call(serviceURI, arg, null);
			ac.setResult(result);
			return true;
		} catch (Throwable e) {
			throw e;
		} finally {
			if (appendHeader) {
				tangYuanManager.cleanAppendTrackingHeader(arg);
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////////

	//	Object  argWrapper   = arg;
	//		Object argWrapper = RuntimeContext.appendHeader(arg);
	//		if (null != argWrapper) {
	//			arg = argWrapper;
	//		}
	//		Object result = rpc.call(serviceKey, arg, null);
	//		if (null != tangYuanManager) {
	//			tangYuanManager.appendHeader(arg);
	//		}
	//		boolean sr = RuntimeContext.setHeader(arg);
	//		try {
	//			// Object result = RpcProxy.call(serviceKey, (XCO) arg);
	//			//			Object result = rpc.call(serviceKey, (XCO) arg);
	//			Object result = rpc.call(serviceKey, arg, null);
	//			context.setResult(result);
	//			return true;
	//		} catch (Throwable e) {
	//			throw e;
	//		} finally {
	//			if (sr) {
	//				RuntimeContext.cleanHeader(arg);
	//			}
	//		}
	//	private static TangYuanManager tangYuanManager;
}
