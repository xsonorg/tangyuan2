package org.xson.tangyuan.mq.service.context;

import org.xson.tangyuan.service.context.ServiceContext;
import org.xson.tangyuan.service.context.ServiceContextFactory;

public class MqServiceContextFactory implements ServiceContextFactory {

	@Override
	public ServiceContext create() {
		return new MqServiceContext();
	}

}
