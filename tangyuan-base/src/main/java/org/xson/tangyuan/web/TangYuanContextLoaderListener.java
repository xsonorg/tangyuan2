package org.xson.tangyuan.web;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;

public class TangYuanContextLoaderListener implements ServletContextListener {

	private Logger log = Logger.getLogger(getClass().getName());

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		try {
			String tangyuanResource = context.getInitParameter("tangyuan.resource");
			TangYuanContainer.getInstance().start(tangyuanResource);
		} catch (Throwable e) {
			log.log(Level.SEVERE, null, e);
			throw new TangYuanException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			TangYuanContainer.getInstance().stop(true);
		} catch (Throwable e) {
			log.log(Level.SEVERE, null, e);
		}
	}

	//			if (null != tangyuanResource) {
	//				TangYuanContainer.getInstance().start(tangyuanResource);
	//			}
	//	private Log log = LogFactory.getLog(getClass());
	//	static {
	//	}

}
