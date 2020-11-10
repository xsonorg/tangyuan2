package org.xson.tangyuan.app;

import java.io.InputStream;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.manager.conf.DefaultResourceReloader;
import org.xson.tangyuan.util.INIXLoader;

/**
 * Tangyuan 应用常量
 */
public class AppProperty extends DefaultResourceReloader {

	private Log					log			= LogFactory.getLog(getClass());

	private static AppProperty	instance	= null;

	public static AppProperty getInstance(XCO data) {
		if (null == instance) {
			instance = new AppProperty(data);
		}
		return instance;
	}

	private XCO data = null;

	private AppProperty(XCO data) {
		this.data = data;
	}

	private void update(XCO data) {
		this.data = data;
	}

	private Object get0(String key) {
		final XCO data = this.data;
		if (null != data) {
			return data.get(key);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(String key) {
		return (T) ((instance == null) ? null : instance.get0(key));
	}

	@Override
	public void reload(String resource) throws Throwable {
		InputStream in = getInputStreamForReload(resource, null, true, true);
		if (null != in) {
			XCO newData = new INIXLoader().load(in, null);
			in.close();
			update(newData);
			// log.info(TangYuanLang.get("resource.reload"), resource);
			log.infoLang("resource.reload", resource);
		}
	}

}
