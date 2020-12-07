package org.xson.tangyuan.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.manager.conf.DefaultResourceReloader;
import org.xson.tangyuan.util.CollectionUtils;

/**
 * Tangyuan 应用占位符
 */
public class AppPlaceholder extends DefaultResourceReloader {

	private static AppPlaceholder instance  = null;

	private Log                   log       = LogFactory.getLog(getClass());
	private List<String>          resources = null;
	private Map<String, String>   data      = null;

	public static void init(List<String> resources) throws Throwable {
		if (null == instance) {
			instance = new AppPlaceholder(resources);
		}
	}

	public static AppPlaceholder getInstance() {
		return instance;
	}

	private AppPlaceholder(List<String> resources) throws Throwable {
		this.resources = resources;
		init0();
	}

	private void init0() throws Throwable {
		if (CollectionUtils.isEmpty(this.resources)) {
			return;
		}
		Map<String, String> newData = new HashMap<String, String>();
		for (String resource : this.resources) {
			Properties props = getPropertiesForReload(resource, null, false, true);
			if (CollectionUtils.isEmpty(props)) {
				continue;
			}
			for (Entry<Object, Object> entry : props.entrySet()) {
				String key = entry.getKey().toString();
				if (newData.containsKey(key)) {
					log.warnLang("property.repeated", key, resource);
				}
				newData.put(key, entry.getValue().toString());
			}
		}
		if (newData.isEmpty()) {
			newData = null;
		}
		update(newData);
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

	@Override
	public synchronized void reload(String resource) throws Throwable {
		init0();
		log.infoLang("resource.reload", resource);
	}

}
