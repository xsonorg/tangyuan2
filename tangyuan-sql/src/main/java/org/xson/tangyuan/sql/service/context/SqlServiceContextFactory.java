package org.xson.tangyuan.sql.service.context;

import org.xson.tangyuan.service.context.ServiceContext;
import org.xson.tangyuan.service.context.ServiceContextFactory;

public class SqlServiceContextFactory implements ServiceContextFactory {

	@Override
	public ServiceContext create() {
		return new SqlServiceContext();
	}

}
