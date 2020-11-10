package org.xson.tangyuan.mongo.datasource;

import java.util.Map;

import org.xson.tangyuan.log.TangYuanLang;

import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;

public class MuiltMongoDataSourceManager implements MongoDataSourceManager {

	protected String                               creator            = null;

	/**
	 * 逻辑上的
	 */
	protected Map<String, MongoDataSourceVo>       logicDataSourceMap = null;

	/**
	 * 所有的
	 */
	protected Map<String, AbstractMongoDataSource> realDataSourceMap  = null;

	public MuiltMongoDataSourceManager(String creator, Map<String, MongoDataSourceVo> logicDataSourceMap, Map<String, AbstractMongoDataSource> realDataSourceMap) {
		this.creator = creator;
		this.logicDataSourceMap = logicDataSourceMap;
		this.realDataSourceMap = realDataSourceMap;
	}

	@Override
	public boolean isValidDsKey(String dsKey) {
		if (dsKey.indexOf(".") < 0) {
			return null != logicDataSourceMap.get(dsKey);
		}
		return null != realDataSourceMap.get(dsKey);
	}

	@Override
	public DBCollection getCollection(String dsKey, String collection) {
		AbstractMongoDataSource dataSource = realDataSourceMap.get(dsKey);
		if (null == dataSource) {
			//			throw new DataSourceException("A non-existent mongo data source: " + dsKey);
			throw new DataSourceException(TangYuanLang.get("mongo.datasource.notexist", dsKey));
		}
		return dataSource.getCollection(collection);
	}

	@Override
	public void close() {
		for (Map.Entry<String, AbstractMongoDataSource> entry : realDataSourceMap.entrySet()) {
			entry.getValue().close(creator);
		}
	}

	@Override
	public WriteConcern getDefaultWriteConcern(String dsKey) {
		AbstractMongoDataSource dataSource = realDataSourceMap.get(dsKey);
		if (null == dataSource) {
			//			throw new DataSourceException("A non-existent mongo data source: " + dsKey);
			throw new DataSourceException(TangYuanLang.get("mongo.datasource.notexist", dsKey));
		}
		return dataSource.getDefaultWriteConcern();
	}

}
