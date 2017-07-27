package org.xson.tangyuan.ognl.vars.warper;

import org.xson.tangyuan.ognl.vars.ParserWarper;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.VariableConfig;
import org.xson.tangyuan.ognl.vars.parser.DefaultValueParser;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;

/**
 * 默认值变量解析包装
 */
public class DefaultValueParserWarper extends ParserWarper {

	public Variable parse(String text) {
		return parse(text, null);
	}

	@Override
	public Variable parse(String text, VariableConfig config) {
		DefaultValueParser defaultValueParser = new DefaultValueParser();
		if (defaultValueParser.check(text)) {
			return defaultValueParser.parse(text);
		}
		// 普通变量
		return new NormalParser().parse(text);
	}
}
