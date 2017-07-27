package org.xson.tangyuan.mongo.executor.sql;

import java.util.ArrayList;
import java.util.List;

public class SelectParser extends SqlParser {

	public SelectVo parse(String sql, String ucSql) {
		SelectVo selectVo = new SelectVo();
		List<String> columns = new ArrayList<>();
		int length = sql.length();
		int columnStart = SELECT_MARK.length() + 1;
		// 1. 采集属性(select -- > from)
		// int fromPos = ucSql.indexOf(FROM_MARK, columnStart);
		int fromPos = findFrom(ucSql, columnStart);
		if (fromPos > -1) {
			if (ucSql.indexOf("COUNT(", SELECT_MARK.length()) > -1) {
				selectVo.setCount(sql.substring(columnStart, fromPos).trim());
			} else {
				String[] array = sql.substring(columnStart, fromPos).split(",");
				if (1 == array.length && "*".equals(array[0].trim())) {
					selectVo.setAllColumn("*");
				} else {
					for (int i = 0; i < array.length; i++) {
						// TODO 暂不考虑 AS关键字
						columns.add(array[i].trim());
					}
					selectVo.setColumns(columns);
				}
			}
		} else {
			throw new SqlParseException("Illegal select from: " + sql);
		}

		int tablePos = parseTableName(selectVo, sql, fromPos + FROM_MARK.length(), length);
		if (tablePos == length) {
			return selectVo;
		}

		int wherePos = -1;
		int orderByPos = -1;
		int limitPos = -1;

		// wherePos = ucSql.indexOf(WHERE_MARK, tablePos);
		// orderByPos = ucSql.lastIndexOf(ORDER_BY_MARK);
		// limitPos = ucSql.lastIndexOf(LIMIT_MARK);

		int _startPos = tablePos;
		wherePos = findWhere(ucSql, _startPos);
		if (wherePos > _startPos) {
			_startPos = wherePos;
		}
		orderByPos = findOrderBy(ucSql, _startPos);
		if (orderByPos > _startPos) {
			_startPos = orderByPos;
		}
		limitPos = findLimit(ucSql, _startPos);

		// limit解析
		if (limitPos > -1) {
			String[] array = sql.substring(limitPos + LIMIT_MARK.length()).trim().split(",");
			if (1 == array.length) {
				LimitVo limit = new LimitVo(null, Integer.parseInt(array[0].trim()));
				selectVo.setLimit(limit);
			} else if (2 == array.length) {
				LimitVo limit = new LimitVo(Integer.parseInt(array[0].trim()), Integer.parseInt(array[1].trim()));
				selectVo.setLimit(limit);
			} else {
				throw new SqlParseException("Illegal select limit: " + sql);
			}
		}

		// orderBy解析
		if (orderByPos > -1) {
			int endPos = length;
			if (limitPos > -1) {
				endPos = limitPos;
			}
			String[] array = sql.substring(orderByPos + ORDER_BY_MARK.length(), endPos).trim().split(",");
			List<OrderByVo> orderByList = new ArrayList<OrderByVo>();
			for (int i = 0; i < array.length; i++) {
				// String[] item = array[i].trim().split(" ");
				String[] item = array[i].trim().split(BLANK_MARK);
				List<String> columnList = new ArrayList<String>();
				OrderType type = OrderType.ASC;
				if (1 == item.length) {
					columnList.add(item[0].trim());
				} else {
					int endLength = item.length;
					if (OrderType.ASC.name().equalsIgnoreCase(item[endLength - 1].trim())) {
						endLength = endLength - 1;
					} else if (OrderType.DESC.name().equalsIgnoreCase(item[endLength - 1].trim())) {
						type = OrderType.DESC;
						endLength = endLength - 1;
					}
					for (int j = 0; j < endLength; j++) {
						if (item[j].trim().length() > 0) {
							columnList.add(item[j].trim());
						}
					}
				}
				OrderByVo orderBy = new OrderByVo(columnList, type);
				orderByList.add(orderBy);
			}
			selectVo.setOrderByList(orderByList);
		}

		// 条件解析
		if (wherePos > -1) {
			int endPos = length;
			if (limitPos > -1) {
				endPos = limitPos;
			}
			if (orderByPos > -1) {
				endPos = orderByPos;
			}
			// String where = sql.substring(wherePos + WHERE_MARK.length(), endPos).trim();
			// System.out.println(where);
			WhereCondition condition = parseSelectWhere(sql, wherePos + WHERE_MARK.length(), endPos);
			selectVo.setCondition(condition);
		}
		selectVo.check();
		return selectVo;
	}

	/**
	 * 解析表名称
	 */
	public int parseTableName(SelectVo selectVo, String sql, int startPos, int endPos) {
		StringBuilder builder = new StringBuilder();
		for (int i = startPos; i < endPos; i++) {
			char key = sql.charAt(i);
			switch (key) {
			case '\r':
			case '\n':
			case '\t':
			case ' ':
				if (builder.length() > 0) {
					selectVo.setTable(builder.toString());
					return i;
				}
				break;
			default:
				builder.append(key);
				break;
			}
		}
		if (builder.length() > 0) {
			selectVo.setTable(builder.toString());
		}
		return endPos;
	}

}
