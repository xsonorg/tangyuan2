package org.xson.tangyuan.mongo.executor.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.BSONObject;
import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.mongo.datasource.MongoSupport;
import org.xson.tangyuan.mongo.executor.cmd.handler.CountHandler;
import org.xson.tangyuan.mongo.executor.cmd.handler.CursorCountHandler;
import org.xson.tangyuan.mongo.executor.cmd.handler.CursorLimitHandler;
import org.xson.tangyuan.mongo.executor.cmd.handler.CursorSkipHandler;
import org.xson.tangyuan.mongo.executor.cmd.handler.CursorSortHandler;
import org.xson.tangyuan.mongo.executor.cmd.handler.FindAndModifyHandler;
import org.xson.tangyuan.mongo.executor.cmd.handler.FindHandler;
import org.xson.tangyuan.mongo.executor.cmd.handler.FindOneHandler;
import org.xson.tangyuan.mongo.executor.cmd.handler.GroupHandler;
import org.xson.tangyuan.mongo.executor.cmd.handler.InsertHandler;
import org.xson.tangyuan.mongo.executor.cmd.handler.RemoveHandler;
import org.xson.tangyuan.mongo.executor.cmd.handler.SaveHandler;
import org.xson.tangyuan.mongo.executor.cmd.handler.UpdateHandler;
import org.xson.tangyuan.mongo.util.BSONUtil;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class CommandActuator {

	private static Map<String, CommandHandler>	collectionHandlerMap	= new HashMap<String, CommandHandler>();
	private static Map<String, CommandHandler>	cursorHandlerMap		= new HashMap<String, CommandHandler>();
	private static Object						dbProxy					= new Object();

	private CommandParser						parser					= new CommandParser();

	static {
		// collectionHandlerMap.put("count", new CountHandler());
		// collectionHandlerMap.put("find", new FindHandler());
		// collectionHandlerMap.put("findAndModify", new FindAndModifyHandler());
		// collectionHandlerMap.put("findOne", new FindOneHandler());
		// collectionHandlerMap.put("group", new GroupHandler());
		// collectionHandlerMap.put("insert", new InsertHandler());
		// collectionHandlerMap.put("remove", new RemoveHandler());
		// collectionHandlerMap.put("save", new SaveHandler());
		// collectionHandlerMap.put("update", new UpdateHandler());
		//
		// cursorHandlerMap.put("count", new CursorCountHandler());
		// cursorHandlerMap.put("limit", new CursorLimitHandler());
		// cursorHandlerMap.put("skip", new CursorSkipHandler());
		// cursorHandlerMap.put("sort", new CursorSortHandler());

		collectionHandlerMap.put("count".toUpperCase(), new CountHandler());
		collectionHandlerMap.put("find".toUpperCase(), new FindHandler());
		collectionHandlerMap.put("findAndModify".toUpperCase(), new FindAndModifyHandler());
		collectionHandlerMap.put("findOne".toUpperCase(), new FindOneHandler());
		collectionHandlerMap.put("group".toUpperCase(), new GroupHandler());
		collectionHandlerMap.put("insert".toUpperCase(), new InsertHandler());
		collectionHandlerMap.put("remove".toUpperCase(), new RemoveHandler());
		collectionHandlerMap.put("save".toUpperCase(), new SaveHandler());
		collectionHandlerMap.put("update".toUpperCase(), new UpdateHandler());

		cursorHandlerMap.put("count".toUpperCase(), new CursorCountHandler());
		cursorHandlerMap.put("limit".toUpperCase(), new CursorLimitHandler());
		cursorHandlerMap.put("skip".toUpperCase(), new CursorSkipHandler());
		cursorHandlerMap.put("sort".toUpperCase(), new CursorSortHandler());
	}

	public Object execute(String context, String dsKey, Class<?> resultType, MappingVo resultMap) {
		List<CommandVo> voList = this.parser.parse(context);
		int size = voList.size();
		Object lastResult = null;
		for (int i = 0; i < size; i++) {
			CommandVo vo = voList.get(i);
			lastResult = execute0(lastResult, vo, dsKey);
		}
		lastResult = convert(lastResult, resultType, resultMap);
		return lastResult;
	}

	private Object execute0(Object target, CommandVo vo, String dsKey) {
		if (null == target) {// Root
			if ("db".equals(vo.getAction())) {
				// return this.db;
				return dbProxy;
			}
			throw new TangYuanException("Invalid action: " + vo.getAction());
		}
		// if (target instanceof DB) {// DB
		if (dbProxy == target) {// DB
			return MongoSupport.getCollection(dsKey, vo.getAction());
			// return ((DB) target).getCollection(vo.getAction());
		} else if (target instanceof DBCollection) {
			CommandHandler handler = collectionHandlerMap.get(vo.getAction().toUpperCase());
			if (null == handler) {
				throw new TangYuanException("Unsupported DBCollection action: " + vo.getAction());
			}
			return handler.process(target, vo, MongoSupport.getDefaultWriteConcern(dsKey));
		} else if (target instanceof DBCursor) {
			CommandHandler handler = cursorHandlerMap.get(vo.getAction().toUpperCase());
			if (null == handler) {
				throw new TangYuanException("Unsupported DBCursor action: " + vo.getAction());
			}
			return handler.process(target, vo, MongoSupport.getDefaultWriteConcern(dsKey));
		}
		throw new TangYuanException("Unknown target: " + target.getClass().getName());
	}

	private Object convert(Object mongoResult, Class<?> resultType, MappingVo resultMap) {
		if (null == mongoResult) {
			return null;
		} else if (mongoResult instanceof Integer) {
			return (Integer) mongoResult;
		} else if (mongoResult instanceof Long) {
			return (Long) mongoResult;
		} else if (mongoResult instanceof org.bson.types.ObjectId) {
			return ((org.bson.types.ObjectId) mongoResult).toString();
		} else if (mongoResult instanceof DBObject) {
			if (XCO.class == resultType || null == resultType) {
				return BSONUtil.BSONToXCO((BSONObject) mongoResult);
			} else {
				return BSONUtil.BSONToMap((BSONObject) mongoResult);
			}
		} else if (mongoResult instanceof DBCursor) {
			if (XCO.class == resultType || null == resultType) {
				return getXCOResults((DBCursor) mongoResult, resultMap);
			} else {
				return getResults((DBCursor) mongoResult, resultMap);
			}
		} else if (mongoResult instanceof WriteResult) {
			WriteResult result = (WriteResult) mongoResult;
			return result.getN();
		}
		// throw new TangYuanException("Command return unsupported data type: " + mongoResult.getClass().getName());
		return mongoResult;
	}

	private List<Map<String, Object>> getResults(DBCursor cursor, MappingVo resultMap) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			if (null == resultMap) {
				while (cursor.hasNext()) {
					DBObject bson = cursor.next();
					Map<String, Object> row = new HashMap<String, Object>();
					for (String key : bson.keySet()) {
						BSONUtil.setObjectValue(row, key, bson.get(key));
					}
					list.add(row);
				}
			} else {
				while (cursor.hasNext()) {
					DBObject bson = cursor.next();
					Map<String, Object> row = new HashMap<String, Object>();
					for (String key : bson.keySet()) {
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
						BSONUtil.setObjectValue(row, key, obj);
					}
					list.add(row);
				}
			} else {
				while (cursor.hasNext()) {
					DBObject bson = cursor.next();
					XCO row = new XCO();
					for (String key : bson.keySet()) {
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
