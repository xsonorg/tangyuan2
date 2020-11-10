package org.xson.tangyuan.service.mongo.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.mongo.datasource.MongoSupport;
import org.xson.tangyuan.service.mongo.MongoBaseActuator;
import org.xson.tangyuan.service.mongo.cmd.handler.AggregateHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.CountHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.CursorCountHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.CursorLimitHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.CursorSkipHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.CursorSortHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.FindAndModifyHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.FindHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.FindOneHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.GroupHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.InsertHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.RemoveHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.SaveHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.UpdateHandler;
import org.xson.tangyuan.service.mongo.cmd.handler.UpdateManyHandler;

import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class CommandActuator extends MongoBaseActuator {

	private static Map<String, CommandHandler>	collectionHandlerMap	= new HashMap<String, CommandHandler>();
	private static Map<String, CommandHandler>	cursorHandlerMap		= new HashMap<String, CommandHandler>();
	private static Object						dbProxy					= new Object();

	private CommandParser						parser					= new CommandParser();

	static {
		collectionHandlerMap.put("count".toUpperCase(), new CountHandler());
		collectionHandlerMap.put("find".toUpperCase(), new FindHandler());
		collectionHandlerMap.put("findAndModify".toUpperCase(), new FindAndModifyHandler());
		collectionHandlerMap.put("findOne".toUpperCase(), new FindOneHandler());
		collectionHandlerMap.put("group".toUpperCase(), new GroupHandler());
		collectionHandlerMap.put("insert".toUpperCase(), new InsertHandler());
		collectionHandlerMap.put("remove".toUpperCase(), new RemoveHandler());
		collectionHandlerMap.put("save".toUpperCase(), new SaveHandler());
		collectionHandlerMap.put("update".toUpperCase(), new UpdateHandler());
		collectionHandlerMap.put("updateMany".toUpperCase(), new UpdateManyHandler());

		collectionHandlerMap.put("aggregate".toUpperCase(), new AggregateHandler());

		cursorHandlerMap.put("count".toUpperCase(), new CursorCountHandler());
		cursorHandlerMap.put("limit".toUpperCase(), new CursorLimitHandler());
		cursorHandlerMap.put("skip".toUpperCase(), new CursorSkipHandler());
		cursorHandlerMap.put("sort".toUpperCase(), new CursorSortHandler());
	}

	public Object execute(String context, String dsKey, Class<?> resultType, MappingVo resultMap, Object arg) {
		List<CommandVo> voList = this.parser.parse(context);
		int size = voList.size();
		Object lastResult = null;
		for (int i = 0; i < size; i++) {
			CommandVo vo = voList.get(i);
			lastResult = execute0(lastResult, vo, dsKey, arg);
		}
		lastResult = convert(lastResult, resultType, resultMap);
		return lastResult;
	}

	private Object execute0(Object target, CommandVo vo, String dsKey, Object arg) {
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
			return handler.process(target, vo, MongoSupport.getDefaultWriteConcern(dsKey), arg);
		} else if (target instanceof Cursor) {
			CommandHandler handler = cursorHandlerMap.get(vo.getAction().toUpperCase());
			if (null == handler) {
				throw new TangYuanException("Unsupported DBCursor action: " + vo.getAction());
			}
			return handler.process(target, vo, MongoSupport.getDefaultWriteConcern(dsKey), arg);
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
				// return BSONUtil.BSONToXCO((BSONObject) mongoResult);
				return getXCOResult((DBObject) mongoResult, resultMap);
			} else {
				// return BSONUtil.BSONToMap((BSONObject) mongoResult);
				return getResult((DBObject) mongoResult, resultMap);
			}
		} else if (mongoResult instanceof Cursor) {
			if (XCO.class == resultType || null == resultType) {
				return getXCOResults((Cursor) mongoResult, resultMap);
			} else {
				return getResults((Cursor) mongoResult, resultMap);
			}
		} else if (mongoResult instanceof WriteResult) {
			WriteResult result = (WriteResult) mongoResult;
			return result.getN();
		}
		// throw new TangYuanException("Command return unsupported data type: " + mongoResult.getClass().getName());
		return mongoResult;
	}

}
