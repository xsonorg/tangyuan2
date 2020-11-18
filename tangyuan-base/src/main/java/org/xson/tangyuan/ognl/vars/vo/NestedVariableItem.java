package org.xson.tangyuan.ognl.vars.vo;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;

/**
 * 嵌套属性,内部
 */
public class NestedVariableItem {

	private List<Object> parts = new ArrayList<Object>();
	private int          level = 0;

	public NestedVariableItem(int level) {
		this.level = level;
	}

	public void addPart(Object part) {
		parts.add(part);
	}

	public Object getValue(Object arg) {
		StringBuilder sb     = new StringBuilder();
		int           length = parts.size();
		for (int i = 0; i < length; i++) {
			Object tmp = parts.get(i);
			if (tmp instanceof String) {
				sb.append(tmp);
			} else {
				sb.append(((NestedVariableItem) tmp).getValue(arg));
			}
		}
		String key     = sb.toString().trim();
		Object tempVal = new NormalParser().parse(key).getValue(arg);
		if (null == tempVal) {
			throw new OgnlException("Invalid value in nested property: " + key);
		}
		if (0 == level) {
			return tempVal;
		}
		return tempVal.toString();

		// System.out.println("key:" + key);
		// 如果是时间类型是否需要特殊处理???
		// return tempVal.toString();
	}
}
