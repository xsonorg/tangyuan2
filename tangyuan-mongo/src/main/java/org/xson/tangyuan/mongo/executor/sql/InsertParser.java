package org.xson.tangyuan.mongo.executor.sql;

import java.util.ArrayList;
import java.util.List;

public class InsertParser extends SqlParser {

	// INSERT INTO table1(x) VALUES(x);
	// INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)
	public InsertVo parse(String sql, String ucSql) {
		InsertVo insertVo = new InsertVo();
		// int length = sql.length();
		// 1. 采集属性

		int leftBracketsPos = ucSql.indexOf("(", SqlParser.INSERT_MARK.length());
		if (-1 == leftBracketsPos) {
			throw new SqlParseException("Illegal insert: " + sql);
		}
		int rightBracketsPos = ucSql.indexOf(")", leftBracketsPos);
		if (-1 == rightBracketsPos) {
			throw new SqlParseException("Illegal insert: " + sql);
		}
		String table = sql.substring(SqlParser.INSERT_MARK.length(), leftBracketsPos).trim();
		insertVo.setTable(table);

		List<String> columns = new ArrayList<String>();
		String[] array = sql.substring(leftBracketsPos + 1, rightBracketsPos).split(",");
		for (int i = 0, n = array.length; i < n; i++) {
			columns.add(array[i].trim());
		}
		insertVo.setColumns(columns);

		int valuesPos = ucSql.indexOf("VALUES", rightBracketsPos);
		if (-1 == valuesPos) {
			throw new SqlParseException("Illegal insert: " + sql);
		}
		leftBracketsPos = ucSql.indexOf("(", valuesPos + "VALUES".length());
		if (-1 == leftBracketsPos) {
			throw new SqlParseException("Illegal insert: " + sql);
		}
		// rightBracketsPos = ucSql.indexOf(")", leftBracketsPos);
		// fix buf
		rightBracketsPos = findCharIndex(ucSql, leftBracketsPos, ')');
		if (-1 == rightBracketsPos) {
			throw new SqlParseException("Illegal insert: " + sql);
		}

		// array = sql.substring(leftBracketsPos + 1, rightBracketsPos).split(",");
		// fix bug
		// array = safeSplit(sql.substring(leftBracketsPos + 1, rightBracketsPos), ',');
		// support array
		array = splitValues(sql.substring(leftBracketsPos + 1, rightBracketsPos), ',');
		List<ValueVo> values = new ArrayList<ValueVo>();
		for (int i = 0, n = array.length; i < n; i++) {
			values.add(parseValueVo(array[i].trim()));
		}
		insertVo.setValues(values);
		insertVo.check();
		return insertVo;
	}

	/** values分隔 */
	protected String[] splitValues(String src, char separator) {
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
			case '[':// 不支持递归,仅支持一维数组
				if (isString) {
					sb.append(key);
				} else {
					int end = findCharIndex(src, i, ']');
					if (-1 == end) {
						throw new SqlParseException("The array is missing an end tag: " + src);
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

		String[] result = new String[temp.size()];
		return temp.toArray(result);
	}

}
