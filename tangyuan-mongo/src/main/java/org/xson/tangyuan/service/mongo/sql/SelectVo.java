package org.xson.tangyuan.service.mongo.sql;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.ext.LogExtUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

// SELECT
// [ALL | DISTINCT | DISTINCTROW ]
// [FROM table_references
// [WHERE where_definition]
// [ORDER BY {col_name | expr | position}
// [ASC | DESC] , ...]
// [LIMIT {[offset,] row_count | row_count OFFSET offset}]
public class SelectVo implements SqlVo {

	private static Log		log	= LogFactory.getLog(SelectVo.class);

	// 列字段
	private List<String>	columns;

	private String			allColumn;

	private String			count;

	private String			table;

	private WhereCondition	condition;

	private List<OrderByVo>	orderByList;

	private LimitVo			limit;

	public void setAllColumn(String allColumn) {
		this.allColumn = allColumn;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public void setCondition(WhereCondition condition) {
		this.condition = condition;
	}

	public void setOrderByList(List<OrderByVo> orderByList) {
		this.orderByList = orderByList;
	}

	public void setLimit(LimitVo limit) {
		this.limit = limit;
	}

	public void check() {

		if (null == table || 0 == table.length()) {
			throw new SqlParseException("select [table] is null");
		}

		if (null == columns && null == allColumn && null == count) {
			throw new SqlParseException("select [column] is null");
		}

		if (null != condition) {
			condition.check();
		}
	}

	@Override
	public String getTable() {
		return this.table;
	}

	public Object toBSON() {
		return null;
	}

	public String toSQL() {
		StringBuilder builder = new StringBuilder();
		builder.append(SqlParser.SELECT_MARK);
		builder.append(SqlParser.BLANK_MARK);

		if (null == this.count) {
			if (null != allColumn) {
				builder.append(allColumn);
			} else {
				for (int i = 0, n = columns.size(); i < n; i++) {
					if (i > 0) {
						builder.append(",");
						builder.append(SqlParser.BLANK_MARK);
					}
					builder.append(columns.get(i));
				}
			}
		} else {
			builder.append(this.count);
		}

		builder.append(SqlParser.BLANK_MARK);
		builder.append(SqlParser.FROM_MARK);
		builder.append(SqlParser.BLANK_MARK);

		builder.append(this.table);
		if (null != condition) {
			builder.append(SqlParser.BLANK_MARK);
			builder.append(SqlParser.WHERE_MARK);
			builder.append(SqlParser.BLANK_MARK);
			condition.toSQL(builder);
		}

		if (null != this.orderByList) {
			builder.append(SqlParser.BLANK_MARK);
			builder.append(SqlParser.ORDER_BY_MARK);
			builder.append(SqlParser.BLANK_MARK);
			for (int i = 0, n = orderByList.size(); i < n; i++) {
				if (i > 0) {
					builder.append(",");
					builder.append(SqlParser.BLANK_MARK);
				}
				orderByList.get(i).toSQL(builder);
			}
		}

		if (null != limit) {
			limit.toSQL(builder);
		}

		return builder.toString();
	}

	private DBObject getFields() {
		if (null != this.allColumn) {
			return null;
		}
		DBObject fields = new BasicDBObject();
		// fields.put("_id", 0);
		for (int i = 0, n = columns.size(); i < n; i++) {
			// fields.put(columns.get(i), true);
			fields.put(columns.get(i), 1);// 1代表需要指定显示的列
		}
		return fields;
	}

	private DBObject getQuery(Object arg) {
		if (null != this.condition) {
			DBObject query = new BasicDBObject();
			this.condition.setQuery(query, null, arg);
			return query;
		}
		return null;
	}

	private DBObject getOrderByObject() {
		DBObject orderByObject = null;
		if (null != this.orderByList) {
			orderByObject = new BasicDBObject();
			for (OrderByVo orderByVo : orderByList) {
				for (String column : orderByVo.getColumns()) {
					orderByObject.put(column, orderByVo.getType().getValue());
				}
			}
		}
		return orderByObject;
	}

	private long getQueryCount(DBCollection collection, Object arg) {
		DBObject query = getQuery(arg);
		long result = -1;
		if (null == query) {
			result = collection.count();
		} else {
			result = collection.count(query);
		}

		if (log.isInfoEnabled() && LogExtUtil.isMongoSqlShellPrint()) {
			logCount(collection, query);
		}

		return result;
	}

	public DBObject selectOne(DBCollection collection, Object arg) {
		DBObject fields = getFields();
		DBObject query = getQuery(arg);
		DBObject orderByObject = getOrderByObject();

		DBObject result = null;
		if (null == fields && null == orderByObject) {
			result = collection.findOne(query);
		} else if (null != fields && null == orderByObject) {
			result = collection.findOne(query, fields);
		} else {
			result = collection.findOne(query, fields, orderByObject);
		}

		if (log.isInfoEnabled() && LogExtUtil.isMongoSqlShellPrint()) {
			logFindOne(collection, query, fields, orderByObject);
		}

		return result;
	}

	public Object selectVar(DBCollection collection, Object arg) {
		if (null == this.count) {
			DBObject result = selectOne(collection, arg);
			return result;
		} else {
			return getQueryCount(collection, arg);
		}
	}

	/** 仅获取一个字段 */
	public Object selectVarOneField(XCO one) {
		if (null == columns || columns.size() != 1) {
			throw new TangYuanException("SelectOne query, the return column is not unique.");
		}
		String column = columns.get(0);
		return one.getObjectValue(column);
	}

	public DBCursor selectSet(DBCollection collection, Object arg) {
		DBObject fields = getFields();
		DBObject query = getQuery(arg);
		DBObject orderByObject = getOrderByObject();
		DBCursor cursor = null;

		if (null != query && null == fields) {
			cursor = collection.find(query);
		} else if (null == query && null != fields) {
			cursor = collection.find(new BasicDBObject(), fields);
		} else if (null != fields && null != query) {
			cursor = collection.find(query, fields);
		} else {
			cursor = collection.find();
		}

		if (null != orderByObject) {
			cursor.sort(orderByObject);
		}
		if (null != this.limit) {
			if (null == this.limit.getOffset()) {
				cursor.limit(this.limit.getRowCount());
			} else {
				cursor.limit(this.limit.getRowCount());
				cursor.skip(this.limit.getOffset());
			}
		}

		// 日志
		if (log.isInfoEnabled() && LogExtUtil.isMongoSqlShellPrint()) {
			logFind(collection, query, fields, orderByObject);
		}

		return cursor;
	}

	private void logCount(DBCollection collection, DBObject query) {
		StringBuilder sb = new StringBuilder();
		sb.append("db.");
		sb.append(collection.getName());
		sb.append(".count(");
		if (null != query) {
			sb.append(query.toString());
		}
		sb.append(")");
		log.info(sb.toString());
	}

	private void logFindOne(DBCollection collection, DBObject query, DBObject fields, DBObject orderByObject) {
		StringBuilder sb = new StringBuilder();
		sb.append("db.");
		sb.append(collection.getName());
		sb.append(".findOne(");
		if (null == query) {
			query = new BasicDBObject();
		}
		sb.append(query.toString());
		if (null != fields) {
			sb.append(", ");
			sb.append(fields.toString());
		}
		if (null != orderByObject) {
			sb.append(", ");
			sb.append(orderByObject.toString());
		}
		sb.append(")");
		log.info(sb.toString());
	}

	private void logFind(DBCollection collection, DBObject query, DBObject fields, DBObject orderByObject) {
		StringBuilder sb = new StringBuilder();
		sb.append("db.");
		sb.append(collection.getName());
		sb.append(".find(");
		if (null != query && null == fields) {
			sb.append(query.toString());
		} else if (null == query && null != fields) {
			sb.append(new BasicDBObject().toString());
			sb.append(", ");
			sb.append(fields.toString());
		} else if (null != fields && null != query) {
			sb.append(query.toString());
			sb.append(", ");
			sb.append(fields.toString());
		}
		sb.append(")");

		if (null != orderByObject) {
			sb.append(".sort(");
			sb.append(orderByObject.toString());
			sb.append(")");
		}

		if (null != this.limit) {
			if (null == this.limit.getOffset()) {
				sb.append(".limit(");
				sb.append(this.limit.getRowCount());
				sb.append(")");
			} else {
				sb.append(".limit(");
				sb.append(this.limit.getRowCount());
				sb.append(")");

				sb.append(".skip(");
				sb.append(this.limit.getOffset());
				sb.append(")");
			}
		}

		log.info(sb.toString());
	}

}
