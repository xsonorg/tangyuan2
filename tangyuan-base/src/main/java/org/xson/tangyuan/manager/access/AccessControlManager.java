package org.xson.tangyuan.manager.access;

/**
 * 授权管理器
 */
public interface AccessControlManager {

	void init(String resource) throws Throwable;

	boolean check(String service, String remoteIp, String remoteDomain);

}
