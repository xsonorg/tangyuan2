package org.xson.tangyuan.rpc;

public interface TangYuanRpc {

	//	XCO call(String url, XCO request) throws Throwable;

	Object call(String url, Object arg, Object attachment) throws Throwable;

}
