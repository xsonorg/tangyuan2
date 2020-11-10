package org.xson.tangyuan.service.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.mapping.ColumnValueHandler;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.mongo.util.BSONUtil;

import com.mongodb.Cursor;
import com.mongodb.DBObject;

public class MongoBaseActuator {

	protected Map<String, Object> getResult(DBObject result, MappingVo resultMap) {
		Map<String, Object> row = new HashMap<String, Object>();
		if (null == resultMap) {
			for (String key : result.keySet()) {
				BSONUtil.setObjectValue(row, key, result.get(key));
			}
		} else {
			for (String key : result.keySet()) {
				//				BSONUtil.setObjectValue(row, resultMap.getProperty(key), result.get(key));
				String             property    = resultMap.getProperty(key);
				ColumnValueHandler cvh         = resultMap.getColumnValueHandler(key);
				Object             columnValue = result.get(key);
				if (null != cvh) {
					columnValue = cvh.process(key, columnValue);
				}
				BSONUtil.setObjectValue(row, property, columnValue);
			}
		}
		return row;
	}

	protected XCO getXCOResult(DBObject result, MappingVo resultMap) {
		XCO row = new XCO();
		if (null == resultMap) {
			for (String key : result.keySet()) {
				BSONUtil.setObjectValue(row, key, result.get(key));
			}
		} else {
			for (String key : result.keySet()) {
				//				BSONUtil.setObjectValue(row, resultMap.getProperty(key), result.get(key));
				String             property    = resultMap.getProperty(key);
				ColumnValueHandler cvh         = resultMap.getColumnValueHandler(key);
				Object             columnValue = result.get(key);
				if (null != cvh) {
					columnValue = cvh.process(key, columnValue);
				}
				BSONUtil.setObjectValue(row, property, columnValue);
			}
		}
		return row;
	}

	protected List<Map<String, Object>> getResults(Cursor cursor, MappingVo resultMap) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			if (null == resultMap) {
				while (cursor.hasNext()) {
					DBObject            bson = cursor.next();
					Map<String, Object> row  = new HashMap<String, Object>();
					for (String key : bson.keySet()) {
						BSONUtil.setObjectValue(row, key, bson.get(key));
					}
					list.add(row);
				}
			} else {
				while (cursor.hasNext()) {
					DBObject            bson = cursor.next();
					Map<String, Object> row  = new HashMap<String, Object>();
					for (String key : bson.keySet()) {
						//						BSONUtil.setObjectValue(row, resultMap.getProperty(key), bson.get(key));
						String             property    = resultMap.getProperty(key);
						ColumnValueHandler cvh         = resultMap.getColumnValueHandler(key);
						Object             columnValue = bson.get(key);
						if (null != cvh) {
							columnValue = cvh.process(key, columnValue);
						}
						BSONUtil.setObjectValue(row, property, columnValue);
					}
					list.add(row);
				}
			}
			return list;
		} finally {
			cursor.close();
		}
	}

	protected List<XCO> getXCOResults(Cursor cursor, MappingVo resultMap) {
		List<XCO> list = new ArrayList<XCO>();
		try {
			if (null == resultMap) {
				while (cursor.hasNext()) {
					DBObject bson = cursor.next();
					XCO      row  = new XCO();
					for (String key : bson.keySet()) {
						Object columnValue = bson.get(key);
						BSONUtil.setObjectValue(row, key, columnValue);
					}
					list.add(row);
				}
			} else {
				while (cursor.hasNext()) {
					DBObject bson = cursor.next();
					XCO      row  = new XCO();
					for (String key : bson.keySet()) {
						String             property    = resultMap.getProperty(key);
						ColumnValueHandler cvh         = resultMap.getColumnValueHandler(key);
						Object             columnValue = bson.get(key);
						if (null != cvh) {
							columnValue = cvh.process(key, columnValue);
						}
						BSONUtil.setObjectValue(row, property, columnValue);
						//						BSONUtil.setObjectValue(row, resultMap.getProperty(key), bson.get(key));
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
