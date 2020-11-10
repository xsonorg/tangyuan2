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

public class FindAndModifyHandler implements CommandHandler {

	@Override
	public Object process(Object target, CommandVo vo, WriteConcern writeConcern, Object arg) {
		DBCollection collection = (DBCollection) target;

		List<String> parameters = vo.getParameters();

		if (parameters == null) {
			throw new TangYuanException("Invalid action parameter size: " + vo.toString());
		}

		if (parameters.size() == 1) {

			DBObject obj       = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback(arg));
			DBObject query     = (DBObject) obj.get("query");
			DBObject fields    = (DBObject) obj.get("fields");
			DBObject sort      = (DBObject) obj.get("sort");
			DBObject update    = (DBObject) obj.get("update");

			boolean  returnNew = false;
			try {
				returnNew = (Boolean) obj.get("new");
			} catch (Throwable e) {
			}
			boolean remove = false;
			try {
				remove = (Boolean) obj.get("remove");
			} catch (Throwable e) {
			}
			boolean upsert = false;
			try {
				upsert = (Boolean) obj.get("upsert");
			} catch (Throwable e) {
			}
			return collection.findAndModify(query, fields, sort, remove, update, returnNew, upsert);
		}

		throw new TangYuanException("Invalid action parameter size: " + vo.toString());
	}

}
