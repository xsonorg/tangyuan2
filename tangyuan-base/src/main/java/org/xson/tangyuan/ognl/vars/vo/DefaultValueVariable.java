package org.xson.tangyuan.ognl.vars.vo;

import org.xson.tangyuan.ognl.vars.Variable;

public class DefaultValueVariable extends Variable {

	private Variable	variable;

	/**
	 * 属性的默认值 #{abccc|0, null, 'xxx', now(), date(), time()}, 只有在变量[#{}|${}]里边才可以存在
	 */
	private Object		defaultValue		= null;

	/**
	 * 默认值类型: 0:普通, 1:now(), 2:date(), 3:time(), 4:timestamp()
	 */
	private int			defaultValueType	= 0;

	public DefaultValueVariable(String original, Variable variable, Object defaultValue, int defaultValueType) {
		this.original = original;
		this.variable = variable;
		this.defaultValue = defaultValue;
		this.defaultValueType = defaultValueType;
	}

	@Override
	public Object getValue(Object arg) {
		Object result = variable.getValue(arg);
		if (null == result) {
			if (0 == defaultValueType) {
				return defaultValue;
			} else if (1 == defaultValueType) {
				return new java.util.Date();
			} else if (2 == defaultValueType) {
				return new java.sql.Date(new java.util.Date().getTime());
			} else if (3 == defaultValueType) {
				return new java.sql.Time(new java.util.Date().getTime());
			} else if (4 == defaultValueType) {
				return new java.sql.Timestamp(new java.util.Date().getTime());
			}
		}
		return result;
	}

}
