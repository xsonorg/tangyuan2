package org.xson.tangyuan.sql.datasource;

import java.util.Map;
import java.util.Map.Entry;

/**
 * 共享数据源
 */
public final class ShareJdbcContainer {

	private static ShareJdbcContainer		instance	= new ShareJdbcContainer();

	private Map<String, DataSourceVo>		dsVoMap;
	private Map<String, AbstractDataSource>	dsMap;

	private ShareJdbcContainer() {
	}

	public static ShareJdbcContainer getInstance() {
		return instance;
	}

	public DataSourceVo getDataSourceVo(String sharedName) {
		if (null == dsVoMap) {
			return null;
		}
		return dsVoMap.get(sharedName);
	}

	public AbstractDataSource getDataSource(String sharedName) {
		if (null == dsMap) {
			return null;
		}
		return dsMap.get(sharedName);
	}

	public void setDsMap(Map<String, AbstractDataSource> dsMap) {
		this.dsMap = dsMap;
	}

	public void setDsVoMap(Map<String, DataSourceVo> dsVoMap) {
		this.dsVoMap = dsVoMap;
	}

	public void close(String creator) {
		if (null == dsMap) {
			return;
		}
		for (Entry<String, AbstractDataSource> entity : dsMap.entrySet()) {
			try {
				entity.getValue().close(creator);
			} catch (Throwable e) {
			}
		}
	}
}
