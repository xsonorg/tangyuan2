package org.xson.tangyuan.ognl.vars.parser;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.vo.OperaExprVariable;
import org.xson.tangyuan.ognl.vars.vo.TreeNode;

/**
 * 运算达式解析器
 */
public class OperaExprParser extends AbstractParser {

	public final static int		TEXTTYPE_VAL				= 1;	// 值
	// private final static int TEXTTYPE_SYMBOL = 2; // 运算符
	public final static int		TEXTTYPE_SYMBOL_PLUS		= 2;	// +
	public final static int		TEXTTYPE_SYMBOL_MINUS		= 3;	// -
	public final static int		TEXTTYPE_SYMBOL_MULTIPLY	= 4;	// *
	public final static int		TEXTTYPE_SYMBOL_DIVISION	= 5;	// /
	public final static int		TEXTTYPE_SYMBOL_REMAINDER	= 6;	// %

	private final static int	SYMBOL_RIORITY_ZERO			= 0;	// 运算符优先级:0
	private final static int	SYMBOL_RIORITY_ONE			= 1;	// 运算符优先级:1[+, -]
	private final static int	SYMBOL_RIORITY_TWO			= 2;	// 运算符优先级:2[*, /, %]

	private Object getVal(String text) {
		text = text.trim();
		if ("null".equalsIgnoreCase(text) || "true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
			throw new OgnlException("Unsupported operation expression: " + text);
		}
		// 仅仅支持:数值类型, 字符串类型
		if (isStaticString(text)) { // 字符串
			return text.substring(1, text.length() - 1);
		} else if (isNumber(text)) { // 数值
			return getNumber(text);
		} else {// 变量
			return new NormalParser().parse(text);
		}
	}

	public boolean check(String text) {
		int length = text.length();
		boolean isString = false;
		for (int i = 0; i < length; i++) {
			char key = text.charAt(i);
			switch (key) {
			case '\'':
				isString = !isString;
				break;
			case '+':
			case '-':
			case '*':
			case '/':
			case '%':
				if (!isString) {
					return true;
				}
			}
		}
		return false;
	}

	private TreeNode build(Object text, int type, int priority, TreeNode tree) {
		if (null == tree) {
			return new TreeNode(text, type, priority, null);
		}

		if (type == TEXTTYPE_VAL) {
			if (null == tree.left) {
				tree.left = new TreeNode(text, type, priority, tree);
				return tree;
			} else if (null == tree.right) {
				tree.right = new TreeNode(text, type, priority, tree);
				return tree;
			} else {
				throw new OgnlException("表达式不合法!");
			}
			// } else if (type == TEXTTYPE_SYMBOL) {
		} else {
			if (tree.type == TEXTTYPE_VAL) {// 之前为值节点, 当前为符号节点
				TreeNode symbol = new TreeNode(text, type, priority, null);
				symbol.left = tree;
				return symbol;
				// } else if (tree.type == TEXTTYPE_SYMBOL) {// 之前为符号节点, 当前为符号节点
			} else {// 之前为符号节点, 当前为符号节点
				if (null == tree.left || null == tree.right) {
					throw new OgnlException("表达式不合法!");
				}
				if (tree.priority >= priority) {
					// a * b + c == a + b + c
					TreeNode symbol = new TreeNode(text, type, priority, null);
					// 这里要递归向上找根节点, 并修正付父节点
					TreeNode oldRoot = findRoot(tree);
					oldRoot.parent = symbol;
					// 设置新的节点关系
					symbol.left = oldRoot;
					return symbol;
				} else {// <
					// a + b * c
					TreeNode symbol = new TreeNode(text, type, priority, tree);
					// 修正之前右子树的父节点
					TreeNode oldRight = tree.right;
					oldRight.parent = symbol;
					// 设置新的节点关系
					symbol.left = oldRight;
					tree.right = symbol;
					return symbol;
				}
			}
		}
	}

	/**
	 * 查找根节点
	 */
	private TreeNode findRoot(TreeNode tree) {
		TreeNode q = tree;
		while (true) {
			if (null != q.parent) {
				q = q.parent;
			} else {
				return q;
			}
		}
	}

