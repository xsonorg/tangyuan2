package org.xson.tangyuan.manager.conf;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;

public class MixedResourceReloader implements ResourceReloader {

	private Log                    log       = LogFactory.getLog(getClass());

	private List<ResourceReloader> reloaders = new ArrayList<ResourceReloader>();

	//	@Override
	//	public void reload(String resource, String context) throws Throwable {
	//		for (ResourceReloader reloader : reloaders) {
	//			try {
	//				reloader.reload(resource, context);
	//			} catch (Throwable e) {
	//				log.error("reload error.", e);
	//			}
	//		}
	//	}

	@Override
	public void reload(String resource) throws Throwable {
		for (ResourceReloader reloader : reloaders) {
			try {
				reloader.reload(resource);
			} catch (Throwable e) {
				log.error("reload error.", e);
			}
		}
	}

	public void addReloader(ResourceReloader reloader) {
		this.reloaders.add(reloader);
	}

}
