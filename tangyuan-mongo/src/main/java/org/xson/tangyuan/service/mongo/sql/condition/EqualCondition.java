package org.xson.tangyuan.service.mongo.sql.condition;

import org.bson.types.ObjectId;
import org.xson.tangyuan.service.mongo.sql.SqlParseException;
import org.xson.tangyuan.service.mongo.sql.SqlParser;
import org.xson.tangyuan.service.mongo.sql.ValueVo;
import org.xson.tangyuan.service.mongo.sql.ValueVo.ValueType;
import org.xson.tangyuan.service.mongo.sql.WhereCondition;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * a=b
 */
public class EqualCondition extends WhereCondition {

	private ValueVo value;

	// TODO 还要确定是不是OID

	@Override
	public void setValue(Object value) {
		this.value = (ValueVo) value;
	}

	@Override
	public void check() {
		if (null == value) {
			throw new SqlParseException("Illegal equal condition");
		}
	}

	@Override
	public void toSQL(StringBuilder builder) {
		builder.append(this.name);
		builder.append(SqlParser.BLANK_MARK);
		builder.append("=");
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
		// if (null == orList) {
		// query.put(this.name, value.getValue());
		// } else {
		// orList.add(new BasicDBObject(this.name, value.getValue()));
		// }

		//		if (this.name.equals("_id")) {
		if (isObjectIdField(this.name)) {
			Object v = value.getValue(arg);
			if (!(v instanceof ObjectId)) {
				v = new ObjectId(v.toString());
			}
			if (null == orList) {
				// query.put(this.name, new ObjectId(value.getValue(arg).toString()));
				query.put(this.name, v);
			} else {
				// orList.add(new BasicDBObject(this.name, new ObjectId(value.getValue(arg).toString())));
				orList.add(new BasicDBObject(this.name, v));
			}
		} else {
			if (null == orList) {
				query.put(this.name, value.getValue(arg));
			} else {
				orList.add(new BasicDBObject(this.name, value.getValue(arg)));
			}
		}
	}
}
