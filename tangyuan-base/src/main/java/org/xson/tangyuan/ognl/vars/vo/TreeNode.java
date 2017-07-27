package org.xson.tangyuan.ognl.vars.vo;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.parser.OperaExprParser;

/**
 * 运算表达式:树结构
 */
public class TreeNode {

	public Object	data;
	public TreeNode	left;
	public TreeNode	right;
	/**
	 * 父级
	 */
	public TreeNode	parent;
	/**
	 * 节点类型
	 */
	public int		type;
	/**
	 * 优先级 ,0:非符号,最低, 1:+,-, 2:*,/
	 */
	public int		priority;

	public TreeNode(Object data, int type, int priority, TreeNode parent) {
		this.data = data;
		this.type = type;
		this.priority = priority;
		this.parent = parent;
	}

	public Object getValue(Object arg) {
		if (type == OperaExprParser.TEXTTYPE_VAL) {
			if (data instanceof Variable) {
				return ((Variable) data).getValue(arg);
			}
			return data;
		}
		Object x = left.getValue(arg);
		Object y = right.getValue(arg);
		if (type == OperaExprParser.TEXTTYPE_SYMBOL_PLUS) {
			// if ((x instanceof String) && (y instanceof String)) {
			// return (String) x + (String) y;
			// } else if ((x instanceof Number) && (y instanceof Number)) {
			// return OperaExpr.add((Number) x, (Number) y);
			// }

			// Increase the function
			if ((x instanceof Number) && (y instanceof Number)) {
				return OperaExpr.add((Number) x, (Number) y);
			} else if ((x instanceof String) && (y instanceof String)) {
				return (String) x + (String) y;
			} else if (x instanceof String && null != y) {
				return (String) x + y.toString();
			} else if (null != x && y instanceof String) {
				return x.toString() + (String) y;
			}

		} else if (type == OperaExprParser.TEXTTYPE_SYMBOL_MINUS) {
			if ((x instanceof Number) && (y instanceof Number)) {
				return OperaExpr.minus((Number) x, (Number) y);
			}
		} else if (type == OperaExprParser.TEXTTYPE_SYMBOL_MULTIPLY) {
			if ((x instanceof Number) && (y instanceof Number)) {
				return OperaExpr.multiply((Number) x, (Number) y);
			}
		} else if (type == OperaExprParser.TEXTTYPE_SYMBOL_DIVISION) {
			if ((x instanceof Number) && (y instanceof Number)) {
				return OperaExpr.divide((Number) x, (Number) y);
			}
		} else if (type == OperaExprParser.TEXTTYPE_SYMBOL_REMAINDER) {
			if ((x instanceof Number) && (y instanceof Number)) {
				return OperaExpr.remainder((Number) x, (Number) y);
			}
		}

		// throw new OgnlException("Unsupported operation expression object. x:" + x.getClass().getName() + ", y:" + y.getClass().getName());
		// fixbug Null processing
		throw new OgnlException("Unsupported operation expression object. x:" + ((null == x) ? null : x.getClass().getName()) + ", y:"
				+ ((null == y) ? null : y.getClass().getName()));

	}
}
