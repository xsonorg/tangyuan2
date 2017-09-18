package org.xson.tangyuan.mongo.datasource;

import java.util.Map;
import java.util.Map.Entry;

/**
 * 共享数据源
 */
public final class ShareMongoContainer {

	private static ShareMongoContainer				instance	= new ShareMongoContainer();

	private Map<String, MongoDataSourceVo>			dsVoMap;
	private Map<String, AbstractMongoDataSource>	dsMap;

	private ShareMongoContainer() {
	}

	public static ShareMongoContainer getInstance() {
		return instance;
	}

	public MongoDataSourceVo getDataSourceVo(String sharedName) {
		if (null == dsVoMap) {
			return null;
		}
		return dsVoMap.get(sharedName);
	}

	public AbstractMongoDataSource getDataSource(String sharedName) {
		if (null == dsMap) {
			return null;
		}
		return dsMap.get(sharedName);
	}

	public void setDsMap(Map<String, AbstractMongoDataSource> dsMap) {
		this.dsMap = dsMap;
	}

	public void setDsVoMap(Map<String, MongoDataSourceVo> dsVoMap) {
		this.dsVoMap = dsVoMap;
	}

	public void close(String creator) {
		if (null == dsMap) {
			return;
		}
		for (Entry<String, AbstractMongoDataSource> entity : dsMap.entrySet()) {
			try {
				entity.getValue().close(creator);
			} catch (Throwable e) {
			}
		}
	}
}
