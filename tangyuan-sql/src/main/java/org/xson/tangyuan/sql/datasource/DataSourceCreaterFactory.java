package org.xson.tangyuan.sql.datasource;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.sql.datasource.DataSourceVo.ConnPoolType;
import org.xson.tangyuan.sql.datasource.dbcp.DBCPDataSourceCreater;

public class DataSourceCreaterFactory {

	public DataSourceCreater newInstance(ConnPoolType type) {
		if (ConnPoolType.DBCP == type) {
			return new DBCPDataSourceCreater();
		} else if (ConnPoolType.C3P0 == type) {
			throw new TangYuanException(TangYuanLang.get("sql.datasource.impl.unsupported", type.toString()));
		} else if (ConnPoolType.PROXOOL == type) {
			throw new TangYuanException(TangYuanLang.get("sql.datasource.impl.unsupported", type.toString()));
		} else if (ConnPoolType.DRUID == type) {
			throw new TangYuanException(TangYuanLang.get("sql.datasource.impl.unsupported", type.toString()));
		}
		//		else if (ConnPoolType.SHARE == type) {
		//			return new ShareDataSourceCreater();
		//		} 
		//		else if (ConnPoolType.JNDI == type) {
		//			return null;
		//		}
		return null;
	}

}
