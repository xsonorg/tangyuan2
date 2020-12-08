package org.xson.tangyuan.hive.datasource;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.hive.datasource.DataSourceVo.ConnPoolType;
import org.xson.tangyuan.hive.datasource.dbcp.DBCPDataSourceCreater;
import org.xson.tangyuan.log.TangYuanLang;

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
		return null;
	}

}
