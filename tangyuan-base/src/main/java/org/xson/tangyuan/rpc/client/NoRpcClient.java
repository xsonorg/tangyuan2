package org.xson.tangyuan.rpc.client;

import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.xml.XmlParseException;

public class NoRpcClient extends AbstractRpcClient {

	@Override
	public Object call(String url, Object arg, Object attachment) throws Throwable {
		// 缺少匹配的A或B
		throw new XmlParseException(TangYuanLang.get("rpc.missing.remote-node.client", url));
	}

	@Override
	public void init() throws Throwable {
	}

	@Override
	public void shutdown() {
	}
}
