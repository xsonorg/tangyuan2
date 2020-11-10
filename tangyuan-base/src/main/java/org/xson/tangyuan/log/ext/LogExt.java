package org.xson.tangyuan.log.ext;

import java.util.Properties;

import org.xson.tangyuan.log.jdk.JdkLogProxy;
import org.xson.tangyuan.manager.conf.DefaultResourceReloader;
import org.xson.tangyuan.manager.conf.ResourceReloaderVo;
import org.xson.tangyuan.xml.XmlGlobalContext;

public class LogExt extends DefaultResourceReloader {

	private LogExtVo extVo = null;

	public static LogExt newInstance() {
		JdkLogProxy log      = new JdkLogProxy(LogExt.class.getName());
		LogExt      f        = new LogExt();
		boolean     initFlag = false;
		String      resource = null;
		try {
			resource = "tangyuan-log-ext.properties";
			initFlag = f.init(resource);
		} catch (Throwable e) {
			log.error(e);
		}
		if (!initFlag) {
			try {
				resource = "properties/tangyuan-log-ext.properties";
				initFlag = f.init(resource);
			} catch (Throwable e) {
				log.error(e);
			}
		}
		if (initFlag) {
			log.info("load resource: {}", resource);
			// 注册Reloader
			XmlGlobalContext.addReloaderVo(new ResourceReloaderVo(resource, f));
			return f;
		}
		return null;
	}

	private LogExt() {
	}

	private void update(LogExtVo extVo) {
		this.extVo = extVo;
	}

	public boolean init(String resource) throws Throwable {
		Properties p = null;
		try {
			p = getPropertiesForReload(resource, null, false, true);
		} catch (Throwable e) {
		}
		if (null != p) {
			update(new LogExtVo(p));
			return true;
		}
		return false;
	}

	@Override
	public void reload(String resource) throws Throwable {
		Properties p = getPropertiesForReload(resource, null, false, true);
		if (null != p) {
			update(new LogExtVo(p));
		}
	}

	public boolean isExclude(String origin) {
		return this.extVo.isExclude(origin);
	}

	public boolean isEnableContextLog() {
		return this.extVo.isEnableContextLog();
	}

	public LogExtVo getExtVo() {
		return extVo;
	}

	//	public boolean init(String resource) throws Throwable {
	//		Properties p = null;
	//		try {
	//			p = MixedResourceManager.getProperties(resource, false, true);
	//		} catch (Throwable e) {
	//		}
	//		if (null != p) {
	//			update(new LogExtVo(p));
	//			return true;
	//		}
	//		return false;
	//	}

	//	@Override
	//	public void reload(String resource, String context) throws Throwable {
	//		Properties p = null;
	//		try {
	//			if (null != context) {
	//				p = new Properties();
	//				p.load(new ByteArrayInputStream(context.getBytes(StandardCharsets.UTF_8)));
	//			} else {
	//				p = MixedResourceManager.getProperties(resource, false, true);
	//			}
	//		} catch (Throwable e) {
	//			throw e;
	//		}
	//		if (null != p) {
	//			update(new LogExtVo(p));
	//		}
	//	}
}
