package org.xson.tangyuan.service.mongo.sql.condition;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.xson.tangyuan.service.mongo.sql.SqlParseException;
import org.xson.tangyuan.service.mongo.sql.ValueVo;
import org.xson.tangyuan.service.mongo.sql.WhereCondition;
import org.xson.tangyuan.util.CollectionUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * a in (1, 2, 3)
 */
public class InCondition extends WhereCondition {

	private ValueVo value;

	@Override
	public void setValue(Object value) {
		this.value = (ValueVo) value;
	}

	@Override
	public void check() {
		if (null == value) {
			throw new SqlParseException("Illegal in condition");
		}
	}

	@Override
	public void toSQL(StringBuilder builder) {
		// builder.append(this.name);
		// builder.append(SqlParser.BLANK_MARK);
		// builder.append("IN");
		// builder.append(SqlParser.BLANK_MARK);
		// builder.append("(");
		// for (int i = 0, n = value.size(); i < n; i++) {
		// if (i > 0) {
		// builder.append(",");
		// builder.append(SqlParser.BLANK_MARK);
		// }
		// ValueVo valueVo = value.get(i);
		// if (ValueType.STRING == valueVo.getType()) {
		// builder.append('\'');
		// builder.append(valueVo.getSqlValue());
		// builder.append('\'');
		// } else {
		// builder.append(valueVo.getSqlValue());
		// }
		// }
		// builder.append(")");
	}

	@Override
	public void setQuery(DBObject query, BasicDBList orList, Object arg) {
		if (isObjectIdField(this.name)) {
			Object v = value.getValue(arg);
			v = convertObjectId(v);
			if (null == orList) {
				query.put(this.name, new BasicDBObject("$in", v));
			} else {
				orList.add(new BasicDBObject(this.name, new BasicDBObject("$in", v)));
			}
		} else {
			if (null == orList) {
				query.put(this.name, new BasicDBObject("$in", this.value.getValue(arg)));
			} else {
				orList.add(new BasicDBObject(this.name, new BasicDBObject("$in", this.value.getValue(arg))));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private Object convertObjectId(Object value) {

		if (null == value) {
			return value;
		}

		if (value instanceof List) {
			List list = (List) value;
			if (CollectionUtils.isEmpty(list)) {
				return value;
			}
			if (list.get(0) instanceof ObjectId) {
				return value;
			}
			List<ObjectId> ids = new ArrayList<ObjectId>();
			for (Object o : list) {
				ids.add(new ObjectId(o.toString()));
			}
			return ids;
		}

		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			if (0 == length) {
				return value;
			}
			Class<?> cls = value.getClass().getComponentType();
			if (cls == ObjectId.class) {
				return value;
			}
			List<ObjectId> ids = new ArrayList<ObjectId>();
			for (int i = 0; i < length; i++) {
				ids.add(new ObjectId(Array.get(value, i).toString()));
			}
			return ids;
		}
		return value;
	}

}
