package org.xson.tangyuan.ognl.vars.vo;

import org.xson.tangyuan.ognl.vars.Variable;

/**
 * 运算表达式变量
 */
public class OperaExprVariable extends Variable {

	private TreeNode tree;

	public OperaExprVariable(String original, TreeNode tree) {
		this.original = original;
		this.tree = tree;
	}

	public Object getValue(Object arg) {
		return tree.getValue(arg);
	}
}
