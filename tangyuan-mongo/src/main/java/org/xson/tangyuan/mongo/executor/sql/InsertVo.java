package org.xson.tangyuan.mongo.executor.sql;

import java.util.List;

import org.bson.types.ObjectId;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mongo.executor.sql.ValueVo.ValueType;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

public class InsertVo implements SqlVo {

	private static Log		log	= LogFactory.getLog(InsertVo.class);

	private List<String>	columns;

	private String			table;

	private List<ValueVo>	values;

	public void setTable(String table) {
		this.table = table;
	}

	public void setValues(List<ValueVo> values) {
		this.values = values;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	@Override
	public String getTable() {
		return this.table;
	}

	public void check() {
		if (null == table || 0 == table.length()) {
			throw new SqlParseException("insert [table] is null");
		}

		if (null == columns || 0 == columns.size()) {
			throw new SqlParseException("insert [column] is null");
		}

		if (null == values || 0 == values.size()) {
			throw new SqlParseException("insert [values] is null");
		}
	}

	public String toSQL() {
		StringBuilder builder = new StringBuilder();
		builder.append(SqlParser.INSERT_MARK);
		builder.append(SqlParser.BLANK_MARK);
		builder.append(table);
		builder.append("(");
		for (int i = 0, n = columns.size(); i < n; i++) {
			if (i > 0) {
				builder.append(",");
				builder.append(SqlParser.BLANK_MARK);
			}
			builder.append(columns.get(i));
		}
		builder.append(")");

		builder.append(SqlParser.BLANK_MARK);
		builder.append("VALUES");
		builder.append("(");
		for (int i = 0, n = values.size(); i < n; i++) {
			if (i > 0) {
				builder.append(",");
				builder.append(SqlParser.BLANK_MARK);
			}
			if (ValueType.STRING == values.get(i).getType()) {
				builder.append('\'');
				builder.append(values.get(i).getSqlValue());
				builder.append('\'');
			} else {
				builder.append(values.get(i).getSqlValue());
			}
		}
		builder.append(")");
		return builder.toString();
	}

	// public int insert(DBCollection collection) {
	// DBObject document = new BasicDBObject();
	// for (int i = 0, n = columns.size(); i < n; i++) {
	// document.put(columns.get(i), values.get(i).getValue());
	// }
	// log(document);
	// WriteResult result = collection.insert(document, WriteConcern.ACKNOWLEDGED);
	// return result.getN();
	// }

	public Object insert(DBCollection collection, WriteConcern writeConcern, Object arg) {
		DBObject document = new BasicDBObject();
		// 匹配_id
		for (int i = 0, n = columns.size(); i < n; i++) {
			// document.put(columns.get(i), values.get(i).getValue());

			String tempColumn = columns.get(i);
			if (3 == tempColumn.length() && tempColumn.equals("_id")) {
				document.put(tempColumn, new ObjectId(values.get(i).getValue(arg).toString()));
			} else {
				document.put(tempColumn, values.get(i).getValue(arg));
			}
		}
		log(document);
		// TODO: WriteConcern.ACKNOWLEDGED需要可以配置
		// WriteResult result = collection.insert(document, WriteConcern.ACKNOWLEDGED);
		// collection.insert(document, MongoComponent.getInstance().getDefaultWriteConcern());
		collection.insert(document, writeConcern);
		Object oid = document.get("_id");
		if (null != oid) {
			return oid.toString();
		}
		return null;
	}

	private void log(DBObject document) {
		if (log.isInfoEnabled()) {
			if (null != document) {
				log.info("document:" + document.toString());
			}
		}
	}
}
