package org.xson.tangyuan.app;

import java.util.Map;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.manager.conf.DefaultResourceReloader;

/**
 * Tangyuan 应用占位符
 */
public class AppPlaceholder3 extends DefaultResourceReloader {

	private Log						log			= LogFactory.getLog(getClass());

	private static AppPlaceholder3	instance	= null;

	public static AppPlaceholder3 getInstance(Map<String, String> data) {
		if (null == instance) {
			instance = new AppPlaceholder3(data);
		}
		return instance;
	}

	private Map<String, String> data = null;

	private AppPlaceholder3(Map<String, String> data) {
		this.data = data;
	}

	private void update(Map<String, String> data) {
		this.data = data;
	}

	private Map<String, String> getData0() {
		return this.data;
	}

	public static Map<String, String> getData() {
		return (instance == null) ? null : instance.getData0();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void reload(String resource) throws Throwable {
		Map<String, String> newData = (Map) getPropertiesForReload(resource, null, false, true);
		update(newData);
		// log.info(TangYuanLang.get("resource.reload"), resource);
		log.infoLang("resource.reload", resource);
	}

}
