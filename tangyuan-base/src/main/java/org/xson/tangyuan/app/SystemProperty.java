package org.xson.tangyuan.app;

import java.util.Map.Entry;
import java.util.Properties;

import org.xson.common.object.XCO;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.xml.nsarg.ExtNsArgWrapper;

/**
 * 系统属性
 */
public class SystemProperty implements ExtNsArgWrapper {

	public final static String    extNsPrefix = "SYS:";

	private static SystemProperty instance    = new SystemProperty();

	private XCO                   data        = new XCO();

	private SystemProperty() {
		init();
	}

	public static SystemProperty getInstance() {
		return instance;
	}

	private void init() {
		Properties props = System.getProperties();
		if (CollectionUtils.isEmpty(props)) {
			return;
		}
		for (Entry<Object, Object> entry : props.entrySet()) {
			String field = entry.getKey().toString();
			Object value = entry.getValue();
			data.setObjectValue(field, value);
		}
	}

	private Object get0(String key) {
		return data.get(key);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(String key) {
		return (T) instance.get0(key);
	}

	@Override
	public XCO getExtNsArg() {
		return this.data;
	}

}
