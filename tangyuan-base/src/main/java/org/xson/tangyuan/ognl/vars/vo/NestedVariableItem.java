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

	public void addPart(Object part) {
		parts.add(part);
	}

	public String getValue(Object arg) {
		StringBuilder sb = new StringBuilder();
		int length = parts.size();
		for (int i = 0; i < length; i++) {
			Object tmp = parts.get(i);
			if (tmp instanceof String) {
				sb.append(tmp);
			} else {
				sb.append(((NestedVariableItem) tmp).getValue(arg));
			}
		}
		String key = sb.toString().trim();
		System.out.println("key:" + key);
		Object tempVal = new NormalParser().parse(key).getValue(arg);
		if (null == tempVal) {
			throw new OgnlException("Invalid value in nested property: " + key);
		}
		// TODO: 如果是时间类型是否需要特殊处理???
		return tempVal.toString();
	}
}
