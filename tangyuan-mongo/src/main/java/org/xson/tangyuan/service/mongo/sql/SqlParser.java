package org.xson.tangyuan.service.mongo.sql;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.warper.GAParserWarper;
import org.xson.tangyuan.service.mongo.sql.ValueVo.ValueType;
import org.xson.tangyuan.service.mongo.sql.condition.BracketsCondition;
import org.xson.tangyuan.service.mongo.sql.condition.EqualCondition;
import org.xson.tangyuan.service.mongo.sql.condition.GreaterEqualCondition;
import org.xson.tangyuan.service.mongo.sql.condition.InCondition;
import org.xson.tangyuan.service.mongo.sql.condition.LessThanCondition;
import org.xson.tangyuan.service.mongo.sql.condition.LessThanEqualCondition;
import org.xson.tangyuan.service.mongo.sql.condition.LikeCondition;
import org.xson.tangyuan.service.mongo.sql.condition.MoreThanCondition;
import org.xson.tangyuan.service.mongo.sql.condition.NotEqualCondition;
import org.xson.tangyuan.service.mongo.sql.condition.NotInCondition;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.XmlTextParseUtil;

public class SqlParser {

	public final static String	BLANK_MARK		= " ";
	public final static String	SELECT_MARK		= "SELECT";
	public final static String	INSERT_MARK		= "INSERT INTO";
	public final static String	UPDATE_MARK		= "UPDATE";
	public final static String	DELETE_MARK		= "DELETE FROM";

	public final static String	FROM_MARK		= "FROM";
	public final static String	WHERE_MARK		= "WHERE";
	public final static String	ORDER_BY_MARK	= "ORDER BY";
	public final static String	LIMIT_MARK		= "LIMIT";

	public SqlVo parse(String sql) throws SqlParseException {

		sql = sql.trim();
		if (sql.endsWith(";")) {// fix bug
			sql = sql.substring(0, sql.length() - 1);
		}
		String ucSql = sql.toUpperCase();
		if (ucSql.startsWith(SELECT_MARK)) {
			return new SelectParser().parse(sql, ucSql);
		} else if (ucSql.startsWith(INSERT_MARK)) {
			return new InsertParser().parse(sql, ucSql);
		} else if (ucSql.startsWith(UPDATE_MARK)) {
			return new UpdateParser().parse(sql, ucSql);
		} else if (ucSql.startsWith(DELETE_MARK)) {
			return new DeleteParser().parse(sql, ucSql);
		}
		return null;
	}

