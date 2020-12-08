package org.xson.tangyuan.hive.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;

/**
 * 最简单的DataSourceManager
 */
public class SimpleDataSourceManager extends DataSourceManager {

	private Log                log              = LogFactory.getLog(getClass());

	private AbstractDataSource singleDataSource = null;

	public SimpleDataSourceManager(String creator, AbstractDataSource singleDataSource, String defaultDsKey) {
		this.creator = creator;
		this.singleDataSource = singleDataSource;
		this.defaultDsKey = defaultDsKey;
	}

	public Connection getConnection(String dsKey) throws SQLException {
		return singleDataSource.getConnection(dsKey);
	}

	public void recycleConnection(String dsKey, Connection connection) throws SQLException {
		// log.info("recycle connection. dsKey[" + dsKey + "], hashCode[" + connection.hashCode() + "]");
		log.info(TangYuanLang.get("sql.datasource.conn.recycle", dsKey, connection.hashCode()));
		singleDataSource.recycleConnection(connection);
	}

	@Override
	public void close() throws SQLException {
		singleDataSource.close(this.creator);
	}

}
