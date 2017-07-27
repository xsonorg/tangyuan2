package org.xson.tangyuan.ognl.vars;

/**
 * 变量基础类, 采用装配模式
 */
public abstract class Variable {

	/**
	 * 原始的属性字符串, 用作日志显示
	 */
	protected String original;

	public String getOriginal() {
		return original;
	}

	abstract public Object getValue(Object arg);
}
