package org.xson.tangyuan.ognl.vars.parser;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.vo.NestedVariable;
import org.xson.tangyuan.ognl.vars.vo.NestedVariableItem;

public class NestedParser extends AbstractParser {

	private char	nestedOpenToken		= '{';
	private char	nestedCloseToken	= '}';

	public NestedParser() {
	}

	public NestedParser(char nestedOpenToken, char nestedCloseToken) {
		this.nestedOpenToken = nestedOpenToken;
		this.nestedCloseToken = nestedCloseToken;
	}

	/**
	 * 检查是否存在嵌套属性, 如果存在返回正式的结束作品"pos-->}" <br />
	 * 0:不存在, >0:存在嵌套
	 */
	public int check(String text, int offset, int end) {
		boolean nesting = false; // 是否存在嵌套
		for (int i = offset; i < end; i++) {
			if (nestedOpenToken == text.charAt(i)) {
				nesting = true;
				break;
			}
		}
		if (nesting) {
			// 这里不会存在closeToken不同的隐患
			int count = 1;
			for (int i = offset; i < text.length(); i++) {
				if (nestedOpenToken == text.charAt(i)) {
					count++;
				} else if (nestedCloseToken == text.charAt(i)) {
					count--;
				}
				if (0 == count) {
					return i;
				}
			}
			if (count > 0) {
				throw new OgnlException("Illegal nested property: " + text);
			}
		}
		return 0;
	}

	/**
	 * 解析嵌套表达式
	 */
	public NestedVariable parse(String text) {
		NestedVariableItem nestedItem = new NestedVariableItem();
		parse0(this.nestedOpenToken + text + this.nestedCloseToken, 0, text.length() + 1, nestedItem);
		return new NestedVariable(text, nestedItem);
	}

	/**
	 * {x{xxx}x} // ${user{x{xxx}x}Name{xxx}} d{e}
	 */
	private int parse0(String text, int start, int end, NestedVariableItem nestedItem) {
		if ((end - start) < 3) { // {x}:最短的
			throw new OgnlException("Illegal nested property length: " + text);
		}
		// 当前坐标-->'{'
		start++;
		boolean toContinue = true;
		StringBuilder sb = new StringBuilder();
		while (toContinue) {
			char chr = text.charAt(start);
			if (nestedOpenToken == chr) {
				if (sb.length() > 0) {
					nestedItem.addPart(sb.toString().trim());
					sb = new StringBuilder();
				}
				NestedVariableItem newNestedItem = new NestedVariableItem();
				start = parse0(text, start, end, newNestedItem);
				nestedItem.addPart(newNestedItem);
			} else if (nestedCloseToken == chr) {
				toContinue = false;
				if (sb.length() > 0) {
					nestedItem.addPart(sb.toString().trim());
					sb = new StringBuilder();
				}
				break;
			} else {
				sb.append(chr);
			}
			if ((start + 1) > end) {
				break;
			}
			start++;
		}
		if (sb.length() > 0) {
			// throw new TangYuanException("sb.length() > 0");
			throw new OgnlException("Illegal nested property: " + text);
		}
		return start;// 当前坐标-->'}'
	}

	// public static void main(String[] args) {
	// NestedParser p = new NestedParser();
	// String text = "c{d}e{fg}";
	// p.parse(text);
	// System.out.println("xxxxxxxxxx");
	// }
}
