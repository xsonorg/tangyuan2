package org.xson.tangyuan.ognl.vars.vo;

import org.xson.tangyuan.ognl.vars.Variable;

/**
 * 普通变量
 */
public class NormalVariable extends Variable {

	private VariableItemWraper item;

	public NormalVariable(String original, VariableItemWraper item) {
		this.original = original;
		this.item = item;
	}

	public Object getValue(Object arg) {
		return item.getValue(arg);
	}
}
