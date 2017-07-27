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
		array = safeSplit(sql.substring(leftBracketsPos + 1, rightBracketsPos), ',');
		List<ValueVo> values = new ArrayList<ValueVo>();
		for (int i = 0, n = array.length; i < n; i++) {
			values.add(parseValueVo(array[i].trim()));
		}
		insertVo.setValues(values);
		insertVo.check();
		return insertVo;
	}

}
