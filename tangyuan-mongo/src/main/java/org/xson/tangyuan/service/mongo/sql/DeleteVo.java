package org.xson.tangyuan.service.mongo.sql;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.ext.LogExtUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

//DELETE FROM 表名称 WHERE 列名称 = 值

public class DeleteVo implements SqlVo {

	private static Log     log = LogFactory.getLog(DeleteVo.class);

	private String         table;

	private WhereCondition condition;

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

	public void check() {
		if (null == table || 0 == table.length()) {
			throw new SqlParseException("delete [table] is null");
		}
		if (null != condition) {
			condition.check();
		}
	}

	@Override
	public String toSQL() {
		StringBuilder builder = new StringBuilder();
		builder.append(SqlParser.DELETE_MARK);
		builder.append(SqlParser.BLANK_MARK);
		builder.append(this.table);
		if (null != condition) {
			builder.append(SqlParser.BLANK_MARK);
			builder.append(SqlParser.WHERE_MARK);
			builder.append(SqlParser.BLANK_MARK);
			condition.toSQL(builder);
		}
		return builder.toString();
	}

	public int delete(DBCollection collection, WriteConcern writeConcern, Object arg) {
		DBObject query = new BasicDBObject();
		if (null != condition) {
			this.condition.setQuery(query, null, arg);
		}

		WriteResult result = collection.remove(query, writeConcern);
		if (log.isInfoEnabled() && LogExtUtil.isMongoSqlShellPrint()) {
			// 日志显示
			log(collection, query);
		}

		return result.getN();
	}

	private void log(DBCollection collection, DBObject query) {
		StringBuilder sb = new StringBuilder();
		sb.append("db.");
		sb.append(collection.getName());
		sb.append(".remove(");
		sb.append(query.toString());
		sb.append(")");
		log.info(sb.toString());
	}

	// // // // // // // // // // // // // // // // // // // // // // // // // // 

	//		log(query);
	// WriteResult result = collection.remove(query, WriteConcern.ACKNOWLEDGED);
	// collection.remove(query)
	// System.out.println(query.toString());
	//	private void log(DBObject query) {
	//		if (log.isInfoEnabled()) {
	//			if (null != query) {
	//				log.info("query:" + query.toString());
	//			}
	//		}
	//	}
}
