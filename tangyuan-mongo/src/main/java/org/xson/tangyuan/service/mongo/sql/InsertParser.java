package org.xson.tangyuan.service.mongo.sql;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.XmlTextParseUtil;

public class InsertParser extends SqlParser {

	private final static String VALUES_MARK = "VALUES";

	// INSERT INTO table1(x) VALUES(x);
	// INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)
	public InsertVo parse(String sql, String ucSql) {
		InsertVo insertVo        = new InsertVo();
		int      leftBracketsPos = ucSql.indexOf("(", SqlParser.INSERT_MARK.length());
		if (-1 == leftBracketsPos) {
			throw new SqlParseException("Illegal insert: " + sql);
		}
		int rightBracketsPos = ucSql.indexOf(")", leftBracketsPos);
		if (-1 == rightBracketsPos) {
			throw new SqlParseException("Illegal insert: " + sql);
		}

		// parse table
		String table = sql.substring(SqlParser.INSERT_MARK.length(), leftBracketsPos).trim();
		insertVo.setTable(table);

		// parse columns
		List<String> columns = new ArrayList<String>();
		String[]     array   = sql.substring(leftBracketsPos + 1, rightBracketsPos).split(",");
		for (int i = 0, n = array.length; i < n; i++) {
			//			columns.add(array[i].trim());
			columns.add(parseColumnName(array[i]));
		}
		insertVo.setColumns(columns);

		// parse values
		int valuesPos = ucSql.indexOf(VALUES_MARK, rightBracketsPos);
		if (-1 == valuesPos) {
			throw new SqlParseException("Illegal insert: " + sql);
		}
		leftBracketsPos = ucSql.indexOf("(", valuesPos + VALUES_MARK.length());
		if (-1 == leftBracketsPos) {
			throw new SqlParseException("Illegal insert: " + sql);
		}

		List<List<ValueVo>> valuesGroup = new ArrayList<List<ValueVo>>();
		parseInsertValueList(valuesGroup, sql, ucSql, leftBracketsPos);

		int groupSize = valuesGroup.size();
		if (0 == groupSize) {
			throw new SqlParseException("Illegal insert: " + sql);
		}
		if (1 == groupSize) {
			insertVo.setValues(valuesGroup.get(0));
		} else {
			insertVo.setValuesGroup(valuesGroup);
		}
		insertVo.check();
		return insertVo;
	}

	public void parseInsertValueList(List<List<ValueVo>> valuesGroup, String sql, String ucSql, int start) {
		//		int end = findNestedCharIndex(sql, start + 1, sql.length(), '(', ')');
		int end = XmlTextParseUtil.findNestedMatchedChar(sql, start, '(', ')');
		if (-1 == end) {
			throw new SqlParseException("Illegal insert: " + sql);
		}

		List<String> valueList = splitValues(sql.substring(start + 1, end), ',');

		if (!CollectionUtils.isEmpty(valueList)) {
			List<ValueVo> valueVoList = new ArrayList<ValueVo>();
			for (String value : valueList) {
				valueVoList.add(parseValueVo(StringUtils.trimEmpty(value)));
			}
			valuesGroup.add(valueVoList);
		}

		start = XmlTextParseUtil.findMatchedChar(ucSql, end, '(');
		if (start > -1) {
			parseInsertValueList(valuesGroup, sql, ucSql, start);
		}
	}

