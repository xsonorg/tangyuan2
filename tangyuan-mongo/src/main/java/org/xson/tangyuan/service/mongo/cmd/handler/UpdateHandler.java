package org.xson.tangyuan.service.mongo.cmd.handler;

import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.service.mongo.cmd.CommandHandler;
import org.xson.tangyuan.service.mongo.cmd.CommandVo;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSONExt;
import com.mongodb.util.JSONExtCallback;

public class UpdateHandler implements CommandHandler {

	@Override
	public Object process(Object target, CommandVo vo, WriteConcern writeConcern, Object arg) {
		DBCollection collection = (DBCollection) target;

		List<String> parameters = vo.getParameters();

		if (parameters == null || parameters.size() == 0) {
			throw new TangYuanException("Invalid action parameter size: " + vo.toString());
		}

		if (parameters.size() == 2) {
			DBObject obj  = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback(arg));
			DBObject obj1 = (DBObject) JSONExt.parse(parameters.get(1), new JSONExtCallback(arg));
			return collection.update(obj, obj1);
		}

		if (parameters.size() == 3) {
			DBObject     obj           = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback(arg));
			DBObject     obj1          = (DBObject) JSONExt.parse(parameters.get(1), new JSONExtCallback(arg));
			DBObject     obj2          = (DBObject) JSONExt.parse(parameters.get(2), new JSONExtCallback(arg));

			boolean      upsert        = false;
			boolean      multi         = false;
			WriteConcern aWriteConcern = writeConcern;
			if (obj2.containsField("upsert")) {
				upsert = (Boolean) obj2.get("upsert");
			}
			if (obj2.containsField("multi")) {
				multi = (Boolean) obj2.get("multi");
			}
			if (obj2.containsField("writeConcern")) {
				aWriteConcern = (WriteConcern) obj2.get("writeConcern");
			}

			return collection.update(obj, obj1, upsert, multi, aWriteConcern);
		}

		if (parameters.size() == 4) {
			DBObject obj    = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback(arg));
			DBObject obj1   = (DBObject) JSONExt.parse(parameters.get(1), new JSONExtCallback(arg));
			boolean  upsert = Boolean.parseBoolean(parameters.get(2));
			boolean  multi  = Boolean.parseBoolean(parameters.get(3));
			return collection.update(obj, obj1, upsert, multi, writeConcern);
		}

		throw new TangYuanException("Invalid action parameter size: " + vo.toString());
	}

}
