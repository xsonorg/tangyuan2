package org.xson.tangyuan.sql.datasource.dbcp;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.sql.datasource.AbstractDataSource;

public class DBCPDataSource extends AbstractDataSource {

	private Log						log	= LogFactory.getLog(getClass());

	private final BasicDataSource	dataSource;

	protected DBCPDataSource(String creator, BasicDataSource dataSource, String logicDataSourceId, String realDataSourceId) {
		this.dataSource = dataSource;
		this.logicDataSourceId = logicDataSourceId;
		this.realDataSourceId = realDataSourceId;
	}

	@Override
	public Connection getConnection(String dsKey) throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public void recycleConnection(Connection connection) throws SQLException {
		try {
			connection.close();
		} catch (Exception e) {
			// log.error("recycleConnection exception", e);
			log.error(TangYuanLang.get("sql.datasource.conn.recycle.error"), e);
		}
	}

	@Override
	public void close(String creator) throws SQLException {
		dataSource.close();
	}

}
