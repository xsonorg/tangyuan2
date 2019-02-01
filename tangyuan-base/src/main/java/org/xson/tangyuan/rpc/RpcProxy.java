package org.xson.tangyuan.rpc;

import java.net.URI;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.zongzi.JPCBridge;

public class RpcProxy {

	private static TangYuanRpc				rpc					= null;

	private static JPCBridge				jpc					= null;

	private static RpcPlaceHolderHandler	placeHolderHandler	= null;

	public static void setRpc(TangYuanRpc rpc) {
		RpcProxy.rpc = rpc;
	}

	public static void setJpc(JPCBridge jpc) {
		RpcProxy.jpc = jpc;
	}

	public static RpcPlaceHolderHandler getPlaceHolderHandler() {
		return placeHolderHandler;
	}

	public static void setPlaceHolderHandler(RpcPlaceHolderHandler placeHolderHandler) {
		RpcProxy.placeHolderHandler = placeHolderHandler;
	}

	public static XCO call(String serviceURI) throws Throwable {
		return call(serviceURI, new XCO());
	}

	public static XCO call(String serviceURI, XCO request) throws Throwable {
		if (null == rpc) {
			// throw new TangYuanException("missing rpc component or requested service does not exist. serviceURI: " + url);
			throw new TangYuanException("missing rpc component when calling service: " + serviceURI);
		}
		// 日志的打印,放在服务所在的系统
		if (null == jpc) {
			return rpc.call(serviceURI, request);
		}

		URI uri = jpc.inJvm(serviceURI);
		if (null != uri) {
			return (XCO) jpc.inJvmCall(uri, request);
		} else {
			return rpc.call(serviceURI, request);
		}
	}

}
