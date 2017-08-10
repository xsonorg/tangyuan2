package org.xson.tangyuan.mongo.executor.sql;

import java.util.List;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.mongo.executor.sql.ValueVo.ValueType;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

// UPDATE Person SET FirstName = 'Fred' WHERE LastName = 'Wilson'
// UPDATE Person SET FirstName = FirstName + 1, WHERE LastName = 'Wilson'

public class UpdateVo implements SqlVo {

	private static Log log = LogFactory.getLog(UpdateVo.class);

	public enum ColumnUpdateType {
		NORMAL, ADD, MINUS
	}

	private List<ColumnUpdateVo>	setColumns;

	private String					table;

	private WhereCondition			condition;

	public void setSetColumns(List<ColumnUpdateVo> setColumns) {
		this.setColumns = setColumns;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public void setCondition(WhereCondition condition) {
		this.condition = condition;
	}

	@Override
	public String getTable() {
		return this.table;
	}

	@Override
	public void check() {
		if (null == table || 0 == table.length()) {
			throw new SqlParseException("update [table] is null");
		}

		if (null == setColumns || 0 == setColumns.size()) {
			throw new SqlParseException("update [set] is null");
		}
		if (null != condition) {
			condition.check();
		}
	}

	@Override
	public String toSQL() {
		StringBuilder builder = new StringBuilder();
		builder.append(SqlParser.UPDATE_MARK);
		builder.append(SqlParser.BLANK_MARK);
		builder.append(table);
		builder.append(SqlParser.BLANK_MARK);
		builder.append("SET");
		builder.append(SqlParser.BLANK_MARK);
		for (int i = 0, n = setColumns.size(); i < n; i++) {
			if (i > 0) {
				builder.append(",");
				builder.append(SqlParser.BLANK_MARK);
			}
			ColumnUpdateVo columnUpdateVo = setColumns.get(i);
			builder.append(columnUpdateVo.getName());
			builder.append(SqlParser.BLANK_MARK);
			builder.append("=");
			builder.append(SqlParser.BLANK_MARK);

			ValueVo valueVo = columnUpdateVo.getValueVo();
			if (ColumnUpdateType.NORMAL == columnUpdateVo.getType()) {
				if (ValueType.STRING == valueVo.getType()) {
					builder.append('\'');
					builder.append(valueVo.getSqlValue());
					builder.append('\'');
				} else {
					builder.append(valueVo.getSqlValue());
				}
			} else if (ColumnUpdateType.ADD == columnUpdateVo.getType()) {
				builder.append(columnUpdateVo.getName());
				builder.append(SqlParser.BLANK_MARK);
				builder.append("+");
				builder.append(SqlParser.BLANK_MARK);
				builder.append(valueVo.getSqlValue());
			} else {
				builder.append(columnUpdateVo.getName());
				builder.append(SqlParser.BLANK_MARK);
				builder.append("-");
				builder.append(SqlParser.BLANK_MARK);
				builder.append(valueVo.getSqlValue());
			}
		}
		if (null != condition) {
			builder.append(SqlParser.BLANK_MARK);
			builder.append(SqlParser.WHERE_MARK);
			builder.append(SqlParser.BLANK_MARK);
			condition.toSQL(builder);
		}
		return builder.toString();
	}

	public int update(DBCollection collection, WriteConcern writeConcern) {
		DBObject query = new BasicDBObject();
		DBObject update = new BasicDBObject();

		if (null != this.condition) {
			this.condition.setQuery(query, null);
		}

		boolean hasSet = false;
		boolean hasInc = false;
		DBObject setObject = new BasicDBObject();
		DBObject incObject = new BasicDBObject();
		for (int i = 0, n = setColumns.size(); i < n; i++) {
			ColumnUpdateVo columnUpdateVo = setColumns.get(i);
			ValueVo valueVo = columnUpdateVo.getValueVo();
			if (ColumnUpdateType.NORMAL == columnUpdateVo.getType()) {
				setObject.put(columnUpdateVo.getName(), valueVo.getValue());
				hasSet = true;
			} else if (ColumnUpdateType.ADD == columnUpdateVo.getType()) {
				// updateSetting.put("$inc", new BasicDBObject(columnUpdateVo.getName(), valueVo.getValue()));
				incObject.put(columnUpdateVo.getName(), valueVo.getValue());
				hasInc = true;
			} else {
				if (ValueType.INTEGER == valueVo.getType()) {
					int val = ((Integer) valueVo.getValue()).intValue();
					// updateSetting.put("$inc", new BasicDBObject(columnUpdateVo.getName(), -val));
					incObject.put(columnUpdateVo.getName(), -val);
				} else if (ValueType.LONG == valueVo.getType()) {
					long val = ((Integer) valueVo.getValue()).intValue();
					// updateSetting.put("$inc", new BasicDBObject(columnUpdateVo.getName(), -val));
					incObject.put(columnUpdateVo.getName(), -val);
				} else if (ValueType.DOUBLE == valueVo.getType()) {
					double val = ((Double) valueVo.getValue()).doubleValue();
					// updateSetting.put("$inc", new BasicDBObject(columnUpdateVo.getName(), -val));
					incObject.put(columnUpdateVo.getName(), -val);
				} else {
					throw new SqlParseException("When updating a minus operation, invalid data type: " + valueVo.getType());
				}
				hasInc = true;
			}
		}

		if (hasSet) {
			update.put("$set", setObject);
		}
		if (hasInc) {
			update.put("$inc", incObject);
		}

		// 日志显示
		log(query, update);

		// upsert: 如果未找到记录是否插入
		// multi:是否更新多条

		// WriteResult result = collection.update(query, update, false, true, WriteConcern.ACKNOWLEDGED);
		// WriteResult result = collection.update(query, update, false, true, MongoComponent.getInstance().getDefaultWriteConcern());
		WriteResult result = collection.update(query, update, false, true, writeConcern);

		// System.out.println(query.toString());
		// System.out.println(update.toString());
		return result.getN();
	}

	private void log(DBObject query, DBObject update) {
		if (log.isInfoEnabled()) {
			if (null != query) {
				log.info("query:" + query.toString());
			}
			if (null != update) {
				log.info("update:" + update.toString());
			}
		}
	}
}
