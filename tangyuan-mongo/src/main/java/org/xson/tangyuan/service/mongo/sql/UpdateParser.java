package org.xson.tangyuan.service.mongo.sql;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.service.mongo.sql.UpdateVo.ColumnUpdateType;
import org.xson.tangyuan.service.mongo.sql.ValueVo.ValueType;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.XmlTextParseUtil;

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
		// String[] array = safeSplit(sql.substring(startPos, endPos), ','); // fix bug
		// String[] array = splitSets(sql.substring(startPos, endPos), ',');

		// a=1, b=[a: 1, b: 3], d=128, e={a: 18}
		List<String> array = splitValues(sql.substring(startPos, endPos), ',');

		List<ColumnUpdateVo> setColumns = new ArrayList<ColumnUpdateVo>();
		for (int i = 0, n = array.size(); i < n; i++) {
			// String[] item = array[i].split("=");// 可能有单引号 fix bug
			// String[] item = safeSplit(array.get(i), '=');

			String[] item = splitSetItem(array.get(i), '=');

			if (null == item || item.length != 2) {
				throw new SqlParseException("Illegal update set: " + sql);
			}

			// ***遵循约定, 变量在左边***
			ColumnUpdateVo columnUpdateVo = new ColumnUpdateVo();
			columnUpdateVo.setName(StringUtils.trimEmpty(item[0]));

			item[1] = StringUtils.trimEmpty(item[1]);

			// fix bug 先判断是基本情况（直接赋值），解决+-问题
			ValueVo itemValue = parseValueVo(item[1]);

			if (ValueType.UNKNOWN != itemValue.getType()) {
				columnUpdateVo.setType(ColumnUpdateType.NORMAL);
				columnUpdateVo.setValueVo(itemValue);
				setColumns.add(columnUpdateVo);
				continue;
			}

			int pos = -1;

			// *
			pos = XmlTextParseUtil.findMatchedChar(item[1], '*');// 新增乘法
			if (pos > -1) {
				columnUpdateVo.setType(ColumnUpdateType.MUL);
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

			// +
			pos = XmlTextParseUtil.findMatchedChar(item[1], '+');
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

			// -, 暂不支持 a=-a ==> a * -1
			pos = XmlTextParseUtil.findMatchedChar(item[1], '-');// fix bug
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
			columnUpdateVo.setValueVo(itemValue);
			setColumns.add(columnUpdateVo);
		}
		updateVo.setSetColumns(setColumns);
	}

	/** 分隔每一个set */
	protected String[] splitSetItem(String src, char separator) {
		int splitPos = -1;
		boolean isString = false;
		for (int i = 0; i < src.length(); i++) {
			char key = src.charAt(i);
			switch (key) {
			case '\'':
				isString = !isString;
				break;
			default:
				if (separator == key && !isString) {
					splitPos = i;
				}
				break;
			}
			if (splitPos > -1) {
				break;
			}
		}
		if (-1 == splitPos) {
			return null;
		}

		String[] array = new String[2];
		array[0] = src.substring(0, splitPos);
		array[1] = src.substring(splitPos + 1);
		return array;
	}

}
