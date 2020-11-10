package org.xson.tangyuan.rpc;

public interface TangYuanRpc {

	Object call(String url, Object arg, Object attachment) throws Throwable;

}
