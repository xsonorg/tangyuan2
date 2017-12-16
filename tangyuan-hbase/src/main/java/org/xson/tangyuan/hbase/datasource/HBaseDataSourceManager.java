package org.xson.tangyuan.hbase.datasource;

import java.util.Map;
import java.util.Map.Entry;

public class HBaseDataSourceManager {

	private static HBaseDataSource				defaultDataSource	= null;
	private static Map<String, HBaseDataSource>	dataSourceMap		= null;

	public static void setDataSource(HBaseDataSource ds, Map<String, HBaseDataSource> dsMap) {
		if (null != ds) {
			defaultDataSource = ds;
		} else {
			dataSourceMap = dsMap;
		}
	}

	public static String getDefaultKey() {
		if (null != defaultDataSource) {
			return defaultDataSource.getId();
		}
		return null;
	}

	public static boolean isValidKey(String dsKey) {
		if (null != defaultDataSource) {
			return defaultDataSource.getId().equals(dsKey);
		}
		return dataSourceMap.containsKey(dsKey);
	}

	public static HBaseDataSource getDataSource(String dsKey) {
		if (null != defaultDataSource) {
			return defaultDataSource;
		}
		return dataSourceMap.get(dsKey);
	}

	public static void stop() {
		if (null != defaultDataSource) {
			defaultDataSource.stop();
		} else {
			for (Entry<String, HBaseDataSource> entry : dataSourceMap.entrySet()) {
				entry.getValue().stop();
			}
		}
	}
}
