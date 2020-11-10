package org.xson.tangyuan.service.mongo.cmd;

import com.mongodb.WriteConcern;

public interface CommandHandler {

	public Object process(Object target, CommandVo vo, WriteConcern writeConcern, Object arg);

}
