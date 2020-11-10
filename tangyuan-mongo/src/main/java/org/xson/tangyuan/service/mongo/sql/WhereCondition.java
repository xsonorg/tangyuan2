package org.xson.tangyuan.service.mongo.sql;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public abstract class WhereCondition {

	protected String name;

	public void setName(String name) {
		if (null == name) {
			throw new SqlParseException("The parameter is null");
		}
		name = name.trim();
		if (name.length() == 0) {
			throw new SqlParseException("The parameter is empty");
		}
		if (null != this.name) {
			throw new SqlParseException("left name is not null, this.name=" + this.name + ", name=" + name);
		}
		this.name = name;
		// if (null != name) {
		// // TODO ERROR 放在setName(leftKey);
		// }
	}

	protected boolean isObjectIdField(String field) {
		return "_id".equals(field);
	}

	abstract public void setValue(Object value);

	abstract public void check();

	abstract public void toSQL(StringBuilder builder);

	abstract public void setQuery(DBObject query, BasicDBList orList, Object arg);

	// abstract public boolean check();
	// abstract public void setQuery(DBObject query);
}
