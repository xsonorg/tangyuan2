package org.xson.tangyuan.app;

import java.util.Map;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.conf.DefaultResourceReloader;

/**
 * Tangyuan 应用占位符
 */
public class AppPlaceholder extends DefaultResourceReloader {

	private Log                   log      = LogFactory.getLog(getClass());

	private static AppPlaceholder instance = null;

	public static AppPlaceholder getInstance(Map<String, String> data) {
		if (null == instance) {
			instance = new AppPlaceholder(data);
		}
		return instance;
	}

	private Map<String, String> data = null;

	private AppPlaceholder(Map<String, String> data) {
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
		log.info(TangYuanLang.get("resource.reload"), resource);
	}

	//////////////////////////////////////////////////////////////////////////////////////

	//	@Override
	//	@SuppressWarnings({ "unchecked", "rawtypes" })
	//	public void reload(String resource, String context) throws Throwable {
	//		Map<String, String> newData = (Map) getPropertiesForReload(resource, context, false, true);
	//		update(newData);
	//		log.info(TangYuanLang.get("resource.reload"), resource);
	//	}
	//	@Override
	//	@SuppressWarnings({ "unchecked", "rawtypes" })
	//	public void reload(String resource, String context) throws Throwable {
	//		final Map<String, String> data         = this.data;
	//		Map<String, String>       resourceData = (Map) getPropertiesForReload(resource, context, false, true);
	//		boolean                   changeFlag   = false;
	//		if (null == data) {
	//			this.data = resourceData;
	//			changeFlag = true;
	//		} else if (null != resourceData && resourceData.size() > 0) {
	//			Map<String, String> newData = new HashMap<String, String>();
	//			newData.putAll(data);
	//			newData.putAll(resourceData);
	//			if (newData.size() > 0) {//防止指令重排
	//				this.data = newData;
	//				changeFlag = true;
	//			}
	//		}
	//		log.info(TangYuanLang.get("resource.reload"), resource);
	//		if (changeFlag) {
	//			notice();
	//		}
	//	}
	//	notice();
	//	private void notice() {
	//		// 告知使用方,静态方式
	//	}
}
