package org.xson.tangyuan.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xson.tangyuan.TangYuanException;

/**
 * 扩展的带命名空间的参数
 */
public class XmlExtNsArg {

	private static XmlExtNsArg instance = new XmlExtNsArg();

	public static XmlExtNsArg getInstance() {
		return instance;
	}

	private Map<String, Object> extArgMap = new HashMap<String, Object>();

	private XmlExtNsArg() {
	}

	/**
	 * 添加一个外部扩展参数，供XML中使用
	 * 
	 * <p><b>请确保：</b>
	 * 
	 * <p>1. 在系统启动完毕之前调用 
	 * <p>2. 添加的参数作为只读使用 
	 * 
	 * @param prefix
	 * @param extArgObject
	 */
	public void addExtArg(String prefix, Object extArgObject) {

		if (null == prefix || 0 == prefix.length()) {
			throw new TangYuanException("Illegal extended arguments prefix: " + prefix);
		}

		if (prefix.indexOf(":") < 0) {
			throw new TangYuanException("Illegal extended arguments prefix: " + prefix);
		}

		String[] usedArray = new String[] { "DT:", "T:", "DI:", "I:", "D:", ":" };
		for (int i = 0; i < usedArray.length; i++) {
			if (usedArray[i].equalsIgnoreCase(prefix)) {
				throw new TangYuanException("Illegal extension arguments prefix: " + prefix);
			}
		}

		usedArray = new String[] { "~", "!", "@", "#", "$", "%", "^", "&", "*", "-", "+", "|", "'", "\"" };
		for (int i = 0; i < usedArray.length; i++) {
			if (prefix.indexOf(usedArray[i]) > -1) {
				throw new TangYuanException("Illegal extended parameter prefix: " + prefix);
			}
		}

		if (extArgMap.containsKey(prefix)) {
			throw new TangYuanException("existing extension arguments prefix: " + prefix);
		}
		this.extArgMap.put(prefix, extArgObject);
	}

	/**
	 * 获取扩展(外部)参数对象
	 */
	public Object getArg(String prefix) {
		return this.extArgMap.get(prefix);
	}

	public String isExtProperty(String property) {
		if (0 == this.extArgMap.size()) {
			return null;
		}
		Set<String> keys = this.extArgMap.keySet();
		for (String key : keys) {
			if (property.startsWith(key)) {
				return key;
			}
		}
		return null;
	}

}