	public Variable parse(String text) {
		// 括弧(), 代表增加运算符的优先级
		int length = text.length();
		boolean isString = false;
		int multiple = 1;// 倍数
		TreeNode tree = null;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char key = text.charAt(i);
			switch (key) {
			// case ' ':
			// if (isString) {
			// builder.append(key);
			// } else if (builder.length() > 0) {
			// tree = build(getVal(builder.toString()), TEXTTYPE_VAL, SYMBOL_RIORITY_ZERO, tree);
			// builder = new StringBuilder();
			// }
			// break;
			case '\'':
				isString = !isString;
				builder.append(key);
				break;
			case '+':
				if (isString) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						tree = build(getVal(builder.toString()), TEXTTYPE_VAL, SYMBOL_RIORITY_ZERO, tree);
						builder = new StringBuilder();
					}
					tree = build(key, TEXTTYPE_SYMBOL_PLUS, SYMBOL_RIORITY_ONE * multiple, tree);
				}
				break;
			case '-':
				if (isString) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						tree = build(getVal(builder.toString()), TEXTTYPE_VAL, SYMBOL_RIORITY_ZERO, tree);
						builder = new StringBuilder();
					}
					tree = build(key, TEXTTYPE_SYMBOL_MINUS, SYMBOL_RIORITY_ONE * multiple, tree);
				}
				break;
			case '*':
				if (isString) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						tree = build(getVal(builder.toString()), TEXTTYPE_VAL, SYMBOL_RIORITY_ZERO, tree);
						builder = new StringBuilder();
					}
					tree = build(key, TEXTTYPE_SYMBOL_MULTIPLY, SYMBOL_RIORITY_TWO * multiple, tree);
				}
				break;
			case '/':
				if (isString) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						tree = build(getVal(builder.toString()), TEXTTYPE_VAL, SYMBOL_RIORITY_ZERO, tree);
						builder = new StringBuilder();
					}
					tree = build(key, TEXTTYPE_SYMBOL_DIVISION, SYMBOL_RIORITY_TWO * multiple, tree);
				}
				break;
			case '%':
				if (isString) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						tree = build(getVal(builder.toString()), TEXTTYPE_VAL, SYMBOL_RIORITY_ZERO, tree);
						builder = new StringBuilder();
					}
					tree = build(key, TEXTTYPE_SYMBOL_REMAINDER, SYMBOL_RIORITY_TWO * multiple, tree);
				}
				break;
			case '(':
				if (isString) {
					builder.append(key);
				} else {
					multiple = multiple * 2;
				}
				break;
			case ')':
				if (isString) {
					builder.append(key);
				} else {
					multiple = multiple / 2;
				}
				break;
			default:
				builder.append(key);
			}
		}

		if (multiple != 1) {
			throw new OgnlException("The operation expression is structurally incorrect: " + text);
		}

		if (builder.length() > 0) {
			tree = build(getVal(builder.toString()), TEXTTYPE_VAL, SYMBOL_RIORITY_ZERO, tree);
		}

		tree = findRoot(tree);

		// return tree;

		return new OperaExprVariable(text, tree);
	}

	/**
	 * 中序遍历
	 */
	protected void ldr(TreeNode tree) {
		if (tree == null) {
			return;
		} else {
			ldr(tree.left);
			System.out.println(tree.data);
			ldr(tree.right);
		}
	}

	protected void ldr(TreeNode tree, int level) {
		if (tree == null) {
			return;
		} else {
			ldr(tree.left, level + 1);
			System.out.println("[" + level + "]" + tree.data);
			ldr(tree.right, level + 1);
		}
	}

	public static void main(String[] args) {
		// 括弧的作用就是提升"符号"的优先级
		// String text = "(2 + 4) * 5";
		// String text = "a + b + c";
		// String text = "a + b * c + d";
		// String text = "1 * 2 * 3 + 4 * 2";
		// String text = "1 + 2";
		// ExpressionParser app = new ExpressionParser();
		// TreeNode tree = app.parse(text);
		// System.out.println(tree);
		// System.out.println(1 * 2 * 3 + 4);
		// System.out.println(tree.getValue(null));
		// app.ldr(tree, 1);
	}
}
