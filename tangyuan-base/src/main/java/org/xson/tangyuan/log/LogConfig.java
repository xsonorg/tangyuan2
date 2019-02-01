package org.xson.tangyuan.log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.util.Resources;

public class LogConfig {

	/**
	 * 关闭日志过滤
	 */
	private boolean					disableFilterLog	= true;
	/**
	 * 开启上下文记录
	 */
	private boolean					enableContextLog	= false;

	private boolean					includeComponentAll	= true;
	private boolean					excludeComponentAll	= false;

	private boolean					includeTypeAll		= true;
	private boolean					excludeTypeAll		= false;

	private Map<String, Integer>	includeComponentMap	= null;
	private Map<String, Integer>	excludeComponentMap	= null;

	private Map<String, Integer>	includeTypeMap		= null;
	private Map<String, Integer>	excludeTypeMap		= null;

	private void fillToMap(String[] array, Map<String, Integer> map) {
		for (int i = 0; i < array.length; i++) {
			map.put(array[i].trim().toUpperCase(), 1);
		}
	}

	private String getEmpty(String str) {
		if (null == str) {
			return null;
		}
		str = str.trim();
		if (0 == str.length()) {
			return null;
		}
		return str;
	}

	public void init() {
		// 1. 首先加载配置文件
		Properties p = null;
		try {
			p = Resources.getResourceAsProperties("tangyuan-log.properties");
		} catch (IOException e1) {
			try {
				p = Resources.getResourceAsProperties("properties/tangyuan-log.properties");
			} catch (IOException e) {
			}
		}

		if (null == p) {
			return;
		}

		String enable_log_filter = getEmpty(p.getProperty("enable_log_filter"));
		if (null != enable_log_filter) {
			disableFilterLog = !Boolean.parseBoolean(enable_log_filter);
		}

		String enable_context_log = getEmpty(p.getProperty("enable_context_log"));
		if (null != enable_context_log) {
			enableContextLog = Boolean.parseBoolean(enable_context_log);
		}

		String exclude_component = getEmpty(p.getProperty("exclude_component"));
		if (null != exclude_component) {
			if ("*".equals(exclude_component)) {
				excludeComponentAll = true;
			} else {
				excludeComponentMap = new HashMap<String, Integer>();
				fillToMap(exclude_component.split(","), excludeComponentMap);
			}
		}

		String include_component = getEmpty(p.getProperty("include_component"));
		if (null != include_component) {
			if ("*".equals(include_component)) {
				includeComponentAll = true;
			} else {
				includeComponentMap = new HashMap<String, Integer>();
				fillToMap(include_component.split(","), includeComponentMap);
			}
		}

		String exclude_type = getEmpty(p.getProperty("exclude_origin"));
		if (null != exclude_type) {
			if ("*".equals(exclude_type)) {
				excludeTypeAll = true;
			} else {
				excludeTypeMap = new HashMap<String, Integer>();
				fillToMap(exclude_type.split(","), excludeTypeMap);
			}
		}

		String include_type = getEmpty(p.getProperty("include_origin"));
		if (null != include_type) {
			if ("*".equals(include_type)) {
				includeTypeAll = true;
			} else {
				includeTypeMap = new HashMap<String, Integer>();
				fillToMap(include_type.split(","), includeTypeMap);
			}
		}

		// optimize
		if (null == exclude_component && null == include_component && null == exclude_type && null == include_type) {
			disableFilterLog = true;
		}

		String redirect_local_path = getEmpty(p.getProperty("log_redirect_path"));
		String log_append = getEmpty(p.getProperty("log_redirect_append"));
		boolean append = true;
		if (null != log_append) {
			append = Boolean.parseBoolean(log_append);
		}
		if (null != redirect_local_path) {
			redirect_local_path = redirect_local_path.replaceAll("\\\\", "/");
			try {
				OutputStream os = new FileOutputStream(redirect_local_path, append);
				PrintStream ps = new PrintStream(os);
				System.setOut(ps);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isEnableContextLog() {
		return enableContextLog;
	}

	public boolean isExclude(String type, String component) {

		if (disableFilterLog) {
			return false;
		}

		// 1. 先检查排除的
		if (null != type) {
			if (excludeTypeAll) {
				return true;
			}
			if (null != excludeTypeMap && excludeTypeMap.containsKey(type)) {
				return true;
			}
		}
		if (null != component) {
			if (excludeComponentAll) {
				return true;
			}
			if (null != excludeComponentMap && excludeComponentMap.containsKey(component)) {
				return true;
			}
		}

		// 2. 再检查排除包含
		int notInclude = 0;
		if (null != type) {
			if (includeTypeAll) {
				return false;
			}
			if (null != includeTypeMap && includeTypeMap.containsKey(type)) {
				return false;
			} else {
				notInclude++;
			}
		}

		if (null != component) {
			if (includeComponentAll) {
				return false;
			}
			if (null != includeComponentMap && includeComponentMap.containsKey(component)) {
				return false;
			} else {
				notInclude++;
			}
		}

		if (notInclude == 2) {
			return true;
		}

		return false;
	}

}
