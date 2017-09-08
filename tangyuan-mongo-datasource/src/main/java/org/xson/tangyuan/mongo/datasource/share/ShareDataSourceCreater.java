package org.xson.tangyuan.mongo.datasource.share;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.tangyuan.mongo.datasource.AbstractMongoDataSource;
import org.xson.tangyuan.mongo.datasource.DataSourceCreater;
import org.xson.tangyuan.mongo.datasource.DataSourceException;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceGroupVo;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceVo;
import org.xson.tangyuan.mongo.datasource.ShareMongoContainer;

public class ShareDataSourceCreater implements DataSourceCreater {

	// private Log log = LogFactory.getLog(ShareDataSourceCreater.class);
	private Logger log = LoggerFactory.getLogger(ShareDataSourceCreater.class);

	@Override
	public void newInstance(MongoDataSourceVo dsVo, Map<String, MongoDataSourceVo> logicMap, Map<String, AbstractMongoDataSource> realMap) {
		String jndiName = dsVo.getJndiName();
		MongoDataSourceVo shareVo = ShareMongoContainer.getInstance().getDataSourceVo(jndiName);
		if (null == shareVo) {
			throw new DataSourceException("Non-existent share datasource: " + jndiName);
		}
		if (shareVo.isGroup()) {
			MongoDataSourceGroupVo dsGroupVo = (MongoDataSourceGroupVo) dsVo;
			for (int i = dsGroupVo.getStart(); i <= dsGroupVo.getEnd(); i++) {
				String readName = dsGroupVo.getId() + "." + i;
				String mappingName = jndiName + "." + i;
				AbstractMongoDataSource ds = ShareMongoContainer.getInstance().getDataSource(mappingName);
				if (null == ds) {
					throw new DataSourceException("Non-existent data source: " + mappingName);
				}
				if (realMap.containsKey(readName)) {
					throw new DataSourceException("Duplicate DataSourceID: " + readName);
				}
				realMap.put(readName, ds);
				log.info("add mongo datasource[group]: " + readName);
			}
		} else {
			AbstractMongoDataSource ds = ShareMongoContainer.getInstance().getDataSource(jndiName);
			if (null == ds) {
				throw new DataSourceException("Non-existent data source: " + jndiName);
			}
			if (realMap.containsKey(dsVo.getId())) {
				throw new DataSourceException("Duplicate DataSourceID: " + dsVo.getId());
			}
			realMap.put(dsVo.getId(), ds);
			log.info("add mongo datasource: " + dsVo.getId());
		}
		logicMap.put(dsVo.getId(), dsVo);
	}

}
