package org.xson.tangyuan.service.mongo.sql.condition;

import java.util.regex.Pattern;

import org.xson.tangyuan.service.mongo.sql.SqlParseException;
import org.xson.tangyuan.service.mongo.sql.SqlParser;
import org.xson.tangyuan.service.mongo.sql.ValueVo;
import org.xson.tangyuan.service.mongo.sql.ValueVo.ValueType;
import org.xson.tangyuan.service.mongo.sql.WhereCondition;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSONExt;
import com.mongodb.util.JSONExtCallback;

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
			//			expr = Pattern.compile(this.value.getValue(arg).toString(), Pattern.CASE_INSENSITIVE);
			expr = this.value.getValue(arg);
			if (!(expr instanceof Pattern)) {
				expr = parseRegex(expr.toString(), arg);
			}
		}
		if (null == orList) {
			query.put(this.name, expr);
		} else {
			orList.add(new BasicDBObject(this.name, expr));
		}
	}

	private Object parseRegex(String str, Object arg) {
		if ('/' == str.charAt(0)) {
			return JSONExt.parse(str, new JSONExtCallback(arg));
		}
		// sql like
		char first = str.charAt(0);
		if (!('%' == first || '_' == first)) {
			str = "^" + str;
		}
		str = str.replace("%", ".*");
		str = str.replace("_", ".");
		return Pattern.compile(str, Pattern.CASE_INSENSITIVE);
	}

}
