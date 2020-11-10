package org.xson.tangyuan.service.mongo.cmd.handler;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.service.mongo.cmd.CommandHandler;
import org.xson.tangyuan.service.mongo.cmd.CommandVo;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.util.CollectionUtils;

import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSONExt;
import com.mongodb.util.JSONExtCallback;

public class SaveHandler implements CommandHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Object process(Object target, CommandVo vo, WriteConcern writeConcern, Object arg) {
		DBCollection collection = (DBCollection) target;

		List<String> parameters = vo.getParameters();

		if (parameters == null) {
			throw new TangYuanException("Invalid action parameter size: " + vo.toString());
		}

		if (parameters.size() == 1) {
			//			DBObject obj = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback());
			//			collection.save(obj, writeConcern);
			//			return getId(obj);

			InsertReturn ir  = null;
			DBObject     obj = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback(arg));
			if (obj instanceof BasicDBList) {// 数组
				List<DBObject> documents = (List<DBObject>) obj;
				WriteResult    result    = collection.insert(documents, writeConcern);
				int            rowCount  = result.getN();
				List<String>   idList    = new ArrayList<String>();
				for (DBObject document : documents) {
					String _id = getId(document);
					if (null != _id) {
						idList.add(_id);
					}
				}
				if (CollectionUtils.isEmpty(idList)) {
					idList = null;
				}
				ir = new InsertReturn(rowCount, idList);
				return ir;
			} else {
				WriteResult result   = collection.insert(obj, writeConcern);
				int         rowCount = result.getN();
				String      _id      = getId(obj);
				ir = new InsertReturn(rowCount, _id);
				return ir;
			}

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
