package org.xson.tangyuan.mongo.executor.cmd.handler;

import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.mongo.executor.cmd.CommandHandler;
import org.xson.tangyuan.mongo.executor.cmd.CommandVo;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSONExt;
import com.mongodb.util.JSONExtCallback;

public class UpdateHandler implements CommandHandler {

	@Override
	public Object process(Object target, CommandVo vo, WriteConcern writeConcern) {
		DBCollection collection = (DBCollection) target;

		List<String> parameters = vo.getParameters();

		if (parameters == null || parameters.size() == 0) {
			throw new TangYuanException("Invalid action parameter size: " + vo.toString());
		}

		if (parameters.size() == 2) {
			DBObject obj = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback());
			DBObject obj1 = (DBObject) JSONExt.parse(parameters.get(1), new JSONExtCallback());
			return collection.update(obj, obj1);
		}

		if (parameters.size() == 4) {
			DBObject obj = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback());
			DBObject obj1 = (DBObject) JSONExt.parse(parameters.get(1), new JSONExtCallback());
			boolean upsert = Boolean.parseBoolean(parameters.get(2));
			boolean multi = Boolean.parseBoolean(parameters.get(3));
			return collection.update(obj, obj1, upsert, multi, writeConcern);
		}

		throw new TangYuanException("Invalid action parameter size: " + vo.toString());
	}

}
