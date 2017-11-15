package org.xson.tangyuan.rpc;

/**
 * 远程节点占位处理器
 */
public interface RpcPlaceHolderHandler {

	String parse(String serviceURL);
	
}
