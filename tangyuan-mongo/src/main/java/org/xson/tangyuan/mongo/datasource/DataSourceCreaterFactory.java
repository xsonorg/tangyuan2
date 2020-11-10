package org.xson.tangyuan.mongo.datasource;

import org.xson.tangyuan.mongo.datasource.impl.DefaultDataSourceCreater;

public class DataSourceCreaterFactory {

	public DataSourceCreater newInstance(String jndiName) {
		//		if (null == jndiName) {
		//			return new DefaultDataSourceCreater();
		//		} else {
		//			return new ShareDataSourceCreater();
		//		}
		return new DefaultDataSourceCreater();
	}

}
