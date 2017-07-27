package org.xson.tangyuan.ognl.vars.vo;

import org.xson.tangyuan.ognl.vars.Variable;

/**
 * SQL占位参数变量(SQL placeholder parameter variable)
 */
public class SPPVariable extends Variable {

	private Variable variable;

	public SPPVariable(String original, Variable variable) {
		this.original = original;
		this.variable = variable;
	}

	@Override
	public Object getValue(Object arg) {
		return this.variable.getValue(arg);
	}
}
