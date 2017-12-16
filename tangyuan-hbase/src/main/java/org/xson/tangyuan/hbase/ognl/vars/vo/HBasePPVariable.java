package org.xson.tangyuan.hbase.ognl.vars.vo;

import org.xson.tangyuan.ognl.vars.Variable;

/**
 * HBase占位参数变量(HBase placeholder parameter variable)
 */
public class HBasePPVariable extends Variable {

	private Variable variable;

	public HBasePPVariable(String original, Variable variable) {
		this.original = original;
		this.variable = variable;
	}

	@Override
	public Object getValue(Object arg) {
		return this.variable.getValue(arg);
	}
}
