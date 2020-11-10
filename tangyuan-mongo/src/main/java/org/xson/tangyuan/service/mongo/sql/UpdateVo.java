package org.xson.tangyuan.service.mongo.sql;

import java.util.List;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.ext.LogExtUtil;
import org.xson.tangyuan.service.mongo.sql.ValueVo.ValueType;

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
		// NORMAL, ADD, MINUS
		NORMAL, ADD, MINUS, MUL
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
			} else if (ColumnUpdateType.MINUS == columnUpdateVo.getType()) {
				builder.append(columnUpdateVo.getName());
				builder.append(SqlParser.BLANK_MARK);
				builder.append("-");
				builder.append(SqlParser.BLANK_MARK);
				builder.append(valueVo.getSqlValue());
			} else if (ColumnUpdateType.MUL == columnUpdateVo.getType()) {
				builder.append(columnUpdateVo.getName());
				builder.append(SqlParser.BLANK_MARK);
				builder.append("*");
				builder.append(SqlParser.BLANK_MARK);
				builder.append(valueVo.getSqlValue());
			}

			// else {
			// builder.append(columnUpdateVo.getName());
			// builder.append(SqlParser.BLANK_MARK);
			// builder.append("-");
			// builder.append(SqlParser.BLANK_MARK);
			// builder.append(valueVo.getSqlValue());
			// }
		}
		if (null != condition) {
			builder.append(SqlParser.BLANK_MARK);
			builder.append(SqlParser.WHERE_MARK);
			builder.append(SqlParser.BLANK_MARK);
			condition.toSQL(builder);
		}
		return builder.toString();
	}

	public int update(DBCollection collection, WriteConcern writeConcern, Object arg) {
		DBObject query = new BasicDBObject();
		DBObject update = new BasicDBObject();

		if (null != this.condition) {
			this.condition.setQuery(query, null, arg);
		}

		boolean hasSet = false;
		boolean hasInc = false;
		boolean hasMul = false;// 新增:乘法
		DBObject setObject = new BasicDBObject();
		DBObject incObject = new BasicDBObject();
		DBObject mulObject = new BasicDBObject();
		for (int i = 0, n = setColumns.size(); i < n; i++) {
			ColumnUpdateVo columnUpdateVo = setColumns.get(i);
			ValueVo valueVo = columnUpdateVo.getValueVo();
			if (ColumnUpdateType.NORMAL == columnUpdateVo.getType()) {
				setObject.put(columnUpdateVo.getName(), valueVo.getValue(arg));
				hasSet = true;
			} else if (ColumnUpdateType.ADD == columnUpdateVo.getType()) {
				// updateSetting.put("$inc", new BasicDBObject(columnUpdateVo.getName(), valueVo.getValue()));
				incObject.put(columnUpdateVo.getName(), valueVo.getValue(arg));
				hasInc = true;
			} else if (ColumnUpdateType.MINUS == columnUpdateVo.getType()) {
				if (ValueType.INTEGER == valueVo.getType()) {
					int val = ((Integer) valueVo.getValue(arg)).intValue();
					// updateSetting.put("$inc", new BasicDBObject(columnUpdateVo.getName(), -val));
					incObject.put(columnUpdateVo.getName(), -val);
				} else if (ValueType.LONG == valueVo.getType()) {
					long val = ((Integer) valueVo.getValue(arg)).intValue();
					// updateSetting.put("$inc", new BasicDBObject(columnUpdateVo.getName(), -val));
					incObject.put(columnUpdateVo.getName(), -val);
				} else if (ValueType.DOUBLE == valueVo.getType()) {
					double val = ((Double) valueVo.getValue(arg)).doubleValue();
					// updateSetting.put("$inc", new BasicDBObject(columnUpdateVo.getName(), -val));
					incObject.put(columnUpdateVo.getName(), -val);
				} else {
					throw new SqlParseException("When updating a minus operation, invalid data type: " + valueVo.getType());
				}
				hasInc = true;
			} else if (ColumnUpdateType.MUL == columnUpdateVo.getType()) {
				mulObject.put(columnUpdateVo.getName(), valueVo.getValue(arg));
				hasMul = true;
			}
		}

		if (hasSet) {
			update.put("$set", setObject);
		}
		if (hasInc) {
			update.put("$inc", incObject);
		}
		if (hasMul) {
			update.put("$mul", mulObject);
		}

		// upsert: 如果未找到记录是否插入 ,multi:是否更新多条
		WriteResult result = collection.update(query, update, false, true, writeConcern);
		if (log.isInfoEnabled() && LogExtUtil.isMongoSqlShellPrint()) {
			// 日志显示
			log(collection, query, update);
		}

		return result.getN();
	}

	private void log(DBCollection collection, DBObject query, DBObject update) {
		StringBuilder sb = new StringBuilder();
		sb.append("db.");
		sb.append(collection.getName());
		sb.append(".update(");
		sb.append(query.toString());
		sb.append(", ");
		sb.append(update.toString());
		sb.append(", ");
		sb.append("{upsert: false, multi: true}");// upsert, final boolean multi
		sb.append(")");
		log.info(sb.toString());
	}

}
