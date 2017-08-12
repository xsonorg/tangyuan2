package org.xson.tangyuan.mongo.executor.sql.condition;

import java.util.List;

import org.xson.tangyuan.mongo.executor.sql.SqlParseException;
import org.xson.tangyuan.mongo.executor.sql.SqlParser;
import org.xson.tangyuan.mongo.executor.sql.ValueVo;
import org.xson.tangyuan.mongo.executor.sql.WhereCondition;
import org.xson.tangyuan.mongo.executor.sql.ValueVo.ValueType;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * a not in (1, 2, 3)
 */
public class NotInCondition extends WhereCondition {

	private List<ValueVo> value;

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(Object value) {
		this.value = (List<ValueVo>) value;
	}

	@Override
	public void check() {
		if (null == value || 0 == value.size()) {
			throw new SqlParseException("Illegal not in condition");
		}
	}

	@Override
	public void toSQL(StringBuilder builder) {
		builder.append(this.name);
		builder.append(SqlParser.BLANK_MARK);
		builder.append("NOT IN");
		builder.append(SqlParser.BLANK_MARK);
		builder.append("(");
		for (int i = 0, n = value.size(); i < n; i++) {
			if (i > 0) {
				builder.append(",");
				builder.append(SqlParser.BLANK_MARK);
			}
			ValueVo valueVo = value.get(i);
			if (ValueType.STRING == valueVo.getType()) {
				builder.append('\'');
				builder.append(valueVo.getSqlValue());
				builder.append('\'');
			} else {
				builder.append(valueVo.getSqlValue());
			}
		}
		builder.append(")");
	}

	@Override
	public void setQuery(DBObject query, BasicDBList orList, Object arg) {
		if (null == orList) {
			BasicDBList list = new BasicDBList();
			for (int i = 0, n = value.size(); i < n; i++) {
				list.add(value.get(i).getValue(arg));
			}
			query.put(this.name, new BasicDBObject("$nin", list));
		} else {
			BasicDBList list = new BasicDBList();
			for (int i = 0, n = value.size(); i < n; i++) {
				list.add(value.get(i).getValue(arg));
			}
			orList.add(new BasicDBObject(this.name, new BasicDBObject("$nin", list)));
		}
	}

}