	protected WhereCondition parseSelectWhere(String sql, int wherePos, int endPos) {
		// 1. a=b EqualCondition
		// 2. a>b MoreThanCondition
		// 3. a<b LessThanCondition
		// 4. a>=b GreaterEqualCondition
		// 5. a<=b LessThanEqualCondition
		// x. a<>b NotEqualCondition
		// 6. a like b LikeCondition
		// x. a in (1, 2, 3) InCondition
		// x. a not in (1, 2, 3) NotInCondition

		StringBuilder builder = new StringBuilder();
		String leftKey = null;

		LinkedList<BracketsCondition> stack = new LinkedList<BracketsCondition>();
		BracketsCondition bracketsCondition = new BracketsCondition(false);
		boolean isString = false; // 是否进入字符串采集
		for (int i = wherePos; i < endPos; i++) {
			char key = sql.charAt(i);
			switch (key) {
			case '(':
				if (isString) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						throw new SqlParseException("Illegal where [(]: " + sql);
					}
					stack.push(bracketsCondition);
					BracketsCondition newBracketsCondition = new BracketsCondition(true);
					bracketsCondition.addCondition(newBracketsCondition);
					bracketsCondition = newBracketsCondition;
				}
				break;
			case ')':
				// if (builder.length() > 0) {
				// bracketsCondition.setValue(parseValueVo(builder.toString().trim(), false));
				// leftKey = null;
				// builder = new StringBuilder();
				// }
				// bracketsCondition = stack.pop();
				// break;

				// fix bug
				if (isString) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						bracketsCondition.setValue(parseValueVo(builder.toString().trim(), false));
						leftKey = null;
						builder = new StringBuilder();
					}
					bracketsCondition = stack.pop();
				}
				break;
			case '=':
				if (isString) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						leftKey = builder.toString();
						builder = new StringBuilder();
					}
					EqualCondition condition = new EqualCondition();
					condition.setName(leftKey);
					bracketsCondition.addCondition(condition);
				}
				break;
			case '!':
				if (isString) {
					builder.append(key);
					break;
				}
				if (((i + 1) < endPos) && ('=' == sql.charAt(i + 1))) {
					if (builder.length() > 0) {
						leftKey = builder.toString();
						builder = new StringBuilder();
					}
					NotEqualCondition condition = new NotEqualCondition();
					condition.setName(leftKey);
					bracketsCondition.addCondition(condition);
					i++;
					break;
				}
			case '>':
				if (isString) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						leftKey = builder.toString();
						builder = new StringBuilder();
					}
					if ((i + 1) < endPos && '=' == sql.charAt(i + 1)) {
						GreaterEqualCondition condition = new GreaterEqualCondition();
						condition.setName(leftKey);
						bracketsCondition.addCondition(condition);
						i++;
					} else {
						MoreThanCondition condition = new MoreThanCondition();
						condition.setName(leftKey);
						bracketsCondition.addCondition(condition);
					}
				}
				break;
			case '<':
				if (isString) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						leftKey = builder.toString();
						builder = new StringBuilder();
					}
					if ((i + 1) < endPos && '=' == sql.charAt(i + 1)) {
						LessThanEqualCondition condition = new LessThanEqualCondition();
						condition.setName(leftKey);
						bracketsCondition.addCondition(condition);
						i++;
					} else if ((i + 1) < endPos && '>' == sql.charAt(i + 1)) {
						NotEqualCondition condition = new NotEqualCondition();
						condition.setName(leftKey);
						bracketsCondition.addCondition(condition);
						i++;
					} else {
						LessThanCondition condition = new LessThanCondition();
						condition.setName(leftKey);
						bracketsCondition.addCondition(condition);
					}
				}
				break;
			case '\r':
			case '\n':
			case '\t':
			case ' ':
				if (isString) {
					builder.append(key);
				} else {
					if (builder.length() > 0) {
						String str = builder.toString();
						builder = new StringBuilder();
						if ("LIKE".equalsIgnoreCase(str)) {
							LikeCondition condition = new LikeCondition();
							condition.setName(leftKey);
							bracketsCondition.addCondition(condition);
						} else if ("IN".equalsIgnoreCase(str)) {
							InCondition condition = new InCondition();
							condition.setName(leftKey);
							bracketsCondition.addCondition(condition);
							i = findIn(bracketsCondition, sql, i, endPos);
						} else if ("NOT".equalsIgnoreCase(str)) {
							NotInCondition condition = new NotInCondition();
							condition.setName(leftKey);
							bracketsCondition.addCondition(condition);
							i = findNotIn(bracketsCondition, sql, i, endPos);
						} else if ("AND".equalsIgnoreCase(str)) {
							bracketsCondition.setAndOr(true);
							leftKey = null;
						} else if ("OR".equalsIgnoreCase(str)) {
							bracketsCondition.setAndOr(false);
							leftKey = null;
						} else {// left or rigth
							if (null == leftKey) {
								leftKey = str.trim();
							} else {
								bracketsCondition.setValue(parseValueVo(str.trim(), false));
								leftKey = null;
							}
						}
					}
				}
				break;
			case '\'':
				if (isString) {
					String str = builder.toString();
					builder = new StringBuilder();
					bracketsCondition.setValue(parseValueVo(str.trim(), true));
					leftKey = null;
					isString = false;
				} else {
					isString = true;
				}
				break;
			case '@':// 新增，支持二次解析和调用
				if (isString) {
					builder.append(key);
					break;
				}
				int specialEndPos = findSpecialEndPos(sql, i);
				if (specialEndPos > -1) {
					builder.append(sql.substring(i, specialEndPos + 1));
					i = specialEndPos;
					break;
				}
				builder.append(key);
				break;
			default:
				builder.append(key);
			}
		}

		if (builder.length() > 0) {
			String str = builder.toString();
			bracketsCondition.setValue(parseValueVo(str.trim(), false));
		}

		// TODO: 需要判断bracketsCondition是否合法

		return bracketsCondition;
	}

	/**
	 * 查找@{xxxx}
	 */
	protected int findSpecialEndPos(String src, int start) {
		int leftStart = src.indexOf("{", start);
		if (leftStart == -1) {
			return -1;
		}
		int end = XmlTextParseUtil.findNestedMatchedChar(src, leftStart, '{', '}');
		if (end == -1) {
			return -1;
		}
		return end;
	}

	/**
	 * support x in (...) [....] @{...}
	 */
	protected int findIn(BracketsCondition condition, String sql, int startPos, int endPos) {
		int endBracketsPos = -1;
		endBracketsPos = findIn1(condition, sql, startPos, endPos);
		if (-1 == endBracketsPos) {
			endBracketsPos = findIn2(condition, sql, startPos, endPos);
		}
		if (-1 == endBracketsPos) {
			endBracketsPos = findIn3(condition, sql, startPos, endPos);
		}
		if (-1 == endBracketsPos) {
			throw new SqlParseException("Illegal where in: " + sql);
		}
		return endBracketsPos + 1;
	}

	/**
	 * support x in (...)[INTEGER, DOUBLE, STRING]
	 */
	protected int findIn1(BracketsCondition condition, String sql, int startPos, int endPos) {
		int startBracketsPos = sql.indexOf("(", startPos);
		if (-1 == startBracketsPos) {
			return -1;
		}
		int endBracketsPos = XmlTextParseUtil.findNestedMatchedChar(sql, startBracketsPos, '(', ')');
		if (-1 == endBracketsPos) {
			return -1;
		}

		ValueVo value = new ValueVo(null, ValueType.ARRAY, "[" + sql.substring(startBracketsPos + 1, endBracketsPos) + "]");
		condition.setValue(value);
		return endBracketsPos;
	}

	/**
	 * support x in [...]
	 */
	protected int findIn2(BracketsCondition condition, String sql, int startPos, int endPos) {
		int startBracketsPos = sql.indexOf("[", startPos);
		if (-1 == startBracketsPos) {
			return -1;
		}
		int endBracketsPos = XmlTextParseUtil.findNestedMatchedChar(sql, startBracketsPos, '[', ']');
		if (-1 == endBracketsPos) {
			return -1;
		}
		ValueVo value = new ValueVo(null, ValueType.ARRAY, sql.substring(startBracketsPos, endBracketsPos + 1));
		condition.setValue(value);
		return endBracketsPos;
	}

	/**
	 * support x in @{...}
	 */
	protected int findIn3(BracketsCondition condition, String sql, int startPos, int endPos) {
		int startBracketsPos = sql.indexOf("@", startPos);
		if (-1 == startBracketsPos) {
			return -1;
		}
		int endBracketsPos = findSpecialEndPos(sql, startBracketsPos);
		if (-1 == endBracketsPos) {
			return -1;
		}
		String original = sql.substring(startBracketsPos + 2, endBracketsPos).trim();
		Variable variable = new GAParserWarper().parse(original);
		ValueVo value = new ValueVo(variable, ValueType.CALL, original);
		condition.setValue(value);
		return endBracketsPos;
	}

	protected int findNotIn(BracketsCondition condition, String sql, int startPos, int endPos) {
		return findIn(condition, sql, startPos, endPos);
	}

	protected boolean isNumber(String var) {
		return var.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}

	protected boolean isInteger(String var) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(var).matches();
	}

	private boolean isLegalEmptyChar(char x) {
		if (x == ' ' || x == '\t' || x == '\r' || x == '\n' || x == '\f' || x == ' ') {
			return true;
		}
		return false;
	}

	protected int findWhere(String sql, int start) {
		// // public final static String WHERE_MARK = " WHERE ";
		char[] src = sql.toCharArray();
		int srcLength = src.length;
		boolean isString = false; // 是否进入字符串采集
		for (int i = 0; i < srcLength; i++) {
			char key = src[i];
			switch (key) {
			case '\'':
				isString = !isString;
				break;
			case 'W':
				if (!isString) {
					if ((i + WHERE_MARK.length() + 1) < srcLength) {
						String mark = new String(src, i, WHERE_MARK.length());
						if (mark.equals(WHERE_MARK) && isLegalEmptyChar(src[i - 1]) && isLegalEmptyChar(src[i + WHERE_MARK.length()])) {
							return i;
						}
					}
				}
				break;
			default:
				break;
			}
		}
		return -1;
	}

	protected int findFrom(String sql, int start) {
		char[] src = sql.toCharArray();
		int srcLength = src.length;
		boolean isString = false; // 是否进入字符串采集
		for (int i = 0; i < srcLength; i++) {
			char key = src[i];
			switch (key) {
			case '\'':
				isString = !isString;
				break;
			case 'F':
				if (!isString) {
					if ((i + FROM_MARK.length() + 1) < srcLength) {
						String mark = new String(src, i, FROM_MARK.length());
						if (mark.equals(FROM_MARK) && isLegalEmptyChar(src[i - 1]) && isLegalEmptyChar(src[i + FROM_MARK.length()])) {
							return i;
						}
					}
				}
				break;
			default:
				break;
			}
		}
		return -1;
	}

	protected int findLimit(String sql, int start) {
		char[] src = sql.toCharArray();
		int srcLength = src.length;
		boolean isString = false; // 是否进入字符串采集
		for (int i = 0; i < srcLength; i++) {
			char key = src[i];
			switch (key) {
			case '\'':
				isString = !isString;
				break;
			case 'L':
				if (!isString) {
					if ((i + LIMIT_MARK.length() + 1) < srcLength) {
						String mark = new String(src, i, LIMIT_MARK.length());
						if (mark.equals(LIMIT_MARK) && isLegalEmptyChar(src[i - 1]) && isLegalEmptyChar(src[i + LIMIT_MARK.length()])) {
							return i;
						}
					}
				}
				break;
			default:
				break;
			}
		}
		return -1;
	}

	protected int findOrderBy(String sql, int start) {
		// public final static String ORDER_BY_MARK = " ORDER BY ";
		char[] src = sql.toCharArray();
		int srcLength = src.length;
		boolean isString = false; // 是否进入字符串采集
		for (int i = 0; i < srcLength; i++) {
			char key = src[i];
			switch (key) {
			case '\'':
				isString = !isString;
				break;
			case 'O':
				if (!isString) {
					if ((i + ORDER_BY_MARK.length() + 1) < srcLength) {
						String mark = new String(src, i, ORDER_BY_MARK.length());
						if (mark.equals(ORDER_BY_MARK) && isLegalEmptyChar(src[i - 1]) && isLegalEmptyChar(src[i + ORDER_BY_MARK.length()])) {
							return i;
						}
					}
				}
				break;
			default:
				break;
			}
		}
		return -1;
	}

	protected String parseColumnName(String val) {
		val = StringUtils.trimEmpty(val);
		if (null == val) {
			return val;
		}
		if ((val.startsWith("'") && val.endsWith("'")) || (val.startsWith("\"") && val.endsWith("\""))) {
			return val.substring(1, val.length() - 1);
		}
		return val;
	}

	/** values分隔 */
	protected List<String> splitValues(String src, char separator) {
		List<String> temp = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		boolean isString = false; // 是否进入字符串采集
		for (int i = 0; i < src.length(); i++) {
			char key = src.charAt(i);
			switch (key) {
			case '\'':
				isString = !isString;
				sb.append(key);
				break;
			case '[':// 支持递归
				if (isString) {
					sb.append(key);
				} else {
					// int end = findNestedCharIndex(src, i + 1, src.length(), '[', ']');
					int end = XmlTextParseUtil.findNestedMatchedChar(src, i, '[', ']');
					if (-1 == end) {
						throw new SqlParseException("The array is missing an end tag: " + src);
					}
					sb.append(src.substring(i, end + 1));
					i = end;
				}
				break;
			case '{':// 支持递归
				if (isString) {
					sb.append(key);
				} else {
					// int end = findNestedCharIndex(src, i + 1, src.length(), '{', '}');
					int end = XmlTextParseUtil.findNestedMatchedChar(src, i, '{', '}');
					if (-1 == end) {
						throw new SqlParseException("The object is missing an end tag: " + src);
					}
					sb.append(src.substring(i, end + 1));
					i = end;
				}
				break;
			default:
				if (separator == key && !isString) {
					if (sb.length() > 0) {
						temp.add(sb.toString());
						sb = new StringBuilder();
					}
				} else {
					sb.append(key);
				}
				break;
			}
		}

		if (sb.length() > 0) {
			temp.add(sb.toString());
		}
		return temp;
	}

	protected ValueVo parseValueVo(String val) {
		return parseValueVo(val, false);
	}

	protected ValueVo parseValueVo(String val, boolean isString) {
		if (isString) {
			return new ValueVo(val, ValueType.STRING, val);
		}

		if (val.equalsIgnoreCase("null")) {
			return new ValueVo(null, ValueType.NULL, val);
		}

		if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
			return new ValueVo(Boolean.parseBoolean(val), ValueType.BOOLEAN, val);
		}

		if ((val.startsWith("'") && val.endsWith("'")) || (val.startsWith("\"") && val.endsWith("\""))) {
			return new ValueVo(val.substring(1, val.length() - 1), ValueType.STRING, val);
		}

		// add array type
		if (val.startsWith("[") && val.endsWith("]")) {
			return new ValueVo(null, ValueType.ARRAY, val);
		}

		// support function call
		if (val.startsWith("@{") && val.endsWith("}")) {
			String original = val.substring(2, val.length() - 1).trim();
			Variable variable = new GAParserWarper().parse(original);
			return new ValueVo(variable, ValueType.CALL, original);
		}

		// support json object
		if (val.startsWith("{") && val.endsWith("}")) {
			return new ValueVo(null, ValueType.OBJECT, val);
		}

		if (isInteger(val)) {
			// fix bug, support long
			Object number = null;
			try {
				number = Integer.parseInt(val);
				return new ValueVo(number, ValueType.INTEGER, val);
			} catch (NumberFormatException e) {
				number = Long.parseLong(val);
			}
			return new ValueVo(number, ValueType.LONG, val);
		}

		if (isNumber(val)) {
			return new ValueVo(Double.parseDouble(val), ValueType.DOUBLE, val);
		}

		return new ValueVo(val, ValueType.UNKNOWN, val);
	}

	// public static void main(String[] args) {
	// // String sql = "select count(*) from table";
	// // String sql = "select a, b from table where a>2 or (c = '4' and c = 1) order by a b,c DESC limit 1,2";
	// String sql = "select * from table where ((c= '4' or c =1) or (c = '4' and c = 1)) and (c = '4' and ( c = '4' and c = 1)) order by a b asc,c
	// limit 1";
	// // String sql = "INSERT INTO tbTrade (name, age) VALUES ('gaop34', 22)";
	// // String sql = "DELETE FROM 表名称 WHERE age = 22";
	// // String sql = " UPDATE Person SET FirstName = FirstName +4, WHERE LastName = 'Wilson'";
	// SqlParser parser = new SqlParser();
	// SqlVo sqlVo = parser.parse(sql);
	// System.out.println(sqlVo.toSQL());
	// }

}
