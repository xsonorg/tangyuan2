package org.xson.tangyuan.rpc;

import org.xson.common.object.XCO;

public interface TangYuanRpc {

	XCO call(String url, XCO request) throws Throwable;

}
