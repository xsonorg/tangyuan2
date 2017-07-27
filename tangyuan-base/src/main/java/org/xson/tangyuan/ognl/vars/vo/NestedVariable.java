package org.xson.tangyuan.ognl.vars.vo;

import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;

/**
 * 嵌套变量
 */
public class NestedVariable extends Variable {

	private NestedVariableItem nestedItem;

	public NestedVariable(String original, NestedVariableItem nestedItem) {
		this.original = original;
		this.nestedItem = nestedItem;
	}

	public Object getValue(Object arg) {
		// return nestedItem.getValue(arg);
		// 通过递归解析, 获取最终的字符串
		String key = nestedItem.getValue(arg);
		// 通过解析最终的字符串, 获取变量对象,并取值
		return new NormalParser().parse(key).getValue(arg);
	}
}
