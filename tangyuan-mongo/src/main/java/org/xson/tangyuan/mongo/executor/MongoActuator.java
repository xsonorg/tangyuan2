package org.xson.tangyuan.mongo.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.mongo.datasource.MongoSupport;
import org.xson.tangyuan.mongo.executor.sql.DeleteVo;
import org.xson.tangyuan.mongo.executor.sql.InsertVo;
import org.xson.tangyuan.mongo.executor.sql.SelectVo;
import org.xson.tangyuan.mongo.executor.sql.SqlParser;
import org.xson.tangyuan.mongo.executor.sql.UpdateVo;
import org.xson.tangyuan.mongo.util.BSONUtil;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoActuator {

	private SqlParser sqlParser = new SqlParser();

	public List<Map<String, Object>> selectAllMap(String dsKey, String sql, MappingVo resultMap, Integer fetchSize, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		DBCursor cursor = selectVo.selectSet(collection, arg);
		return getResults(cursor, resultMap);
	}

	public List<XCO> selectAllXCO(String dsKey, String sql, MappingVo resultMap, Integer fetchSize, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		DBCursor cursor = selectVo.selectSet(collection, arg);
		return getXCOResults(cursor, resultMap);
	}

	public Map<String, Object> selectOneMap(String dsKey, String sql, MappingVo resultMap, Integer fetchSize, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		DBObject result = selectVo.selectOne(collection, arg);
		if (null != result) {
			return getResults(result, resultMap);
		}
		return null;
	}

	public XCO selectOneXCO(String dsKey, String sql, MappingVo resultMap, Integer fetchSize, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		DBObject result = selectVo.selectOne(collection, arg);
		if (null != result) {
			return getXCOResults(result, resultMap);
		}
		return null;
	}

	public List<Map<String, Object>> selectAll(String dsKey, String sql, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		DBCursor cursor = selectVo.selectSet(collection, arg);
		return getResults(cursor, null);
	}

	public Map<String, Object> selectOne(String dsKey, String sql, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		DBObject result = selectVo.selectOne(collection, arg);
		if (null != result) {
			return getResults(result, null);
		}
		return null;
	}

	public Object selectVar(String dsKey, String sql, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		// return selectVo.selectVar(collection); fix bug
		Object result = selectVo.selectVar(collection, arg);
		if (null == result) {
			return result;
		}
		if (result instanceof DBObject) {
			XCO one = getXCOResults((DBObject) result, null);
			return selectVo.selectVarOneField(one);
		} else {
			return result;
		}
	}

	public Object insert(String dsKey, String sql, Object arg) {
		InsertVo insertVo = (InsertVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, insertVo.getTable());
		return insertVo.insert(collection, MongoSupport.getDefaultWriteConcern(dsKey), arg);
	}

	public int update(String dsKey, String sql, Object arg) {
		UpdateVo updateVo = (UpdateVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, updateVo.getTable());
		return updateVo.update(collection, MongoSupport.getDefaultWriteConcern(dsKey), arg);
	}

	public int delete(String dsKey, String sql, Object arg) {
		DeleteVo deleteVo = (DeleteVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, deleteVo.getTable());
		return deleteVo.delete(collection, MongoSupport.getDefaultWriteConcern(dsKey), arg);
	}

	private Map<String, Object> getResults(DBObject result, MappingVo resultMap) {
		Map<String, Object> row = new HashMap<String, Object>();
		if (null == resultMap) {
			for (String key : result.keySet()) {
				// row.put(key, result.get(key));
				BSONUtil.setObjectValue(row, key, result.get(key));
			}
		} else {
			for (String key : result.keySet()) {
				// row.put(resultMap.getProperty(key), result.get(key));
				BSONUtil.setObjectValue(row, resultMap.getProperty(key), result.get(key));
			}
		}
		return row;
	}

	private XCO getXCOResults(DBObject result, MappingVo resultMap) {
		XCO row = new XCO();
		if (null == resultMap) {
			for (String key : result.keySet()) {
				// row.setObjectValue(key, result.get(key));
				BSONUtil.setObjectValue(row, key, result.get(key));
			}
		} else {
			for (String key : result.keySet()) {
				// row.setObjectValue(resultMap.getProperty(key), result.get(key));
				BSONUtil.setObjectValue(row, resultMap.getProperty(key), result.get(key));
			}
		}
		return row;
	}

	private List<Map<String, Object>> getResults(DBCursor cursor, MappingVo resultMap) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			if (null == resultMap) {
				while (cursor.hasNext()) {
					DBObject bson = cursor.next();
					Map<String, Object> row = new HashMap<String, Object>();
					for (String key : bson.keySet()) {
						// row.put(key, bson.get(key));
						BSONUtil.setObjectValue(row, key, bson.get(key));
					}
					list.add(row);
				}
			} else {
				while (cursor.hasNext()) {
					DBObject bson = cursor.next();
					Map<String, Object> row = new HashMap<String, Object>();
					for (String key : bson.keySet()) {
						// row.put(resultMap.getProperty(key), bson.get(key));
						BSONUtil.setObjectValue(row, resultMap.getProperty(key), bson.get(key));
					}
					list.add(row);
				}
			}
			return list;
		} finally {
			cursor.close();
		}
	}

	private List<XCO> getXCOResults(DBCursor cursor, MappingVo resultMap) {
		List<XCO> list = new ArrayList<XCO>();
		try {
			if (null == resultMap) {
				while (cursor.hasNext()) {
					DBObject bson = cursor.next();
					XCO row = new XCO();
					for (String key : bson.keySet()) {
						Object obj = bson.get(key);
						// System.out.println(bson.get(key).getClass() + ":" + bson.get(key));
						// if (obj instanceof org.bson.types.ObjectId) {
						// row.setObjectValue(key, obj.toString());
						// } else {
						// row.setObjectValue(key, obj);
						// }
						BSONUtil.setObjectValue(row, key, obj);
					}
					list.add(row);
				}
			} else {
				while (cursor.hasNext()) {
					DBObject bson = cursor.next();
					XCO row = new XCO();
					for (String key : bson.keySet()) {
						// row.setObjectValue(resultMap.getProperty(key), bson.get(key));
						BSONUtil.setObjectValue(row, resultMap.getProperty(key), bson.get(key));
					}
					list.add(row);
				}
			}
			return list;
		} finally {
			cursor.close();
		}
	}

}
