package org.xson.tangyuan.es.ognl.vars.warper;

import org.xson.tangyuan.es.ognl.vars.vo.ESPPVariable;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.VariableConfig;
import org.xson.tangyuan.ognl.vars.warper.SRPParserWarper;

/**
 * ElasticSearch #{}变量解析包装
 */
public class ESPPParserWarper extends SRPParserWarper {

	@Override
	public Variable parse(String text, VariableConfig config) {
		return new ESPPVariable(text, parseVariable(text, config));
	}
}
