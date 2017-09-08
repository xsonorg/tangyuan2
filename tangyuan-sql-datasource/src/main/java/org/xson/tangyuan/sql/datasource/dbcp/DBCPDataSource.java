package org.xson.tangyuan.sql.datasource.dbcp;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.tangyuan.sql.datasource.AbstractDataSource;

public class DBCPDataSource extends AbstractDataSource {

	// private static Log log = LogFactory.getLog(DBCPDataSource.class);
	private static Logger			log	= LoggerFactory.getLogger(DBCPDataSource.class);

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
			log.error("recycleConnection exception", e);
		}
	}

	@Override
	public void close(String creator) throws SQLException {
		if (this.creator != creator || !this.creator.equals(creator)) {
			return;
		}
		dataSource.close();
	}
}
