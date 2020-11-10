package org.xson.tangyuan.service.mongo.sql;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.ext.LogExtUtil;
import org.xson.tangyuan.mongo.util.MongoUtil;
import org.xson.tangyuan.service.mongo.sql.ValueVo.ValueType;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.util.CollectionUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class InsertVo implements SqlVo {

	private static Log          log         = LogFactory.getLog(InsertVo.class);

	private List<String>        columns     = null;
	private String              table       = null;
	private List<ValueVo>       values      = null;
	private List<List<ValueVo>> valuesGroup = null;

	public void setTable(String table) {
		this.table = table;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public void setValues(List<ValueVo> values) {
		this.values = values;
	}

	public void setValuesGroup(List<List<ValueVo>> valuesGroup) {
		this.valuesGroup = valuesGroup;
	}

	@Override
	public String getTable() {
		return this.table;
	}

	public void check() {
		if (null == table || 0 == table.length()) {
			throw new SqlParseException("insert [table] is null");
		}

		if (CollectionUtils.isEmpty(this.columns)) {
			throw new SqlParseException("insert [column] is null");
		}

		if (CollectionUtils.isEmpty(this.values) && CollectionUtils.isEmpty(this.valuesGroup)) {
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

	public Object insert(DBCollection collection, WriteConcern writeConcern, Object arg) {

		if (null != this.valuesGroup) {
			return insertGroup(collection, writeConcern, arg);
		}

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

		WriteResult  result   = collection.insert(document, writeConcern);
		int          rowCount = result.getN();
		String       _id      = MongoUtil.getId(document);
		InsertReturn ir       = new InsertReturn(rowCount, _id);

		if (log.isInfoEnabled() && LogExtUtil.isMongoSqlShellPrint()) {
			log(collection, document);
		}

		return ir;
	}

	public Object insertGroup(DBCollection collection, WriteConcern writeConcern, Object arg) {

		List<DBObject> documents = new ArrayList<DBObject>();

		for (List<ValueVo> itemValues : this.valuesGroup) {
			DBObject document = new BasicDBObject();
			for (int i = 0, n = this.columns.size(); i < n; i++) {
				String tempColumn = this.columns.get(i);
				if (3 == tempColumn.length() && tempColumn.equals("_id")) {
					// 匹配_id
					document.put(tempColumn, new ObjectId(itemValues.get(i).getValue(arg).toString()));
				} else {
					document.put(tempColumn, itemValues.get(i).getValue(arg));
				}
			}
			documents.add(document);
		}

		WriteResult  result   = collection.insert(documents, writeConcern);
		int          rowCount = result.getN();
		List<String> idList   = new ArrayList<String>();
		for (DBObject document : documents) {
			String _id = MongoUtil.getId(document);
			if (null != _id) {
				idList.add(_id);
			}
		}
		if (CollectionUtils.isEmpty(idList)) {
			idList = null;
		}

		if (log.isInfoEnabled() && LogExtUtil.isMongoSqlShellPrint()) {
			log(collection, documents);
		}

		InsertReturn ir = new InsertReturn(rowCount, idList);
		return ir;
	}

	private void log(DBCollection collection, Object obj) {
		StringBuilder sb = new StringBuilder();
		sb.append("db.");
		sb.append(collection.getName());
		sb.append(".insert(");
		sb.append(obj.toString());
		sb.append(")");
		log.info(sb.toString());
	}

	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	//	
	//		String      _id      = null;
	//		Object      oid      = document.get("_id");
	//		if (null != oid) {
	//			_id = oid.toString();
	//		}
	//	private void log(DBObject document) {
	//		if (log.isInfoEnabled()) {
	//			if (null != document) {
	//				log.info("document:" + document.toString());
	//			}
	//		}
	//	}

	////////////////////////////////////////////////////////////////////////////

	//		log(document);
	// WriteConcern.ACKNOWLEDGED需要可以配置
	// WriteResult result = collection.insert(document, WriteConcern.ACKNOWLEDGED);
	// collection.insert(document, MongoComponent.getInstance().getDefaultWriteConcern());

	//		collection.insert(document, writeConcern);
	//		Object oid = document.get("_id");
	//		if (null != oid) {
	//			return oid.toString();
	//		}
	//		return null;

	// public int insert(DBCollection collection) {
	// DBObject document = new BasicDBObject();
	// for (int i = 0, n = columns.size(); i < n; i++) {
	// document.put(columns.get(i), values.get(i).getValue());
	// }
	// log(document);
	// WriteResult result = collection.insert(document, WriteConcern.ACKNOWLEDGED);
	// return result.getN();
	// }

	//		if (null == columns || 0 == columns.size()) {
	//			throw new SqlParseException("insert [column] is null");
	//		}
	//		if (null == values || 0 == values.size()) {
	//			throw new SqlParseException("insert [values] is null");
	//		}
}
