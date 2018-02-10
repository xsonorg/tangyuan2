package org.xson.tangyuan.share;

import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.cache.ShareCacheContainer;
import org.xson.tangyuan.mongo.datasource.ShareMongoContainer;
import org.xson.tangyuan.share.xml.ShareTangYuanBuilder;
import org.xson.tangyuan.sql.datasource.ShareJdbcContainer;

public class ShareComponent {

	private static ShareComponent	instance		= new ShareComponent();

	private Log						log				= LogFactory.getLog(getClass());

	private String					systemName		= "tangyuan-share";

	private Map<String, String>		placeholderMap	= null;

	private ShareComponent() {
	}

	public static ShareComponent getInstance() {
		return instance;
	}

	public void start(String basePath, String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("share component starting, version: " + Version.getVersion());
		ShareTangYuanBuilder builder = new ShareTangYuanBuilder();
		builder.parse(basePath, resource);
		log.info("share component successfully.");
	}

	public void stop(boolean wait) {
		log.info("share component stopping...");
		try {
			ShareJdbcContainer.getInstance().close(systemName);
		} catch (Throwable e) {
			log.error(null, e);
		}
		try {
			ShareMongoContainer.getInstance().close(systemName);
		} catch (Throwable e) {
			log.error(null, e);
		}
		try {
			ShareCacheContainer.getInstance().close(systemName);
		} catch (Throwable e) {
			log.error(null, e);
		}
		// TODO mq
		log.info("share component stop successfully.");
	}

	public Map<String, String> getPlaceholderMap() {
		return placeholderMap;
	}

	public void setPlaceholderMap(Map<String, String> placeholderMap) {
		this.placeholderMap = placeholderMap;
	}

	public String getSystemName() {
		return systemName;
	}
}
