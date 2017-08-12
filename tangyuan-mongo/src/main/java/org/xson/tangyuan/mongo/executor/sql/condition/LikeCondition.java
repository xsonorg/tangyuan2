package org.xson.tangyuan.mongo.executor.sql.condition;

import java.util.regex.Pattern;

import org.xson.tangyuan.mongo.executor.sql.SqlParseException;
import org.xson.tangyuan.mongo.executor.sql.SqlParser;
import org.xson.tangyuan.mongo.executor.sql.ValueVo;
import org.xson.tangyuan.mongo.executor.sql.WhereCondition;
import org.xson.tangyuan.mongo.executor.sql.ValueVo.ValueType;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * a like b
 */
public class LikeCondition extends WhereCondition {

	// private String value;
	private ValueVo value;

	@Override
	public void setValue(Object value) {
		// this.value = (String) ((ValueVo) value).getValue();
		this.value = (ValueVo) value;
	}

	@Override
	public void check() {
		if (null == value) {
			throw new SqlParseException("Illegal like condition");
		}
	}

	@Override
	public void toSQL(StringBuilder builder) {
		builder.append(this.name);
		builder.append(SqlParser.BLANK_MARK);
		builder.append("LIKE");
		builder.append(SqlParser.BLANK_MARK);
		builder.append('\'');
		builder.append(value);// TODO
		builder.append('\'');
	}

	@Override
	public void setQuery(DBObject query, BasicDBList orList, Object arg) {
		// Pattern expr = Pattern.compile(value, Pattern.CASE_INSENSITIVE);
		Object expr = null;
		if (ValueType.CALL == this.value.getType()) {
			expr = this.value.getValue(arg);
		} else {
			expr = Pattern.compile(this.value.getValue(arg).toString(), Pattern.CASE_INSENSITIVE);
		}
		if (null == orList) {
			query.put(this.name, expr);
		} else {
			orList.add(new BasicDBObject(this.name, expr));
		}
	}
}