	//	/** values分隔 */
	//	protected List<String> splitValues(String src, char separator) {
	//		List<String>  temp     = new ArrayList<String>();
	//		StringBuilder sb       = new StringBuilder();
	//		boolean       isString = false; // 是否进入字符串采集
	//		for (int i = 0; i < src.length(); i++) {
	//			char key = src.charAt(i);
	//			switch (key) {
	//			case '\'':
	//				isString = !isString;
	//				sb.append(key);
	//				break;
	//			case '[':// 支持递归
	//				if (isString) {
	//					sb.append(key);
	//				} else {
	//					int end = findNestedCharIndex(src, i + 1, src.length(), '[', ']');
	//					if (-1 == end) {
	//						throw new SqlParseException("The array is missing an end tag: " + src);
	//					}
	//					sb.append(src.substring(i, end + 1));
	//					i = end;
	//				}
	//				break;
	//			case '{':// 支持递归
	//				if (isString) {
	//					sb.append(key);
	//				} else {
	//					int end = findNestedCharIndex(src, i + 1, src.length(), '{', '}');
	//					if (-1 == end) {
	//						throw new SqlParseException("The object is missing an end tag: " + src);
	//					}
	//					sb.append(src.substring(i, end + 1));
	//					i = end;
	//				}
	//				break;
	//			default:
	//				if (separator == key && !isString) {
	//					if (sb.length() > 0) {
	//						temp.add(sb.toString());
	//						sb = new StringBuilder();
	//					}
	//				} else {
	//					sb.append(key);
	//				}
	//				break;
	//			}
	//		}
	//
	//		if (sb.length() > 0) {
	//			temp.add(sb.toString());
	//		}
	//		return temp;
	//	}
	//
	//	protected int findNestedCharIndex(String src, int start, int end, char startChr, char endChr) {
	//		boolean isString    = false; // 是否进入字符串采集
	//		int     nestedCount = 0;
	//		for (int i = start; i < end; i++) {
	//			char key = src.charAt(i);
	//			switch (key) {
	//			case '\'':
	//				isString = !isString;
	//				break;
	//			default:
	//				if (isString) {
	//					break;
	//				}
	//				if (startChr == key) {
	//					nestedCount++;
	//					break;
	//				}
	//				if (endChr == key && 0 == nestedCount) {
	//					return i;
	//				}
	//				if (endChr == key && 0 != nestedCount) {
	//					nestedCount--;
	//				}
	//				break;
	//			}
	//		}
	//		return -1;
	//	}
	//
	//	protected ValueVo parseValueVo(String val, boolean isString) {
	//		if (isString) {
	//			return new ValueVo(val, ValueType.STRING, val);
	//		}
	//
	//		if (val.equalsIgnoreCase("null")) {
	//			return new ValueVo(null, ValueType.NULL, val);
	//		}
	//
	//		if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
	//			return new ValueVo(Boolean.parseBoolean(val), ValueType.BOOLEAN, val);
	//		}
	//
	//		if ((val.startsWith("'") && val.endsWith("'")) || (val.startsWith("\"") && val.endsWith("\""))) {
	//			return new ValueVo(val.substring(1, val.length() - 1), ValueType.STRING, val);
	//		}
	//
	//		// add array type
	//		if (val.startsWith("[") && val.endsWith("]")) {
	//			//return new ValueVo(parseArrayValue(val.substring(1, val.length() - 1).trim()), ValueType.ARRAY, val);
	//			return new ValueVo(val, ValueType.ARRAY, val);
	//		}
	//
	//		// support function call
	//		if (val.startsWith("@{") && val.endsWith("}")) {
	//			return new ValueVo(parseCall(val.substring(2, val.length() - 1).trim()), ValueType.CALL, val);
	//		}
	//
	//		// support json object
	//		if (val.startsWith("{") && val.endsWith("}")) {
	//			return new ValueVo(val, ValueType.OBJECT, val);
	//		}
	//
	//		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx:" + val.toString());
	//
	//		if (isInteger(val)) {
	//			// return new ValueVo(Integer.parseInt(val), ValueType.INTEGER);
	//			// fix bug, support long
	//			Object number = null;
	//			try {
	//				number = Integer.parseInt(val);
	//				return new ValueVo(number, ValueType.INTEGER, val);
	//			} catch (NumberFormatException e) {
	//				number = Long.parseLong(val);
	//			}
	//			return new ValueVo(number, ValueType.LONG, val);
	//		}
	//
	//		// return new ValueVo(Double.parseDouble(val), ValueType.DOUBLE, val);
	//		if (isNumber(val)) {
	//			return new ValueVo(Double.parseDouble(val), ValueType.DOUBLE, val);
	//		}
	//
	//		// String
	//		//		return new ValueVo(val, ValueType.STRING, val);
	//
	//		// unknown
	//		return new ValueVo(val, ValueType.UNKNOWN, val);
	//	}

