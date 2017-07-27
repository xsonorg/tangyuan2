package org.xson.tangyuan.mongo.executor.sql.condition;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.mongo.executor.sql.SqlParseException;
import org.xson.tangyuan.mongo.executor.sql.SqlParser;
import org.xson.tangyuan.mongo.executor.sql.WhereCondition;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * ()
 */
public class BracketsCondition extends WhereCondition {

	private List<WhereCondition>	conditions;

	// true: and, false:or
	private Boolean					andOr;

	// 是否原始就有括号
	private boolean					brackets;

	public BracketsCondition(boolean brackets) {
		this.brackets = brackets;
	}

	public void setAndOr(Boolean andOr) {
		if (null == this.andOr) {
			this.andOr = andOr;
		} else if (this.andOr.booleanValue() != andOr.booleanValue()) {
			throw new SqlParseException("Illegal brackets condition and or.");
		}
	}

	public void addCondition(WhereCondition condition) {
		if (null == conditions) {
			conditions = new ArrayList<WhereCondition>();
		}
		conditions.add(condition);
	}

	public void setValue(Object value) {
		if (null == conditions && conditions.size() == 0) {
			throw new SqlParseException("Illegal brackets condition.");
		}
		conditions.get(conditions.size() - 1).setValue(value);
	}

	@Override
	public void check() {
		if (null == this.andOr) {
			this.andOr = true;
		}

		if (null == conditions || 0 == conditions.size()) {
			throw new SqlParseException("Illegal brackets condition.");
		}
	}

	@Override
	public void toSQL(StringBuilder builder) {
		if (brackets) {
			builder.append("(");
		}
		for (int i = 0, n = conditions.size(); i < n; i++) {
			if (i > 0) {
				builder.append(SqlParser.BLANK_MARK);
				if (this.andOr) {
					builder.append("AND");
				} else {
					builder.append("OR");
				}
				builder.append(SqlParser.BLANK_MARK);
			}
			WhereCondition condition = conditions.get(i);
			condition.toSQL(builder);
		}
		if (brackets) {
			builder.append(")");
		}
	}

	@Override
	public void setQuery(DBObject query, BasicDBList orList) {
		if (null == orList) {
			if (this.andOr) {
				for (int i = 0, n = conditions.size(); i < n; i++) {
					conditions.get(i).setQuery(query, null);
				}
			} else {
				BasicDBList newOrList = new BasicDBList();
				for (int i = 0, n = conditions.size(); i < n; i++) {
					conditions.get(i).setQuery(null, newOrList);
				}
				query.put("$or", newOrList);
			}
		} else {
			BasicDBObject newQuery = new BasicDBObject();
			if (this.andOr) {
				for (int i = 0, n = conditions.size(); i < n; i++) {
					conditions.get(i).setQuery(newQuery, null);
				}
			} else {
				BasicDBList newOrList = new BasicDBList();
				for (int i = 0, n = conditions.size(); i < n; i++) {
					conditions.get(i).setQuery(null, newOrList);
				}
				newQuery.put("$or", newOrList);
			}
			orList.add(newQuery);
		}
	}

}
