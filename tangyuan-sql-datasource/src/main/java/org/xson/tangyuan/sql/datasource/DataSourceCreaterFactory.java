package org.xson.tangyuan.sql.datasource;

import org.xson.tangyuan.sql.datasource.DataSourceVo.ConnPoolType;
import org.xson.tangyuan.sql.datasource.dbcp.DBCPDataSourceCreater;
import org.xson.tangyuan.sql.datasource.share.ShareDataSourceCreater;

public class DataSourceCreaterFactory {

	public DataSourceCreater newInstance(ConnPoolType type) {
		if (ConnPoolType.DBCP == type) {
			return new DBCPDataSourceCreater();
		} else if (ConnPoolType.C3P0 == type) {
			return null;
		} else if (ConnPoolType.PROXOOL == type) {
			return null;
		} else if (ConnPoolType.DRUID == type) {
			return null;
		} else if (ConnPoolType.SHARE == type) {
			return new ShareDataSourceCreater();
		} else if (ConnPoolType.JNDI == type) {
			return null;
		}
		return null;
	}

}
