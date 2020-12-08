package org.xson.tangyuan.hive.datasource;

import java.util.Map;

public interface DataSourceCreater {

	void newInstance(DataSourceVo dsVo, Map<String, DataSourceVo> logicMap, Map<String, AbstractDataSource> realMap);

}
