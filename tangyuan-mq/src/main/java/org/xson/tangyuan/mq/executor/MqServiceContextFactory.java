package org.xson.tangyuan.mq.executor;

import org.xson.tangyuan.executor.IServiceContext;
import org.xson.tangyuan.executor.ServiceContextFactory;

public class MqServiceContextFactory implements ServiceContextFactory {

	@Override
	public IServiceContext create() {
		return new MqServiceContext();
	}

}
