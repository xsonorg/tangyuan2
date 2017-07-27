package org.xson.tangyuan.ognl.vars;

import org.xson.tangyuan.ognl.vars.parser.NestedParser;

/**
 * Parser包装类,可以包装多个Parser的实现
 */
public abstract class ParserWarper {

	public NestedParser getNestedParser() {
		return null;
	}

	abstract public Variable parse(String text, VariableConfig config);

}
