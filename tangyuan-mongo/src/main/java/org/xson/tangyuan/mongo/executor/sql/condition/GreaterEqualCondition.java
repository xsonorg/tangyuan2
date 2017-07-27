package org.xson.tangyuan.mongo.executor.sql.condition;

import org.xson.tangyuan.mongo.executor.sql.SqlParseException;
import org.xson.tangyuan.mongo.executor.sql.SqlParser;
import org.xson.tangyuan.mongo.executor.sql.ValueVo;
import org.xson.tangyuan.mongo.executor.sql.WhereCondition;
import org.xson.tangyuan.mongo.executor.sql.ValueVo.ValueType;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * a>=b
 */
public class GreaterEqualCondition extends WhereCondition {

	private ValueVo value;

	@Override
	public void setValue(Object value) {
		this.value = (ValueVo) value;
	}

	@Override
	public void check() {
		if (null == value) {
			throw new SqlParseException("Illegal greater equal condition");
		}
	}

	@Override
	public void toSQL(StringBuilder builder) {
		builder.append(this.name);
		builder.append(SqlParser.BLANK_MARK);
		builder.append(">=");
		builder.append(SqlParser.BLANK_MARK);
		if (ValueType.STRING == value.getType()) {
			builder.append('\'');
			builder.append(value.getValue());
			builder.append('\'');
		} else {
			builder.append(value.getValue());
		}
	}

	@Override
	public void setQuery(DBObject query, BasicDBList orList) {
		if (null == orList) {
			query.put(this.name, new BasicDBObject("$gte", value.getValue()));
		} else {
			orList.add(new BasicDBObject(this.name, new BasicDBObject("$gte", value.getValue())));
		}
	}
}
