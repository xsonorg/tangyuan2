package org.xson.tangyuan.sql.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 复杂的DataSourceManager
 */
public class MuiltDataSourceManager extends DataSourceManager {

	// private Log log = LogFactory.getLog(this.getClass());
	private Logger								log					= LoggerFactory.getLogger(MuiltDataSourceManager.class);

	/**
	 * 逻辑上的
	 */
	protected Map<String, DataSourceVo>			logicDataSourceMap	= null;

	/**
	 * 所有的
	 */
	protected Map<String, AbstractDataSource>	realDataSourceMap	= null;

	public MuiltDataSourceManager(String creator, Map<String, DataSourceVo> logicDataSourceMap, Map<String, AbstractDataSource> realDataSourceMap,
			String defaultDsKey) {
		this.creator = creator;
		this.logicDataSourceMap = logicDataSourceMap;
		this.realDataSourceMap = realDataSourceMap;
		this.defaultDsKey = defaultDsKey;
	}

	@Override
	public boolean isValidDsKey(String dsKey) {
		if (dsKey.indexOf(".") < 0) {
			return null != logicDataSourceMap.get(dsKey);
		}
		return null != realDataSourceMap.get(dsKey);
	}

	@Override
	public Connection getConnection(String dsKey) throws SQLException {
		AbstractDataSource dataSource = realDataSourceMap.get(dsKey);
		if (null == dataSource) {
			throw new SQLException("不存在的DataSource: " + dsKey);
		}
		return dataSource.getConnection(dsKey);
	}

	@Override
	public void recycleConnection(String dsKey, Connection connection) throws SQLException {
		AbstractDataSource dataSource = realDataSourceMap.get(dsKey);
		if (null == dataSource) {
			throw new SQLException("recycleConnection不存在的DataSource:" + dsKey);
		}
		log.info("recycle connection. dsKey[" + dsKey + "], hashCode[" + connection.hashCode() + "]");
		dataSource.recycleConnection(connection);
	}

	@Override
	public void close() throws SQLException {
		for (Map.Entry<String, AbstractDataSource> entry : realDataSourceMap.entrySet()) {
			try {
				entry.getValue().close(this.creator);
			} catch (Exception e) {
				//
			}
		}
	}
}
