package org.xson.tangyuan.mongo.executor.cmd.handler;

import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.mongo.executor.cmd.CommandHandler;
import org.xson.tangyuan.mongo.executor.cmd.CommandVo;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONExt;
import com.mongodb.util.JSONExtCallback;

public class FindHandler implements CommandHandler {

	@Override
	public Object process(Object target, CommandVo vo, WriteConcern writeConcern) {
		DBCollection collection = (DBCollection) target;

		List<String> parameters = vo.getParameters();

		if (parameters == null || parameters.size() == 0) {
			return collection.find();
		}

		if (parameters.size() == 1) {
			DBObject obj = (DBObject) JSON.parse(parameters.get(0), new JSONExtCallback());
			return collection.find(obj);
		}

		if (parameters.size() == 2) {
			DBObject obj = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback());
			DBObject obj1 = (DBObject) JSONExt.parse(parameters.get(1), new JSONExtCallback());
			return collection.find(obj, obj1);
		}

		throw new TangYuanException("Invalid action parameter size: " + vo.toString());
	}

}
