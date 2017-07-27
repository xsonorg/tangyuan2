package org.xson.tangyuan.mongo.executor.sql;

import java.util.List;

public class OrderByVo {

	private List<String>	columns;
	private OrderType		type;

	public OrderByVo(List<String> columns, OrderType type) {
		this.columns = columns;
		this.type = type;
	}

	public List<String> getColumns() {
		return columns;
	}

	public OrderType getType() {
		return type;
	}

	public void toSQL(StringBuilder builder) {
		for (int i = 0, n = columns.size(); i < n; i++) {
			if (i > 0) {
				builder.append(SqlParser.BLANK_MARK);
			}
			builder.append(columns.get(i));
		}
		builder.append(SqlParser.BLANK_MARK);
		builder.append(type.name());
	}

}
