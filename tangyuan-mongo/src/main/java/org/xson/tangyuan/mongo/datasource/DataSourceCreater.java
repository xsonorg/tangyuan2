package org.xson.tangyuan.mongo.datasource;

import java.util.Map;

public interface DataSourceCreater {

	void newInstance(MongoDataSourceVo dsVo, Map<String, MongoDataSourceVo> logicMap, Map<String, AbstractMongoDataSource> realMap);

}
