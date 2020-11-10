package org.xson.tangyuan.service.mongo.cmd.handler;

import java.util.List;

import org.bson.types.Code;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.service.mongo.cmd.CommandHandler;
import org.xson.tangyuan.service.mongo.cmd.CommandVo;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.GroupCommand;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSONExt;
import com.mongodb.util.JSONExtCallback;

public class GroupHandler implements CommandHandler {

	@Override
	public Object process(Object target, CommandVo vo, WriteConcern writeConcern, Object arg) {
		DBCollection collection = (DBCollection) target;

		List<String> parameters = vo.getParameters();

		if (parameters == null) {
			throw new TangYuanException("Invalid action parameter size: " + vo.toString());
		}

		if (parameters.size() == 1) {
			DBObject obj         = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback(arg));
			DBObject key         = (DBObject) obj.get("key");
			DBObject cond        = (DBObject) obj.get("cond");
			DBObject initial     = (DBObject) obj.get("initial");
			Object   reduceValue = obj.get("reduce");
			String   finalize    = (String) obj.get("finalize");

			String   reduce      = null;
			if (reduceValue instanceof Code) {
				reduce = ((Code) reduceValue).getCode();
			} else if (null != reduceValue) {
				reduce = reduceValue.toString();
			}

			GroupCommand groupCommand = new GroupCommand(collection, key, cond, initial, reduce, finalize);
			return collection.group(groupCommand);
		}

		if (parameters.size() == 4) {
			DBObject obj    = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback(arg));
			DBObject obj1   = (DBObject) JSONExt.parse(parameters.get(1), new JSONExtCallback(arg));
			DBObject obj2   = (DBObject) JSONExt.parse(parameters.get(2), new JSONExtCallback(arg));
			String   reduce = parameters.get(3);
			return collection.group(obj, obj1, obj2, reduce);
		}

		throw new TangYuanException("Invalid action parameter size: " + vo.toString());
	}

}
