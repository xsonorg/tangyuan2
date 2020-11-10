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
		int      length   = sql.length();
		int      setPos   = ucSql.indexOf(SET, UPDATE_MARK.length());
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
		// String[]             array      = splitSets(sql.substring(startPos, endPos), ',');

		//  a=1, b=[a: 1, b: 3], d=128, e={a: 18} 
		List<String>         array      = splitValues(sql.substring(startPos, endPos), ',');

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
		int     splitPos = -1;
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
	//		if (isInteger(val)) {
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
	//		if (isNumber(val)) {
	//			return new ValueVo(Double.parseDouble(val), ValueType.DOUBLE, val);
	//		}
	//
	//		return new ValueVo(val, ValueType.UNKNOWN, val);
	//	}

	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	

	//	/** values分隔 */
	//	protected String[] splitSets(String src, char separator) {
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
