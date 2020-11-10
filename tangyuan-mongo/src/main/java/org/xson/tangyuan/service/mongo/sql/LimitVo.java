package org.xson.tangyuan.service.mongo.sql;

public class LimitVo {

	private Integer	offset;

	private Integer	rowCount;

	public LimitVo(Integer offset, Integer rowCount) {
		this.offset = offset;
		this.rowCount = rowCount;
	}

	public Integer getOffset() {
		return offset;
	}

	public Integer getRowCount() {
		return rowCount;
	}

	public void toSQL(StringBuilder builder) {
		builder.append(SqlParser.BLANK_MARK);
		builder.append(SqlParser.LIMIT_MARK);
		builder.append(SqlParser.BLANK_MARK);
		if (null == offset) {
			builder.append(rowCount.intValue());
		} else {
			builder.append(offset.intValue());
			builder.append(",");
			builder.append(SqlParser.BLANK_MARK);
			builder.append(rowCount.intValue());
		}
	}
}
