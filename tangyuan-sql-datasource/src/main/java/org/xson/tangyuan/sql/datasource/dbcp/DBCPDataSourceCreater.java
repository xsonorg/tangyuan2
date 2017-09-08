package org.xson.tangyuan.sql.datasource.dbcp;

import java.sql.Connection;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.tangyuan.sql.datasource.AbstractDataSource;
import org.xson.tangyuan.sql.datasource.DataSourceCreater;
import org.xson.tangyuan.sql.datasource.DataSourceException;
import org.xson.tangyuan.sql.datasource.DataSourceGroupVo;
import org.xson.tangyuan.sql.datasource.DataSourceVo;
import org.xson.tangyuan.sql.datasource.util.DSPropertyUtil;

public class DBCPDataSourceCreater implements DataSourceCreater {

	// private Log log = LogFactory.getLog(DBCPDataSourceCreater.class);
	private Logger log = LoggerFactory.getLogger(DBCPDataSourceCreater.class);

	@Override
	public void newInstance(DataSourceVo dsVo, Map<String, DataSourceVo> logicMap, Map<String, AbstractDataSource> realMap) {
		if (dsVo.isGroup()) {
			DataSourceGroupVo dsGroupVo = (DataSourceGroupVo) dsVo;
			Map<String, String> properties = dsGroupVo.getProperties();
			for (int i = dsGroupVo.getStart(); i <= dsGroupVo.getEnd(); i++) {
				BasicDataSource dsPool = createDataSource(properties, i + "");
				String realId = dsGroupVo.getId() + "." + i;
				if (realMap.containsKey(realId)) {
					throw new DataSourceException("Duplicate DataSourceID: " + realId);
				}
				realMap.put(realId, new DBCPDataSource(dsVo.getCreator(), dsPool, dsGroupVo.getId(), realId));
				log.info("add datasource[group]: " + realId);
			}
		} else {
			Map<String, String> properties = dsVo.getProperties();
			BasicDataSource dsPool = createDataSource(properties, null);
			DBCPDataSource dbcpDataSource = new DBCPDataSource(dsVo.getCreator(), dsPool, dsVo.getId(), dsVo.getId());
			realMap.put(dsVo.getId(), dbcpDataSource);
			log.info("add datasource: " + dsVo.getId());
		}
		logicMap.put(dsVo.getId(), dsVo);
	}

	private BasicDataSource createDataSource(Map<String, String> properties, String urlPattern) {
		BasicDataSource dsPool = new BasicDataSource();

		dsPool.setDriverClassName(DSPropertyUtil.getPropertyStringValue("driver", properties, null, null, true));
		dsPool.setUsername(DSPropertyUtil.getPropertyStringValue("username", properties, null, null, true));
		dsPool.setPassword(DSPropertyUtil.getPropertyStringValue("password", properties, null, null, true));

		String url = DSPropertyUtil.getPropertyStringValue("url", properties, null, null, true);
		if (null != urlPattern) {
			url = DSPropertyUtil.replace(url, "{}", urlPattern);
		}
		dsPool.setUrl(url);
		dsPool.setPoolPreparedStatements(DSPropertyUtil.getPropertyBooleanValue("poolingStatements", properties, null, true, true)); // 开启池的prepared
																																		// 池功能
		dsPool.setRemoveAbandoned(DSPropertyUtil.getPropertyBooleanValue("removeAbandoned", properties, null, true, true));
		dsPool.setRemoveAbandonedTimeout(DSPropertyUtil.getPropertyIntegerValue("removeAbandonedTimeout", properties, null, 1000, true));
		dsPool.setLogAbandoned(DSPropertyUtil.getPropertyBooleanValue("logAbandoned", properties, null, true, true));

		dsPool.setInitialSize(DSPropertyUtil.getPropertyIntegerValue("initialSize", properties, null, 2, true));
		dsPool.setMaxActive(DSPropertyUtil.getPropertyIntegerValue("maxActive", properties, null, 8, true));
		dsPool.setMaxIdle(DSPropertyUtil.getPropertyIntegerValue("maxIdle", properties, null, 8, true));
		dsPool.setMinIdle(DSPropertyUtil.getPropertyIntegerValue("minIdle", properties, null, 0, true));
		dsPool.setMaxWait(DSPropertyUtil.getPropertyIntegerValue("maxWait", properties, null, 10000, true));

		dsPool.setTimeBetweenEvictionRunsMillis(DSPropertyUtil.getPropertyIntegerValue("timeBetweenEvictionRunsMillis", properties, null, -1, true));

		dsPool.setTestOnBorrow(DSPropertyUtil.getPropertyBooleanValue("testOnBorrow", properties, null, false, true));
		dsPool.setTestOnReturn(DSPropertyUtil.getPropertyBooleanValue("testOnReturn", properties, null, false, true));
		dsPool.setTestWhileIdle(DSPropertyUtil.getPropertyBooleanValue("testWhileIdle", properties, null, false, true));

		String validationQuery = DSPropertyUtil.getPropertyStringValue("validationQuery", properties, null, null, false);
		if (null != validationQuery) {
			dsPool.setValidationQuery(validationQuery);
		}
		int timeout = DSPropertyUtil.getPropertyIntegerValue("", properties, null, -1, false);
		if (timeout > -1) {
			dsPool.setValidationQueryTimeout(timeout);
		}

		dsPool.setNumTestsPerEvictionRun(DSPropertyUtil.getPropertyIntegerValue("numTestsPerEvictionRun", properties, null, 10, true));
		dsPool.setMinEvictableIdleTimeMillis(DSPropertyUtil.getPropertyIntegerValue("minEvictableIdleTimeMillis", properties, null, 60000, true));

		// mysql:select 1
		// oracle:select 1 from dual
		// sqlserver:select 1
		// jtds:select 1

		boolean openTest = DSPropertyUtil.getPropertyBooleanValue("openTest", properties, null, false, false);
		if (openTest) {
			try {
				Connection conn = dsPool.getConnection();
				conn.close();
				log.info("test open database success.");
			} catch (Exception e) {
				throw new DataSourceException("test open database error: " + e.getMessage(), e);
			}
		}
		return dsPool;
	}

}
