package org.xson.tangyuan.mongo.datasource;

import org.xson.tangyuan.mongo.datasource.impl.DefaultDataSourceCreater;

public class DataSourceCreaterFactory {

	public DataSourceCreater newInstance(String jndiName) {
		return new DefaultDataSourceCreater();
	}

}