	/////////////////////////////////////////////////////////////////

	//	public InsertVo parse(String sql, String ucSql) {
	//		InsertVo insertVo        = new InsertVo();
	//		// int length = sql.length();
	//		// 1. 采集属性
	//
	//		int      leftBracketsPos = ucSql.indexOf("(", SqlParser.INSERT_MARK.length());
	//		if (-1 == leftBracketsPos) {
	//			throw new SqlParseException("Illegal insert: " + sql);
	//		}
	//		int rightBracketsPos = ucSql.indexOf(")", leftBracketsPos);
	//		if (-1 == rightBracketsPos) {
	//			throw new SqlParseException("Illegal insert: " + sql);
	//		}
	//		String table = sql.substring(SqlParser.INSERT_MARK.length(), leftBracketsPos).trim();
	//		insertVo.setTable(table);
	//
	//		List<String> columns = new ArrayList<String>();
	//		String[]     array   = sql.substring(leftBracketsPos + 1, rightBracketsPos).split(",");
	//		for (int i = 0, n = array.length; i < n; i++) {
	//			//			columns.add(array[i].trim());
	//			columns.add(parseColumnName(array[i]));
	//		}
	//		insertVo.setColumns(columns);
	//
	//		int valuesPos = ucSql.indexOf("VALUES", rightBracketsPos);
	//		if (-1 == valuesPos) {
	//			throw new SqlParseException("Illegal insert: " + sql);
	//		}
	//		leftBracketsPos = ucSql.indexOf("(", valuesPos + "VALUES".length());
	//		if (-1 == leftBracketsPos) {
	//			throw new SqlParseException("Illegal insert: " + sql);
	//		}
	//		// rightBracketsPos = ucSql.indexOf(")", leftBracketsPos);
	//		// fix buf
	//		rightBracketsPos = findCharIndex(ucSql, leftBracketsPos, ')');
	//		if (-1 == rightBracketsPos) {
	//			throw new SqlParseException("Illegal insert: " + sql);
	//		}
	//
	//		// array = sql.substring(leftBracketsPos + 1, rightBracketsPos).split(",");
	//		// fix bug
	//		// array = safeSplit(sql.substring(leftBracketsPos + 1, rightBracketsPos), ',');
	//		// support array
	//		array = splitValues(sql.substring(leftBracketsPos + 1, rightBracketsPos), ',');
	//		List<ValueVo> values = new ArrayList<ValueVo>();
	//		for (int i = 0, n = array.length; i < n; i++) {
	//			values.add(parseValueVo(array[i].trim()));
	//		}
	//		insertVo.setValues(values);
	//		insertVo.check();
	//		return insertVo;
	//	}

	//	/** values分隔 */
	//	protected String[] splitValues(String src, char separator) {
	//		List<String>  temp     = new ArrayList<String>();
	//		StringBuilder sb       = new StringBuilder();
	//		boolean       isString = false; // 是否进入字符串采集
	//		for (int i = 0; i < src.length(); i++) {
	//			char key = src.charAt(i);
	//			switch (key) {
	//			case '\'':
	//				isString = !isString;
	//				sb.append(key);
	//				break;
	//			case '[':// 不支持递归,仅支持一维数组
	//				if (isString) {
	//					sb.append(key);
	//				} else {
	//					int end = findCharIndex(src, i, ']');
	//					if (-1 == end) {
	//						throw new SqlParseException("The array is missing an end tag: " + src);
	//					}
	//					sb.append(src.substring(i, end + 1));
	//					i = end;
	//				}
	//				break;
	//			default:
	//				if (separator == key && !isString) {
	//					if (sb.length() > 0) {
	//						temp.add(sb.toString());
	//						sb = new StringBuilder();
	//					}
	//				} else {
	//					sb.append(key);
	//				}
	//				break;
	//			}
	//		}
	//
	//		if (sb.length() > 0) {
	//			temp.add(sb.toString());
	//		}
	//
	//		String[] result = new String[temp.size()];
	//		return temp.toArray(result);
	//	}

}
