package org.xson.tangyuan.rpc;

import java.net.URI;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.zongzi.JPCBridge;

public class RpcProxy {

	private static TangYuanRpc	rpc	= null;

	private static JPCBridge	jpc	= null;

	public static void setRpc(TangYuanRpc rpc) {
		RpcProxy.rpc = rpc;
	}

	public static void setJpc(JPCBridge jpc) {
		RpcProxy.jpc = jpc;
	}

	public static XCO call(String url) throws Throwable {
		return call(url, new XCO());
	}

	public static XCO call(String url, XCO request) throws Throwable {
		if (null == rpc) {
			throw new TangYuanException("missing rpc component.");
		}
		// 日志的打印,放在服务所在的系统
		if (null == jpc) {
			return rpc.call(url, request);
		} else {
			URI uri = jpc.inJvm(url);
			if (null != uri) {
				return (XCO) jpc.inJvmCall(uri, request);
			} else {
				return rpc.call(url, request);
			}
		}
	}

}
