package org.xson.tangyuan.service.mongo;

import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.mongo.datasource.MongoSupport;
import org.xson.tangyuan.service.mongo.sql.DeleteVo;
import org.xson.tangyuan.service.mongo.sql.InsertVo;
import org.xson.tangyuan.service.mongo.sql.SelectVo;
import org.xson.tangyuan.service.mongo.sql.SqlParser;
import org.xson.tangyuan.service.mongo.sql.UpdateVo;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoActuator extends MongoBaseActuator {

	private SqlParser sqlParser = new SqlParser();

	public List<Map<String, Object>> selectAllMap(String dsKey, String sql, MappingVo resultMap, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		DBCursor cursor = selectVo.selectSet(collection, arg);
		return getResults(cursor, resultMap);
	}

	public List<XCO> selectAllXCO(String dsKey, String sql, MappingVo resultMap, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		DBCursor cursor = selectVo.selectSet(collection, arg);
		return getXCOResults(cursor, resultMap);
	}

	public Map<String, Object> selectOneMap(String dsKey, String sql, MappingVo resultMap, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		DBObject result = selectVo.selectOne(collection, arg);
		if (null != result) {
			return getResult(result, resultMap);
		}
		return null;
	}

	public XCO selectOneXCO(String dsKey, String sql, MappingVo resultMap, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		DBObject result = selectVo.selectOne(collection, arg);
		if (null != result) {
			return getXCOResult(result, resultMap);
		}
		return null;
	}

	public List<Map<String, Object>> selectAll(String dsKey, String sql, MappingVo resultMap, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		DBCursor cursor = selectVo.selectSet(collection, arg);
		return getResults(cursor, resultMap);
	}

	public Map<String, Object> selectOne(String dsKey, String sql, MappingVo resultMap, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		DBObject result = selectVo.selectOne(collection, arg);
		if (null != result) {
			return getResult(result, resultMap);
		}
		return null;
	}

	public Object selectVar(String dsKey, String sql, MappingVo resultMap, Object arg) {
		SelectVo selectVo = (SelectVo) sqlParser.parse(sql);
		DBCollection collection = MongoSupport.getCollection(dsKey, selectVo.getTable());
		// return selectVo.selectVar(collection); fix bug
		Object result = selectVo.selectVar(collection, arg);
		if (null == result) {
			return result;
		}
		if (result instanceof DBObject) {
			XCO one = getXCOResult((DBObject) result, resultMap);
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

}
