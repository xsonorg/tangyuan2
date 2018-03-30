package org.xson.tangyuan.mongo.executor.cmd;

import com.mongodb.WriteConcern;

public interface CommandHandler {

	public Object process(Object target, CommandVo vo, WriteConcern writeConcern);

}
