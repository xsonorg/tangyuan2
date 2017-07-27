package org.xson.tangyuan.mongo.ognl.vars.vo;

import org.xson.tangyuan.ognl.vars.Variable;

/**
 * Mongo占位参数变量(Mongo placeholder parameter variable)
 */
public class MPPVariable extends Variable {

	private Variable variable;

	public MPPVariable(String original, Variable variable) {
		this.original = original;
		this.variable = variable;
	}

	@Override
	public Object getValue(Object arg) {
		return this.variable.getValue(arg);
	}
}
