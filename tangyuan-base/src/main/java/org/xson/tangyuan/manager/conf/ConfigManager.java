package org.xson.tangyuan.manager.conf;

import java.util.List;

/**
 * 配置中心管理器
 */
public interface ConfigManager {

	void init();

	void reload(String resource, String context);

	void postInit(List<ResourceReloaderVo> reloaderVoList) throws Throwable;

	//	void setEnabled();
	//	void registerReloader(ResourceReloaderVo rrVo);
}
