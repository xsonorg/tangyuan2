package org.xson.tangyuan.ognl.vars.vo;

/**
 * 逻辑表达式单元
 */
public class LogicalVariableItem {

	private Object	value;

	/**
	 * 是否是变量
	 */
	private boolean	variable;

	protected LogicalVariableItem(Object value, boolean variable) {
		this.value = value;
		this.variable = variable;
	}

	public Object getValue() {
		return this.value;
	}

	public boolean isVariable() {
		return variable;
	}

}
