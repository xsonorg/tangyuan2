package org.xson.tangyuan.ognl.vars.warper;

import org.xson.tangyuan.ognl.vars.ParserWarper;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.VariableConfig;
import org.xson.tangyuan.ognl.vars.parser.CallParser;
import org.xson.tangyuan.ognl.vars.parser.DefaultValueParser;
import org.xson.tangyuan.ognl.vars.parser.NestedParser;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.ognl.vars.parser.OperaExprParser;

/**
 * SQL ${} 变量解析包装
 */
public class SRPParserWarper extends ParserWarper {

	protected NestedParser nestedParser = new NestedParser();

	public NestedParser getNestedParser() {
		return this.nestedParser;
	}

	protected Variable parseVariable(String text, VariableConfig config) {

		text = text.trim();

		// 嵌套
		if (config.isExistNested()) {
			config.setExistNested(false);
			return nestedParser.parse(text);
		}

		// 是否是调用表达式
		CallParser callParser = new CallParser();
		if (callParser.check(text)) {
			return callParser.parse(text);
		}

		// fix bug:默认值处理(提前)
		DefaultValueParser defaultValueParser = new DefaultValueParser();
		if (defaultValueParser.check(text)) {
			return defaultValueParser.parse(text);
		}

		// 是否是运算表达式, 只包含[+,-,*,/,%]
		OperaExprParser exprParser = new OperaExprParser();
		if (exprParser.check(text)) {
			return exprParser.parse(text);
		}

		// DefaultValueParser defaultValueParser = new DefaultValueParser();
		// if (defaultValueParser.check(text)) {
		// return defaultValueParser.parse(text);
		// }

		// 普通变量
		return new NormalParser().parse(text);
	}

	@Override
	public Variable parse(String text, VariableConfig config) {
		return parseVariable(text, config);
	}
}
