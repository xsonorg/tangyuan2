package org.xson.tangyuan.service.mongo.sql;

import java.util.ArrayList;
import java.util.List;

public class App2 {

	//	public List<List<ValueVo>> parseInsertValueList(String sql, String ucSql, int leftBracketsPos) {
	//		
	//		return null;
	//	}

	protected int findCharIndex(String src, int start, char chr) {
		return findCharIndex(src, start, src.length(), chr);
	}

	protected int findCharIndex(String src, int start, int end, char chr) {
		boolean isString = false; // 是否进入字符串采集
		for (int i = start; i < end; i++) {
			char key = src.charAt(i);
			switch (key) {
			case '\'':
				isString = !isString;
				break;
			default:
				if (chr == key && !isString) {
					return i;
				}
				break;
			}
		}
		return -1;
	}

	protected List<String> splitValues(String src, char separator) {
		List<String>  temp     = new ArrayList<String>();
		StringBuilder sb       = new StringBuilder();
		boolean       isString = false; // 是否进入字符串采集
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
					int end = findNestedCharIndex(src, i + 1, src.length(), '[', ']');
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
					int end = findNestedCharIndex(src, i + 1, src.length(), '{', '}');
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

		//		String[] result = new String[temp.size()];
		//		return temp.toArray(result);

		return temp;
	}

	public void parseInsertValueList(String sql, String ucSql, int start) {
		int end = findNestedCharIndex(sql, start + 1, sql.length(), '(', ')');
		if (-1 == end) {
			throw new SqlParseException("Illegal insert: " + sql);
		}
		System.out.println(sql.substring(start + 1, end));

		List<String> values = splitValues(sql.substring(start + 1, end), ',');

		//		for (int i = 0; i < values.length; i++) {
		//			System.out.println(values[i]);
		//		}
		for (String s : values) {
			System.out.println(s);
		}

		start = findCharIndex(ucSql, end, '(');
		if (start > -1) {
			parseInsertValueList(sql, ucSql, start);
		}
	}

	/**
	 * 
	 * @param src
	 * @param start 包含收个特征的位置
	 * @param end
	 * @param startChr
	 * @param endChr
	 * @return
	 */
	protected int findNestedCharIndex(String src, int start, int end, char startChr, char endChr) {
		boolean isString    = false; // 是否进入字符串采集
		int     nestedCount = 0;
		for (int i = start; i < end; i++) {
			char key = src.charAt(i);
			switch (key) {
			case '\'':
				isString = !isString;
				break;
			default:
				if (isString) {
					break;
				}
				if (startChr == key) {
					nestedCount++;
					break;
				}
				if (endChr == key && 0 == nestedCount) {
					return i;
				}
				if (endChr == key && 0 != nestedCount) {
					nestedCount--;
				}
				break;
			}
		}
		return -1;
	}
	
	// splitSets

	public static void main(String[] args) {
		//		String sql   = "insert into col_obj(id, obj, arr) values( 2, {\"name\":\"张三\",\"age\":18} , [2  ,4, 7 ]  );";
		String sql   = "INSERT INTO table ( \"clo1\", \"col2\", \"col3\", \"col4\", \"col5\" ) VALUES ( 1, 10, NULL, '2019-12-19 13:38:35', '新年活动16张卡券'),( 2, 11, NULL, '2019-12-19 15:05:13', '圣诞活动11张卡券'), ( 3, 12, NULL, '2019-12-19 15:05:13', '圣诞活动12张卡券'),( 4, 13, NULL, '2019-12-19 15:05:13', '圣诞活动13张卡券');";

		String ucSql = sql.toUpperCase();
		int    start = ucSql.indexOf("VALUES");
		start = ucSql.indexOf("(", start);

		//		System.out.println(sql.substring(start));

		App2 x = new App2();
		x.parseInsertValueList(sql, sql.toUpperCase(), start);

	}
}
