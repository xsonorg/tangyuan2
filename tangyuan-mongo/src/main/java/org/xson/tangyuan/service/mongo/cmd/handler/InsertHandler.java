package org.xson.tangyuan.service.mongo.cmd.handler;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.mongo.util.MongoUtil;
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

public class InsertHandler implements CommandHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Object process(Object target, CommandVo vo, WriteConcern writeConcern, Object arg) {
		DBCollection collection = (DBCollection) target;

		List<String> parameters = vo.getParameters();

		if (parameters == null) {
			throw new TangYuanException("Invalid action parameter size: " + vo.toString());
		}

		if (parameters.size() == 1) {

			InsertReturn ir = null;

			DBObject obj = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback(arg));

			// WriteResult result = null;
			if (obj instanceof BasicDBList) {// 数组
				// List<DBObject> documents = (List<DBObject>) obj;
				// collection.insert(documents, writeConcern);
				// int size = documents.size();
				// String[] ids = new String[size];
				// for (int i = 0; i < size; i++) {
				// ids[i] = getId(documents.get(i));
				// }
				// return ids;

				List<DBObject> documents = (List<DBObject>) obj;
				WriteResult result = collection.insert(documents, writeConcern);
				int rowCount = result.getN();
				List<String> idList = new ArrayList<String>();
				for (DBObject document : documents) {
					String _id = MongoUtil.getId(document);
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
				// collection.insert(obj, writeConcern);
				// return getId(obj);

				WriteResult result = collection.insert(obj, writeConcern);
				int rowCount = result.getN();
				String _id = MongoUtil.getId(obj);
				ir = new InsertReturn(rowCount, _id);
				return ir;
			}
			// return result.getN();
		}

		throw new TangYuanException("Invalid action parameter size: " + vo.toString());
	}

}
