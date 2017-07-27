package org.xson.tangyuan.mongo.executor.sql;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.mongo.executor.sql.UpdateVo.ColumnUpdateType;

public class UpdateParser extends SqlParser {

	// UPDATE Person SET FirstName = 'Fred' WHERE LastName = 'Wilson'

	private final static String SET = "SET";

	public UpdateVo parse(String sql, String ucSql) {
		UpdateVo updateVo = new UpdateVo();
		int length = sql.length();
		int setPos = ucSql.indexOf(SET, UPDATE_MARK.length());
		if (-1 == setPos) {
			throw new SqlParseException("Illegal update: " + sql);
		}

		String table = sql.substring(UPDATE_MARK.length(), setPos).trim();
		updateVo.setTable(table);

		// int wherePos = ucSql.indexOf(WHERE_MARK, setPos);
		int wherePos = findWhere(ucSql, setPos);

		if (-1 == wherePos) {
			parseSetColumn(updateVo, sql, setPos + SET.length(), length);
		} else {
			parseSetColumn(updateVo, sql, setPos + SET.length(), wherePos);
			WhereCondition condition = parseSelectWhere(sql, wherePos + WHERE_MARK.length(), length);
			updateVo.setCondition(condition);
		}

		updateVo.check();
		return updateVo;
	}

	private void parseSetColumn(UpdateVo updateVo, String sql, int startPos, int endPos) {
		// SET FirstName = 'Fred', ,
		// String[] array = sql.substring(startPos, endPos).split(","); fix bug
		String[] array = safeSplit(sql.substring(startPos, endPos), ',');

		List<ColumnUpdateVo> setColumns = new ArrayList<ColumnUpdateVo>();
		for (int i = 0, n = array.length; i < n; i++) {
			// String[] item = array[i].split("=");// 可能有单引号 fix bug
			String[] item = safeSplit(array[i], '=');

			if (item.length != 2) {
				throw new SqlParseException("Illegal update set: " + sql);
			}

			ColumnUpdateVo columnUpdateVo = new ColumnUpdateVo();
			columnUpdateVo.setName(item[0].trim());
			// int pos = item[1].indexOf("+");
			int pos = findCharIndex(item[1], '+');// fix bug
			if (pos > -1) {
				columnUpdateVo.setType(ColumnUpdateType.ADD);
				String a = item[1].substring(0, pos).trim();
				String b = item[1].substring(pos + 1, item[1].length()).trim();
				if (columnUpdateVo.getName().equalsIgnoreCase(a)) {
					columnUpdateVo.setValueVo(parseValueVo(b));
				} else if (columnUpdateVo.getName().equalsIgnoreCase(b)) {
					columnUpdateVo.setValueVo(parseValueVo(a));
				} else {
					throw new SqlParseException("Illegal update set: " + sql);
				}
				setColumns.add(columnUpdateVo);
				continue;
			}
			// pos = item[1].indexOf("-");
			pos = findCharIndex(item[1], '-');// fix bug
			if (pos > -1) {
				columnUpdateVo.setType(ColumnUpdateType.MINUS);
				String a = item[1].substring(0, pos).trim();
				String b = item[1].substring(pos + 1, item[1].length()).trim();
				if (columnUpdateVo.getName().equalsIgnoreCase(a)) {
					columnUpdateVo.setValueVo(parseValueVo(b));
				} else if (columnUpdateVo.getName().equalsIgnoreCase(b)) {
					columnUpdateVo.setValueVo(parseValueVo(a));
				} else {
					throw new SqlParseException("Illegal update set: " + sql);
				}
				setColumns.add(columnUpdateVo);
				continue;
			}
			columnUpdateVo.setType(ColumnUpdateType.NORMAL);
			columnUpdateVo.setValueVo(parseValueVo(item[1].trim()));
			setColumns.add(columnUpdateVo);
		}
		updateVo.setSetColumns(setColumns);
	}

	// private int findCharIndex(String src, char chr) {
	// boolean isString = false; // 是否进入字符串采集
	// for (int i = 0; i < src.length(); i++) {
	// char key = src.charAt(i);
	// switch (key) {
	// case '\'':
	// isString = !isString;
	// break;
	// default:
	// if (chr == key && !isString) {
	// return i;
	// }
	// break;
	// }
	// }
	// return -1;
	// }

}
