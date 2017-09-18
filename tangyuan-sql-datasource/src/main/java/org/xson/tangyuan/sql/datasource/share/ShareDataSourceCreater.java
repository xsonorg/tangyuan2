package org.xson.tangyuan.sql.datasource.share;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.tangyuan.sql.datasource.AbstractDataSource;
import org.xson.tangyuan.sql.datasource.DataSourceCreater;
import org.xson.tangyuan.sql.datasource.DataSourceException;
import org.xson.tangyuan.sql.datasource.DataSourceGroupVo;
import org.xson.tangyuan.sql.datasource.DataSourceVo;
import org.xson.tangyuan.sql.datasource.ShareJdbcContainer;

public class ShareDataSourceCreater implements DataSourceCreater {

	// private Log log = LogFactory.getLog(ShareDataSourceCreater.class);
	private Logger log = LoggerFactory.getLogger(ShareDataSourceCreater.class);

	@Override
	public void newInstance(DataSourceVo dsVo, Map<String, DataSourceVo> logicMap, Map<String, AbstractDataSource> realMap) {
		// String jndiName = dsVo.getJndiName();
		String sharedUse = dsVo.getSharedUse();
		DataSourceVo shareVo = ShareJdbcContainer.getInstance().getDataSourceVo(sharedUse);
		if (null == shareVo) {
			throw new DataSourceException("Non-existent share datasource: " + sharedUse);
		}
		if (shareVo.isGroup()) {
			DataSourceGroupVo dsGroupVo = (DataSourceGroupVo) dsVo;
			for (int i = dsGroupVo.getStart(); i <= dsGroupVo.getEnd(); i++) {
				String readName = dsGroupVo.getId() + "." + i;
				String mappingName = sharedUse + "." + i;
				AbstractDataSource ds = ShareJdbcContainer.getInstance().getDataSource(mappingName);
				if (null == ds) {
					throw new DataSourceException("Non-existent data source: " + mappingName);
				}
				if (realMap.containsKey(readName)) {
					throw new DataSourceException("Duplicate DataSourceID: " + readName);
				}
				realMap.put(readName, ds);
				log.info("add datasource[group]: " + readName);
			}
		} else {
			AbstractDataSource ds = ShareJdbcContainer.getInstance().getDataSource(sharedUse);
			if (null == ds) {
				throw new DataSourceException("Non-existent data source: " + sharedUse);
			}
			if (realMap.containsKey(dsVo.getId())) {
				throw new DataSourceException("Duplicate DataSourceID: " + dsVo.getId());
			}
			realMap.put(dsVo.getId(), ds);
			log.info("add datasource: " + dsVo.getId());
		}
		logicMap.put(dsVo.getId(), dsVo);
	}

}
