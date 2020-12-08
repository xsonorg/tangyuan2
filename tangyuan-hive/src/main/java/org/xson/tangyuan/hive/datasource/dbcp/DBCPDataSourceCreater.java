package org.xson.tangyuan.hive.datasource.dbcp;

import java.sql.Connection;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.xson.tangyuan.hive.datasource.AbstractDataSource;
import org.xson.tangyuan.hive.datasource.DataSourceCreater;
import org.xson.tangyuan.hive.datasource.DataSourceException;
import org.xson.tangyuan.hive.datasource.DataSourceGroupVo;
import org.xson.tangyuan.hive.datasource.DataSourceVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.util.PropertyUtils;

public class DBCPDataSourceCreater implements DataSourceCreater {

	private Log log = LogFactory.getLog(getClass());

	@Override
	public void newInstance(DataSourceVo dsVo, Map<String, DataSourceVo> logicMap, Map<String, AbstractDataSource> realMap) {
		if (dsVo.isGroup()) {
			DataSourceGroupVo   dsGroupVo  = (DataSourceGroupVo) dsVo;
			Map<String, String> properties = dsGroupVo.getProperties();
			for (int i = dsGroupVo.getStart(); i <= dsGroupVo.getEnd(); i++) {
				BasicDataSource dsPool = createDataSource(properties, i + "", dsVo.getId());
				String          realId = dsGroupVo.getId() + "." + i;
				if (realMap.containsKey(realId)) {
					throw new DataSourceException("Duplicate DataSourceID: " + realId);
				}
				realMap.put(realId, new DBCPDataSource(dsVo.getCreator(), dsPool, dsGroupVo.getId(), realId));
				// log.info("add datasource[group]: " + realId);
				// log.info(TangYuanLang.get("add.tag", "datasource", realId));
				log.infoLang("add.tag", "datasource", realId);
			}
		} else {
			Map<String, String> properties     = dsVo.getProperties();
			BasicDataSource     dsPool         = createDataSource(properties, null, dsVo.getId());
			DBCPDataSource      dbcpDataSource = new DBCPDataSource(dsVo.getCreator(), dsPool, dsVo.getId(), dsVo.getId());
			realMap.put(dsVo.getId(), dbcpDataSource);
			// log.info("add datasource: " + dsVo.getId());
			// log.info(TangYuanLang.get("add.tag", "datasource", dsVo.getId()));
			log.infoLang("add.tag", "datasource", dsVo.getId());
		}
		logicMap.put(dsVo.getId(), dsVo);
	}

	private String lang(String key, String dataSourceId) {
		// String content = TangYuanLang.get("properties.miss.datasource");
		// content = TangYuanUtil.format(content, dataSourceId, key);
		// return content;
		return TangYuanLang.get("sql.datasource.properties.miss", key, dataSourceId);
	}

