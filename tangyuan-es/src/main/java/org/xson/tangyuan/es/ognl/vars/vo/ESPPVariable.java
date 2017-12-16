package org.xson.tangyuan.es.ognl.vars.vo;

import org.xson.tangyuan.ognl.vars.Variable;

/**
 * ElasticSearch占位参数变量(ElasticSearch placeholder parameter variable)
 */
public class ESPPVariable extends Variable {

	private Variable variable;

	public ESPPVariable(String original, Variable variable) {
		this.original = original;
		this.variable = variable;
	}

	@Override
	public Object getValue(Object arg) {
		return this.variable.getValue(arg);
	}
}
