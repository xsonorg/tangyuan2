package org.xson.tangyuan.ognl.vars.parser;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.vo.LogicalVariable;

/**
 * test 逻辑表达式解析器
 */
public class LogicalExprParser {

	public LogicalVariable parse(String var) {
		char[] src = var.toCharArray();
		StringBuilder builder = new StringBuilder();
		LogicalVariable exprGroup = new LogicalVariable();

		boolean isStringGathering = false; // 是否进入字符串采集
		for (int i = 0; i < src.length; i++) {
			char key = src[i];
			switch (key) {
			case '\'': // 开始或结束字符串采集
				if (isStringGathering) {
					exprGroup.addUnit(builder.toString(), true);
					if (builder.length() > 0) {
						builder = new StringBuilder();
					}
					isStringGathering = false;
				} else {
					if (builder.length() > 0) {
						exprGroup.addUnit(builder.toString(), true);
						builder = new StringBuilder();
					}
					isStringGathering = true;
				}
				break;
			case ' ': // 空格
				if (isStringGathering) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						exprGroup.addUnit(builder.toString(), false);
						builder = new StringBuilder();
					}
				}
				break;
			case '!': // 判断开始
				if (!isStringGathering && builder.length() > 0) {
					exprGroup.addUnit(builder.toString(), false);
					builder = new StringBuilder();
				}
				builder.append(key);
				break;
			case '=': // 判断开始或结束
				if (isStringGathering) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						String tmp = builder.toString();
						if ("=".equals(tmp) || "!".equals(tmp) || ">".equals(tmp) || "<".equals(tmp)) {
							builder.append(key);
							exprGroup.addOperators(builder.toString());
							builder = new StringBuilder();
						} else {
							exprGroup.addUnit(builder.toString(), false);
							builder = new StringBuilder();
							builder.append(key);
						}
					} else {
						builder.append(key);
					}
				}
				break;
			case '>': // 大于
			case '<': // 小于
				if (isStringGathering) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						exprGroup.addUnit(builder.toString(), false);
						builder = new StringBuilder();
					}
					// 当满足一下情况, 立即采集,
					if ((i + 1) < src.length && '=' != src[i + 1]) {
						exprGroup.addOperators(key + "");
					} else {
						builder.append(key);// 继续
					}
				}
				break;
			default:
				builder.append(key);
			}
		}

		if (builder.length() > 0) {
			exprGroup.addUnit(builder.toString(), false);
		}

		if (!exprGroup.check()) {
			throw new OgnlException("不合法的Test表达式:" + var);
		}
		return exprGroup;
	}

	public LogicalVariable parseEscape(String var) {
		char[] src = var.toCharArray();
		StringBuilder builder = new StringBuilder();
		LogicalVariable exprGroup = new LogicalVariable();

		boolean isStringGathering = false; // 是否进入字符串采集
		for (int i = 0; i < src.length; i++) {
			char key = src[i];
			switch (key) {
			case '\'': // 开始或结束字符串采集
				if (isStringGathering) {
					exprGroup.addUnit(builder.toString(), true);
					if (builder.length() > 0) {
						builder = new StringBuilder();
					}
					isStringGathering = false;
				} else {
					if (builder.length() > 0) {
						exprGroup.addUnit(builder.toString(), true);
						builder = new StringBuilder();
					}
					isStringGathering = true;
				}
				break;
			case ' ': // 空格
				if (isStringGathering) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						exprGroup.addUnit(builder.toString(), false);
						builder = new StringBuilder();
					}
				}
				break;
			case '!': // 判断开始
				if (!isStringGathering && builder.length() > 0) {
					exprGroup.addUnit(builder.toString(), false);
					builder = new StringBuilder();
				}
				builder.append(key);
				break;
			case '=': // 判断开始或结束
				if (isStringGathering) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						String tmp = builder.toString();
						if ("=".equals(tmp) || "!".equals(tmp) || "&lt;".equals(tmp) || "&gt;".equals(tmp)) {
							builder.append(key);
							exprGroup.addOperators(builder.toString());
							builder = new StringBuilder();
						} else {
							exprGroup.addUnit(builder.toString(), false);
							builder = new StringBuilder();
							builder.append(key);
						}
					} else {
						builder.append(key);
					}
				}
				break;
			case '&': // 大于小于的转义开始
				if (!isStringGathering && builder.length() > 0) {
					exprGroup.addUnit(builder.toString(), false);
					builder = new StringBuilder();
				}
				builder.append(key);
				break;
			case ';': // 大于小于的转义结束
				builder.append(key);
				if (!isStringGathering) {
					if ((i + 1) < src.length && '=' != src[i + 1]) {
						String tmp = builder.toString();
						// &lt; < 小于号 &gt; > 大于号
						if ("&lt;".equals(tmp) || "&gt;".equals(tmp)) {
							exprGroup.addOperators(tmp);
							builder = new StringBuilder();
						}
					}
				}
				break;
			default:
				builder.append(key);
			}
		}

		if (builder.length() > 0) {
			exprGroup.addUnit(builder.toString(), false);
		}

		if (!exprGroup.check()) {
			throw new OgnlException("不合法的Test表达式:" + var);
		}
		return exprGroup;
	}
}
