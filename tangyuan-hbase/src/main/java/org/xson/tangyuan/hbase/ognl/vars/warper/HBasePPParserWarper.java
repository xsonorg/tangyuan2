package org.xson.tangyuan.hbase.ognl.vars.warper;

import org.xson.tangyuan.hbase.ognl.vars.vo.HBasePPVariable;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.VariableConfig;
import org.xson.tangyuan.ognl.vars.warper.SRPParserWarper;

/**
 * HBase #{}变量解析包装
 */
public class HBasePPParserWarper extends SRPParserWarper {

	@Override
	public Variable parse(String text, VariableConfig config) {
		return new HBasePPVariable(text, parseVariable(text, config));
	}
}
