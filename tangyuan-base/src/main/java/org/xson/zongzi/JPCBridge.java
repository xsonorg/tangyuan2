package org.xson.zongzi;

import java.net.URI;

/**
 * JPC: JVM Procedure Call Protocol
 */
public interface JPCBridge {

	public Object call(String url, Object request);

	public URI inJvm(String url);

	/** 在同一个JVM中调用服务 */
	public Object inJvmCall(URI uri, Object request);

	/** 设置钩子函数 */
	public void setHook(Object obj);

	/** 获取服务信息 */
	public Object getServiceInfo();
}
