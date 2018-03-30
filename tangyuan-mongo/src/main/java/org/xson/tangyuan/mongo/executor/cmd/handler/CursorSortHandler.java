package org.xson.tangyuan.mongo.executor.cmd.handler;

import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.mongo.executor.cmd.CommandHandler;
import org.xson.tangyuan.mongo.executor.cmd.CommandVo;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONExtCallback;

public class CursorSortHandler implements CommandHandler {

	@Override
	public Object process(Object target, CommandVo vo, WriteConcern writeConcern) {
		DBCursor cursor = (DBCursor) target;
		List<String> parameters = vo.getParameters();

		if (parameters == null) {
			throw new TangYuanException("Invalid action parameter size: " + vo.toString());
		}

		if (parameters.size() == 1) {
			DBObject obj = (DBObject) JSON.parse(parameters.get(0), new JSONExtCallback());
			return cursor.sort(obj);
		}

		throw new TangYuanException("Invalid action parameter size: " + vo.toString());
	}

}