	private BasicDataSource createDataSource(Map<String, String> p, String urlPattern, String dataSourceId) {
		BasicDataSource dsPool = new BasicDataSource();

		// dsPool.setDriverClassName(DSPropertyUtil.getPropertyStringValue("driver", properties, null, null, true));
		dsPool.setDriverClassName(PropertyUtils.getStringValue(p, "driver", null, lang("driver", dataSourceId), true));
		// dsPool.setUsername(DSPropertyUtil.getPropertyStringValue("username", properties, null, null, true));
		dsPool.setUsername(PropertyUtils.getStringValue(p, "username", null, lang("username", dataSourceId), true));
		// dsPool.setPassword(DSPropertyUtil.getPropertyStringValue("password", properties, null, null, true));
		dsPool.setPassword(PropertyUtils.getStringValue(p, "password", null, lang("password", dataSourceId), true));

		// String url = DSPropertyUtil.getPropertyStringValue("url", properties, null, null, true);
		String url = PropertyUtils.getStringValue(p, "url", null, lang("url", dataSourceId), true);
		if (null != urlPattern) {
			url = PropertyUtils.replace(url, "{}", urlPattern);
		}
		dsPool.setUrl(url);

		// dsPool.setPoolPreparedStatements(DSPropertyUtil.getPropertyBooleanValue("poolingStatements", properties, null, true, true)); //
		// 开启池的prepared
		dsPool.setPoolPreparedStatements(PropertyUtils.getBoolValue(p, "poolingStatements", true, true));

		// dsPool.setRemoveAbandoned(DSPropertyUtil.getPropertyBooleanValue("removeAbandoned", properties, null, true, true));
		dsPool.setRemoveAbandoned(PropertyUtils.getBoolValue(p, "removeAbandoned", true, true));
		// dsPool.setRemoveAbandonedTimeout(DSPropertyUtil.getPropertyIntegerValue("removeAbandonedTimeout", properties, null, 1000, true));
		dsPool.setRemoveAbandonedTimeout(PropertyUtils.getIntValue(p, "removeAbandonedTimeout", 1000, true));

		// dsPool.setLogAbandoned(DSPropertyUtil.getPropertyBooleanValue("logAbandoned", properties, null, true, true));
		dsPool.setLogAbandoned(PropertyUtils.getBoolValue(p, "logAbandoned", true, true));

		// dsPool.setInitialSize(DSPropertyUtil.getPropertyIntegerValue("initialSize", properties, null, 2, true));
		dsPool.setInitialSize(PropertyUtils.getIntValue(p, "initialSize", 2, true));
		// dsPool.setMaxActive(DSPropertyUtil.getPropertyIntegerValue("maxActive", properties, null, 8, true));
		dsPool.setMaxActive(PropertyUtils.getIntValue(p, "maxActive", 8, true));
		// dsPool.setMaxIdle(DSPropertyUtil.getPropertyIntegerValue("maxIdle", properties, null, 8, true));
		dsPool.setMaxIdle(PropertyUtils.getIntValue(p, "maxIdle", 8, true));
		// dsPool.setMinIdle(DSPropertyUtil.getPropertyIntegerValue("minIdle", properties, null, 0, true));
		dsPool.setMinIdle(PropertyUtils.getIntValue(p, "minIdle", 0, true));
		// dsPool.setMaxWait(DSPropertyUtil.getPropertyIntegerValue("maxWait", properties, null, 10000, true));
		dsPool.setMaxWait(PropertyUtils.getIntValue(p, "maxWait", 10000, true));

		// dsPool.setTimeBetweenEvictionRunsMillis(DSPropertyUtil.getPropertyIntegerValue("timeBetweenEvictionRunsMillis", properties, null, -1,
		// true));
		dsPool.setTimeBetweenEvictionRunsMillis(PropertyUtils.getIntValue(p, "timeBetweenEvictionRunsMillis", -1, true));

		// dsPool.setTestOnBorrow(DSPropertyUtil.getPropertyBooleanValue("testOnBorrow", properties, null, false, true));
		dsPool.setTestOnBorrow(PropertyUtils.getBoolValue(p, "testOnBorrow", false, true));
		// dsPool.setTestOnReturn(DSPropertyUtil.getPropertyBooleanValue("testOnReturn", properties, null, false, true));
		dsPool.setTestOnReturn(PropertyUtils.getBoolValue(p, "testOnReturn", false, true));
		// dsPool.setTestWhileIdle(DSPropertyUtil.getPropertyBooleanValue("testWhileIdle", properties, null, false, true));
		dsPool.setTestWhileIdle(PropertyUtils.getBoolValue(p, "testWhileIdle", false, true));

		// String validationQuery = DSPropertyUtil.getPropertyStringValue("validationQuery", properties, null, null, false);
		String validationQuery = PropertyUtils.getStringValue(p, "validationQuery", null, null, true);
		if (null != validationQuery) {
			dsPool.setValidationQuery(validationQuery);
		}
		// int timeout = DSPropertyUtil.getPropertyIntegerValue("", properties, null, -1, false);
		int timeout = PropertyUtils.getIntValue(p, "validationQueryTimeout", -1, true);
		if (timeout > -1) {
			dsPool.setValidationQueryTimeout(timeout);
		}

		// dsPool.setNumTestsPerEvictionRun(DSPropertyUtil.getPropertyIntegerValue("numTestsPerEvictionRun", properties, null, 10, true));
		dsPool.setNumTestsPerEvictionRun(PropertyUtils.getIntValue(p, "numTestsPerEvictionRun", 10, true));
		// dsPool.setMinEvictableIdleTimeMillis(DSPropertyUtil.getPropertyIntegerValue("minEvictableIdleTimeMillis", properties, null, 60000, true));
		dsPool.setMinEvictableIdleTimeMillis(PropertyUtils.getIntValue(p, "minEvictableIdleTimeMillis", 60000, true));

		// mysql:select 1
		// oracle:select 1 from dual
		// sqlserver:select 1
		// jtds:select 1

		// boolean openTest = DSPropertyUtil.getPropertyBooleanValue("openTest", properties, null, false, false);
		boolean openTest = PropertyUtils.getBoolValue(p, "openTest", false, true);
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
