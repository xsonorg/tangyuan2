package org.xson.tangyuan.ognl.vars.vo;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;

/**
 * 逻辑表达式
 */
public class LogicalVariableWraper {

	public static int			exprOperators1	= 1;	// ==
	public static int			exprOperators2	= 2;	// !=
	public static int			exprOperators3	= 3;	// >
	public static int			exprOperators4	= 4;	// >=
	public static int			exprOperators5	= 5;	// <
	public static int			exprOperators6	= 6;	// <=
	public static int			exprOperators9	= 9;	// ||

	private LogicalVariableItem	unit1;
	private int					operators;
	private LogicalVariableItem	unit2;

	private boolean isVar(String var) {
		if (var.length() > 2 && var.startsWith("{") && var.endsWith("}")) {
			return true;
		}
		return false;
	}

	private String getRealVar(String var) {
		return var.substring(1, var.length() - 1);
	}

	private boolean isNumber(String var) {
		return var.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}

	private LogicalVariableItem getNumberUnit(String var) {
		LogicalVariableItem unit = null;
		if (var.indexOf(".") == -1) {
			try {
				unit = new LogicalVariableItem(Integer.parseInt(var), false);
			} catch (NumberFormatException e) {
				unit = new LogicalVariableItem(Long.parseLong(var), false);
			}
		} else {
			try {
				unit = new LogicalVariableItem(Float.parseFloat(var), false);
			} catch (NumberFormatException e) {
				unit = new LogicalVariableItem(Double.parseDouble(var), false);
			}
		}
		return unit;
	}

	public void addUnit(String var, boolean isStringConstant) {
		LogicalVariableItem unit = null;
		if (isStringConstant) {
			// 常量字符串,''
			unit = new LogicalVariableItem(var, false);
		} else {
			// null, true, false, and or
			if ("null".equalsIgnoreCase(var)) {
				unit = new LogicalVariableItem(null, false);
			} else if ("and".equalsIgnoreCase(var) || "or".equalsIgnoreCase(var)) {
				addOperators(var);
			} else if ("true".equalsIgnoreCase(var) || "false".equalsIgnoreCase(var)) {
				unit = new LogicalVariableItem(Boolean.parseBoolean(var), false);
			} else {
				if (isVar(var)) { // 是否是变量:{}
					// unit = new LogicalVariableItem(VariableParser.parse(getRealVar(var), false), true);
					unit = new LogicalVariableItem(new NormalParser().parse(getRealVar(var)), true);
				} else if (isNumber(var)) { // 是否是数字常量
					unit = getNumberUnit(var);
				} else {
					throw new OgnlException("addUnit 不合法的内容:" + var);
				}
			}
		}
		addUnit(unit);
	}

	private void addUnit(LogicalVariableItem unit) {
		if (0 == this.operators && null == this.unit1) {
			this.unit1 = unit;
		} else if (this.operators > 0 && null == this.unit2) {
			this.unit2 = unit;
		} else {
			throw new RuntimeException("addUnit error");
		}
	}

	public void addOperators(String var) {
		if (null == this.unit1 || this.operators > 0) {
			throw new OgnlException("不合理的表达式操作符:" + var);
		}
		if ("==".equals(var)) {
			addOperators(exprOperators1);
		} else if ("!=".equals(var)) {
			addOperators(exprOperators2);
		} else if (">".equals(var) || "&gt;".equalsIgnoreCase(var)) {
			addOperators(exprOperators3);
		} else if (">=".equals(var) || "&gt;=".equalsIgnoreCase(var)) {
			addOperators(exprOperators4);
		} else if ("<".equals(var) || "&lt;".equalsIgnoreCase(var)) {
			addOperators(exprOperators5);
		} else if ("<=".equals(var) || "&lt;=".equalsIgnoreCase(var)) {
			addOperators(exprOperators6);
		} else if ("and".equalsIgnoreCase(var)) {
			addOperators(exprOperators1);
		} else if ("or".equalsIgnoreCase(var)) {
			addOperators(exprOperators9);
		} else {
			throw new OgnlException("不合理的表达式操作符:" + var);
		}
	}

	private void addOperators(int operators) {
		this.operators = operators;
	}

	public boolean check() {
		// 有一个没有赋值,就返回false
		if (null == this.unit1 || null == this.unit2 || 0 == this.operators) {
			return false;
		}
		return true;
	}

