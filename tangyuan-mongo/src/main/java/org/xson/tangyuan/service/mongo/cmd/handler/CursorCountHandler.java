package org.xson.tangyuan.service.mongo.cmd.handler;

import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.service.mongo.cmd.CommandHandler;
import org.xson.tangyuan.service.mongo.cmd.CommandVo;

import com.mongodb.DBCursor;
import com.mongodb.WriteConcern;

public class CursorCountHandler implements CommandHandler {

	@Override
	public Object process(Object target, CommandVo vo, WriteConcern writeConcern, Object arg) {
		DBCursor     cursor     = (DBCursor) target;
		List<String> parameters = vo.getParameters();

		if (parameters == null || parameters.size() == 0) {
			return cursor.count();
		}

		throw new TangYuanException("Invalid action parameter size: " + vo.toString());
	}

}
