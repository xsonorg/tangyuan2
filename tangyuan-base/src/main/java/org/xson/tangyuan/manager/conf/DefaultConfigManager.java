package org.xson.tangyuan.manager.conf;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;

/**
 * 配置中心管理器
 */
public class DefaultConfigManager implements ConfigManager {

	private Log                           log       = LogFactory.getLog(getClass());

	private Map<String, ResourceReloader> reloaders = null;

	@Override
	public void init() {
	}

	@Override
	public void reload(String resource, String context) {
		if (null == this.reloaders) {
			return;
		}
		ResourceReloader reloader = this.reloaders.get(resource);
		if (null != reloader) {
			try {
				reloader.reload(resource);
			} catch (Throwable e) {
				log.error(e);
			}
		}
	}

	@Override
	public void postInit(List<ResourceReloaderVo> reloaderVoList) throws Throwable {
		final Map<String, ResourceReloader> reloaders = new HashMap<>();
		Collections.sort(reloaderVoList);
		for (ResourceReloaderVo rrVo : reloaderVoList) {
			rrVo.initInstance();
			ResourceReloader existingReloader = reloaders.get(rrVo.getResource());
			if (null == existingReloader) {
				reloaders.put(rrVo.getResource(), rrVo.getInstance());
			} else {
				if (existingReloader instanceof MixedResourceReloader) {
					((MixedResourceReloader) existingReloader).addReloader(existingReloader);
				} else {
					MixedResourceReloader mixedResourceReloader = new MixedResourceReloader();
					mixedResourceReloader.addReloader(existingReloader);
					mixedResourceReloader.addReloader(rrVo.getInstance());
					reloaders.put(rrVo.getResource(), mixedResourceReloader);
				}
			}
		}

		if (!reloaders.isEmpty()) {
			this.reloaders = reloaders;
		}
	}

	//	@Override
	//	public void postInit() throws Throwable {
	//		if (!enabled) {
	//			rrList.clear();
	//			rrList = null;
	//			return;
	//		}
	//		reloaders = new HashMap<>();
	//		Collections.sort(this.rrList);
	//		for (ResourceReloaderVo rrVo : rrList) {
	//
	//			rrVo.initInstance();
	//
	//			ResourceReloader existingReloader = this.reloaders.get(rrVo.getResource());
	//			if (null == existingReloader) {
	//				this.reloaders.put(rrVo.getResource(), rrVo.getInstance());
	//			} else {
	//				if (existingReloader instanceof MixedResourceReloader) {
	//					((MixedResourceReloader) existingReloader).addReloader(existingReloader);
	//				} else {
	//					MixedResourceReloader mixedResourceReloader = new MixedResourceReloader();
	//					mixedResourceReloader.addReloader(existingReloader);
	//					mixedResourceReloader.addReloader(rrVo.getInstance());
	//					this.reloaders.put(rrVo.getResource(), mixedResourceReloader);
	//				}
	//			}
	//		}
	//		rrList.clear();
	//		rrList = null;
	//	}

	//	private boolean                       enabled   = false;
	//	private List<ResourceReloaderVo>      rrList    = new ArrayList<ResourceReloaderVo>();
	//	@Override
	//	public void setEnabled() {
	//		this.enabled = true;
	//	}
	//	@Override
	//	public void registerReloader(ResourceReloaderVo rrVo) {
	//		this.rrList.add(rrVo);
	//	}

	//	@Override
	//	public void reload(String resource, String context) {
	//		if (null == this.reloaders) {
	//			return;
	//		}
	//		ResourceReloader reloader = this.reloaders.get(resource);
	//		if (null != reloader) {
	//			try {
	//				reloader.reload(resource, context);
	//			} catch (Throwable e) {
	//				log.error(e);
	//			}
	//		}
	//	}
}
