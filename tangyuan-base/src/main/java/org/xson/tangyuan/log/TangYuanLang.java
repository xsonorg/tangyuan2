package org.xson.tangyuan.log;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.util.MessageFormatter;
import org.xson.tangyuan.util.ResourceManager;

/**
 * 日志语言管理
 */
public class TangYuanLang {

	private static TangYuanLang	instance	= new TangYuanLang();

	private String				lang		= "en";

	private Map<String, String>	context		= new HashMap<String, String>();

	public void init(String lang) {
		if (null != lang) {
			this.lang = lang;
		}
	}

	public static TangYuanLang getInstance() {
		return instance;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void load(String baseName) {
		// tangyuan-lang-base-cn.properties
		String resource = baseName + "-" + lang + ".properties";
		try {
			Properties props = ResourceManager.getProperties(resource);
			if (CollectionUtils.isEmpty(props)) {
				return;
			}
			this.context.putAll((Map) props);
		} catch (Throwable e) {
			throw new TangYuanException("Invalid resource: " + resource, e);
		}
	}

	public void reset() {
		this.context.clear();
	}

	private String get0(String template) {
		String val = this.context.get(template);
		if (null == val) {
			val = template;
		}
		return val;
	}

	public static String get(String template) {
		return instance.get0(template);
	}

	public static String get(String template, Object... args) {
		String content = instance.get0(template);
		content = MessageFormatter.formatArgs(content, args);
		return content;
	}
}
