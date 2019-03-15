package org.xson.tangyuan.ognl.vars.warper;

import org.xson.tangyuan.ognl.vars.ParserWarper;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.VariableConfig;
import org.xson.tangyuan.ognl.vars.parser.CallParser;
import org.xson.tangyuan.ognl.vars.parser.DefaultValueParser;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.ognl.vars.parser.OperaExprParser;

/**
 * General attribute ParserWarper: 通用XML属性变量解析
 */
public class GAParserWarper extends ParserWarper {

	@Override
	public Variable parse(String text, VariableConfig config) {

		text = text.trim();

		// 嵌套
		// if (config.isExistNested()) {
		// config.setExistNested(false);
		// return nestedParser.parse(text);
		// }

		// 是否是调用表达式
		CallParser callParser = new CallParser();
		if (callParser.check(text)) {
			return callParser.parse(text);
		}

		// fixBug: 默认值处理
		DefaultValueParser defaultValueParser = new DefaultValueParser();
		if (defaultValueParser.check(text)) {
			return defaultValueParser.parse(text);
		}

		// 是否是运算表达式, 只包含[+,-,*,/,%]
		OperaExprParser exprParser = new OperaExprParser();
		if (exprParser.check(text)) {
			return exprParser.parse(text);
		}

		// 普通变量
		return new NormalParser().parse(text);
	}

	public Variable parse(String text) {
		return parse(text, null);
	}
}
