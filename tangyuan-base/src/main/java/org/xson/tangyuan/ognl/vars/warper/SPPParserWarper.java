package org.xson.tangyuan.ognl.vars.warper;

import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.VariableConfig;
import org.xson.tangyuan.ognl.vars.vo.SPPVariable;

/**
 * SQL#{}变量解析包装
 */
public class SPPParserWarper extends SRPParserWarper {

	@Override
	public Variable parse(String text, VariableConfig config) {
		return new SPPVariable(text, parseVariable(text, config));
	}
}
