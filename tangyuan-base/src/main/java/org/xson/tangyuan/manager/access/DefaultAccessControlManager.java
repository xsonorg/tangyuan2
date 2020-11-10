package org.xson.tangyuan.manager.access;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.access.acr.DefaultACRManager;
import org.xson.tangyuan.manager.access.xml.XmlAccessBuilder;
import org.xson.tangyuan.manager.conf.DefaultResourceReloader;

/**
 * 授权管理器
 */
public class DefaultAccessControlManager extends DefaultResourceReloader implements AccessControlManager {

	private Log               log               = LogFactory.getLog(getClass());

	private DefaultACRManager defaultACRManager = null;

	@Override
	public void init(String resource) throws Throwable {
		DefaultACRManager defaultACRManager = reload0(resource);
		update(defaultACRManager);
		//		XmlAccessBuilder builder = new XmlAccessBuilder();
		//		builder.parse(null, resource);
		//		this.defaultACRManager = builder.getDefaultACRManager();
		//		builder.clean();
	}

	private DefaultACRManager reload0(String resource) throws Throwable {
		XmlAccessBuilder builder = new XmlAccessBuilder();
		builder.parse(null, resource);
		DefaultACRManager defaultACRManager = builder.getDefaultACRManager();
		builder.clean();
		return defaultACRManager;
	}

	private void update(DefaultACRManager defaultACRManager) {
		this.defaultACRManager = defaultACRManager;
	}

	@Override
	public void reload(String resource) throws Throwable {
		DefaultACRManager defaultACRManager = reload0(resource);
		update(defaultACRManager);
		log.info(TangYuanLang.get("resource.reload"), resource);
	}

	@Override
	public boolean check(String service, String remoteIp, String remoteDomain) {
		// 使用内部变量，可以防止优化并发，防止空指针
		final DefaultACRManager defaultACRManager = this.defaultACRManager;
		if (null == defaultACRManager) {
			return true;
		}
		return defaultACRManager.check(service, remoteIp, remoteDomain);
	}

	//	@Override
	//	public void reload(String resource, String context) throws Throwable {
	//		InputStream      inputStream = getInputStreamForReload(resource, context, false, false);
	//		XmlAccessBuilder builder     = new XmlAccessBuilder();
	//		builder.parse(null, resource, inputStream);
	//		final DefaultACRManager tempDefaultACRManager = builder.getDefaultACRManager();
	//		builder.clean();
	//		this.defaultACRManager = tempDefaultACRManager;
	//	}
}
