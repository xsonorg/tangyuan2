package org.xson.tangyuan.service.mongo.sql.condition;

import org.bson.BSONObject;
import org.xson.tangyuan.service.mongo.sql.SqlParseException;
import org.xson.tangyuan.service.mongo.sql.SqlParser;
import org.xson.tangyuan.service.mongo.sql.ValueVo;
import org.xson.tangyuan.service.mongo.sql.ValueVo.ValueType;
import org.xson.tangyuan.service.mongo.sql.WhereCondition;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * a>b
 */
public class MoreThanCondition extends WhereCondition {

	private ValueVo value;

	@Override
	public void setValue(Object value) {
		this.value = (ValueVo) value;
	}

	@Override
	public void check() {
		if (null == value) {
			throw new SqlParseException("Illegal more than condition");
		}
	}

	@Override
	public void toSQL(StringBuilder builder) {
		builder.append(this.name);
		builder.append(SqlParser.BLANK_MARK);
		builder.append(">");
		builder.append(SqlParser.BLANK_MARK);
		if (ValueType.STRING == value.getType()) {
			builder.append('\'');
			builder.append(value.getSqlValue());
			builder.append('\'');
		} else {
			builder.append(value.getSqlValue());
		}
	}

	@Override
	public void setQuery(DBObject query, BasicDBList orList, Object arg) {
		if (null == orList) {
			// query.put(this.name, new BasicDBObject("$gt", value.getValue(arg)));
			if (query.containsField(this.name)) {
				BSONObject bson = (BSONObject) query.get(this.name);
				bson.put("$gt", value.getValue(arg));
			} else {
				query.put(this.name, new BasicDBObject("$gt", value.getValue(arg)));
			}
		} else {
			orList.add(new BasicDBObject(this.name, new BasicDBObject("$gt", value.getValue(arg))));
		}
	}
}
