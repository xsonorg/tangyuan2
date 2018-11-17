package org.xson.tangyuan.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;

public class TangYuanContextLoaderListener implements ServletContextListener {

	private Log log = LogFactory.getLog(getClass());

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		try {
			String tangyuanResource = context.getInitParameter("tangyuan.resource");
			if (null != tangyuanResource) {
				TangYuanContainer.getInstance().start(tangyuanResource);
			}
		} catch (Throwable e) {
			log.error(null, e);
			throw new TangYuanException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			TangYuanContainer.getInstance().stop(true);
		} catch (Throwable e) {
			log.error(null, e);
		}
	}

}
