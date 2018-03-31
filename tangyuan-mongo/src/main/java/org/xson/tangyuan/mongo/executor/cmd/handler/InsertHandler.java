package org.xson.tangyuan.mongo.executor.cmd.handler;

import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.mongo.executor.cmd.CommandHandler;
import org.xson.tangyuan.mongo.executor.cmd.CommandVo;

import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSONExt;
import com.mongodb.util.JSONExtCallback;

public class InsertHandler implements CommandHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Object process(Object target, CommandVo vo, WriteConcern writeConcern) {
		DBCollection collection = (DBCollection) target;

		List<String> parameters = vo.getParameters();

		if (parameters == null) {
			throw new TangYuanException("Invalid action parameter size: " + vo.toString());
		}

		if (parameters.size() == 1) {
			DBObject obj = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback());

			// WriteResult result = null;
			if (obj instanceof BasicDBList) {// 数组
				// result = collection.insert((List) obj, writeConcern);
				List<DBObject> documents = (List<DBObject>) obj;
				collection.insert(documents, writeConcern);
				int size = documents.size();
				String[] ids = new String[size];
				for (int i = 0; i < size; i++) {
					ids[i] = getId(documents.get(i));
				}
				return ids;
			} else {
				// result = collection.insert(obj, writeConcern);
				collection.insert(obj, writeConcern);
				return getId(obj);
			}
			// return result.getN();
		}

		throw new TangYuanException("Invalid action parameter size: " + vo.toString());
	}

	private String getId(DBObject document) {
		Object oid = document.get("_id");
		if (null != oid) {
			return oid.toString();
		}
		return null;
	}
}
