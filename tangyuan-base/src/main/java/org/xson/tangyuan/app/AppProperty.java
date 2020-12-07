package org.xson.tangyuan.app;

import java.io.InputStream;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.manager.conf.DefaultResourceReloader;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.util.INIXLoader;
import org.xson.tangyuan.xml.nsarg.ExtNsArgWrapper;

/**
 * Tangyuan 应用常量
 */
public class AppProperty extends DefaultResourceReloader implements ExtNsArgWrapper {

	public final static String extNsPrefix = "APP:";
	private static AppProperty instance    = null;

	private Log                log         = LogFactory.getLog(getClass());
	private List<String>       resources   = null;
	private XCO                data        = null;

	public static void init(List<String> resources) throws Throwable {
		if (null == instance) {
			instance = new AppProperty(resources);
		}
	}

	public static AppProperty getInstance() {
		return instance;
	}

	private AppProperty(List<String> resources) throws Throwable {
		this.resources = resources;
		init0();
	}

	private void init0() throws Throwable {
		if (CollectionUtils.isEmpty(this.resources)) {
			return;
		}
		XCO newData = new XCO();
		for (String resource : this.resources) {
			InputStream in = getInputStreamForReload(resource, null, true, true);
			if (null == in) {
				continue;
			}
			XCO one = new INIXLoader().load(in);
			in.close();
			if (one.isEmpty()) {
				continue;
			}
			for (String field : one.keysList()) {
				if (newData.exists(field)) {
					log.warnLang("property.repeated", field, resource);
				}
				newData.setObjectValue(field, one.get(field));
			}
		}
		if (newData.isEmpty()) {
			newData = null;
		}
		update(newData);
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
	public XCO getExtNsArg() {
		return this.data;
	}

	@Override
	public synchronized void reload(String resource) throws Throwable {
		init0();
		log.infoLang("resource.reload", resource);
	}

}
