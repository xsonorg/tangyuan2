package org.xson.tangyuan.mongo.executor.cmd.handler;

import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.mongo.executor.cmd.CommandHandler;
import org.xson.tangyuan.mongo.executor.cmd.CommandVo;

import com.mongodb.DBCursor;
import com.mongodb.WriteConcern;

public class CursorLimitHandler implements CommandHandler {

	@Override
	public Object process(Object target, CommandVo vo, WriteConcern writeConcern) {
		DBCursor cursor = (DBCursor) target;
		List<String> parameters = vo.getParameters();

		if (parameters == null) {
			throw new TangYuanException("Invalid action parameter size: " + vo.toString());
		}

		if (parameters.size() == 1) {
			int limit = Integer.parseInt(parameters.get(0));
			return cursor.limit(limit);
		}

		throw new TangYuanException("Invalid action parameter size: " + vo.toString());
	}

}