	public boolean getResult(Object data) {

		Object x = this.unit1.getValue();
		Object y = this.unit2.getValue();
		if (this.unit1.isVariable()) {
			x = ((Variable) x).getValue(data);
		}
		if (this.unit2.isVariable()) {
			y = ((Variable) y).getValue(data);
		}

		// 1. null判断
		// if (null == x && null != y) {
		// return false;
		// }
		// if (null != x && null == y) {
		// return false;
		// }
		// if (null == x && null == y) {
		// return true;
		// }

		// fix 空值判断
		if (null == x || null == y) {
			return objectCompare(x, y);
		}

		// 2. 数值判断
		if ((x instanceof Number) && (y instanceof Number)) {
			return numberCompare((Number) x, (Number) y);
		}

		// 3. 数值判断
		if ((x instanceof String) && (y instanceof String)) {
			return stringCompare((String) x, (String) y);
		}

		// 4. 布尔判断
		if ((x instanceof Boolean) && (y instanceof Boolean)) {
			return booleanCompare((Boolean) x, (Boolean) y);
		}

		// 5. 时间判断
		if ((x instanceof java.util.Date) && (y instanceof java.util.Date)) {
			return dateCompare((java.util.Date) x, (java.util.Date) y);
		}

		// 6. 对象判断
		return objectCompare(x, y);

	}

	/**
	 * 数值比较
	 */
	private boolean numberCompare(Number x, Number y) {
		if (this.operators == exprOperators1) {// ==
			return LogicalExpr.numberEqual(x, y);
		} else if (this.operators == exprOperators2) { // !=
			return LogicalExpr.numberNotEqual(x, y);
		} else if (this.operators == exprOperators3) { // >
			return LogicalExpr.numberMoreThan(x, y);
		} else if (this.operators == exprOperators4) { // >=
			return LogicalExpr.numberMoreThanEqual(x, y);
		} else if (this.operators == exprOperators5) { // <
			return LogicalExpr.numberLessThan(x, y);
		} else if (this.operators == exprOperators6) { // <=
			return LogicalExpr.numberLessThanEqual(x, y);
		}
		throw new OgnlException("Illegal logical expression operator in numeric comparison: " + this.operators);
	}

	/**
	 * 时间比较
	 */
	private boolean dateCompare(java.util.Date x, java.util.Date y) {
		if (this.operators == exprOperators1) {// ==
			return LogicalExpr.dateEqual(x, y);
		} else if (this.operators == exprOperators2) { // !=
			return LogicalExpr.dateNotEqual(x, y);
		} else if (this.operators == exprOperators3) { // >
			return LogicalExpr.dateMoreThan(x, y);
		} else if (this.operators == exprOperators4) { // >=
			return LogicalExpr.dateMoreThanEqual(x, y);
		} else if (this.operators == exprOperators5) { // <
			return LogicalExpr.dateLessThan(x, y);
		} else if (this.operators == exprOperators6) { // <=
			return LogicalExpr.dateLessThanEqual(x, y);
		}
		throw new OgnlException("Illegal logical expression operator in date comparison: " + this.operators);
	}

	/**
	 * 布尔比较
	 */
	private boolean booleanCompare(Boolean x, Boolean y) {
		if (this.operators == exprOperators1) {
			return ((Boolean) x).booleanValue() == ((Boolean) y).booleanValue();
		} else if (this.operators == exprOperators2) {
			return ((Boolean) x).booleanValue() != ((Boolean) y).booleanValue();
		}
		throw new OgnlException("Illegal logical expression operator in boolean comparison: " + this.operators);
	}

	/**
	 * 字符串比较
	 */
	private boolean stringCompare(String x, String y) {
		if (this.operators == exprOperators1) {
			return x.equals(y);
		} else if (this.operators == exprOperators2) {
			return !x.equals(y);
		}
		throw new OgnlException("Illegal logical expression operator in object comparison: " + this.operators);
	}

	/**
	 * 对象比较
	 */
	private boolean objectCompare(Object x, Object y) {
		if (this.operators == exprOperators1) {
			return x == y;
		} else if (this.operators == exprOperators2) {
			return x != y;
		}
		throw new OgnlException("Illegal logical expression operator in object comparison: " + this.operators);
	}

}
