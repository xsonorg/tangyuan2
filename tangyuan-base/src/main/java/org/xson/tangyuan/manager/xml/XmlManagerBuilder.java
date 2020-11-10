package org.xson.tangyuan.manager.xml;

import java.util.List;

import org.xson.tangyuan.manager.ManagerLauncherContext;
import org.xson.tangyuan.manager.TangYuanManager;
import org.xson.tangyuan.manager.access.AccessControlManager;
import org.xson.tangyuan.manager.access.DefaultAccessControlManager;
import org.xson.tangyuan.manager.app.AppManager;
import org.xson.tangyuan.manager.app.DefaultAppManager;
import org.xson.tangyuan.manager.conf.ConfigManager;
import org.xson.tangyuan.manager.conf.DefaultConfigManager;
import org.xson.tangyuan.manager.conf.ResourceReloader;
import org.xson.tangyuan.manager.conf.ResourceReloaderVo;
import org.xson.tangyuan.manager.monitor.DefaultMonitorManager;
import org.xson.tangyuan.manager.monitor.MonitorManager;
import org.xson.tangyuan.manager.service.DefaultServiceManager;
import org.xson.tangyuan.manager.service.ServiceManager;
import org.xson.tangyuan.manager.trace.DefaultTraceManager;
import org.xson.tangyuan.manager.trace.TraceManager;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;

public class XmlManagerBuilder extends DefaultXmlComponentBuilder {

	private ManagerLauncherContext mlc                  = null;

	private AppManager             appManager           = null;
	private ServiceManager         serviceManager       = null;
	private AccessControlManager   accessControlManager = null;
	private MonitorManager         monitorManager       = null;
	private TraceManager           traceManager         = null;
	private ConfigManager          configManager        = null;

	private boolean                includeOne           = false;

	public void parse(XmlContext xmlContext, String resource) throws Throwable {

		log.info(lang("xml.start.parsing", resource));
		this.globalContext = (XmlGlobalContext) xmlContext;
		this.init(resource, "manager-component", false);
		// Init other
		this.mlc = XmlGlobalContext.getMlc();
		// build
		this.configurationElement();
		// process other
		if (includeOne) {
			TangYuanManager.init(mlc, appManager, serviceManager, accessControlManager, monitorManager, traceManager, configManager);
		}
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();
		this.mlc = null;
		this.appManager = null;
		this.serviceManager = null;
		this.accessControlManager = null;
		this.monitorManager = null;
		this.traceManager = null;
		this.configManager = null;
	}

	private void configurationElement() throws Throwable {
		if (null != this.mlc) {
			buildAppManagerNode(getMostOneNode(this.root, "app-manager", lang("xml.tag.mostone", "app-manager")));
		}
		if (null != this.mlc) {
			buildServiceManagerNode(getMostOneNode(this.root, "service-manager", lang("xml.tag.mostone", "service-manager")));
		}
		buildAccessManagerNode(getMostOneNode(this.root, "access-manager", lang("xml.tag.mostone", "access-manager")));
		if (null != this.mlc) {
			buildTraceManagerNode(getMostOneNode(this.root, "trace-manager", lang("xml.tag.mostone", "trace-manager")));
		}
		if (null != this.mlc) {
			buildMonitorManagerNode(getMostOneNode(this.root, "monitor-manager", lang("xml.tag.mostone", "monitor-manager")));
		}
		if (null != this.mlc) {
			buildConfigManagerNode(getMostOneNode(this.root, "config-manager", lang("xml.tag.mostone", "config-manager")));
		}
	}

	private void buildAppManagerNode(XmlNodeWrapper context) {
		boolean enabled = false;
		if (null != context) {
			enabled = getBoolFromAttr(context, "enabled", true);
		}
		if (enabled) {
			this.appManager = new DefaultAppManager(mlc);
			this.appManager.init(null);

			this.includeOne = true;
			log.info(lang("manager.enabled", "app"));
		}
	}

	private void buildServiceManagerNode(XmlNodeWrapper context) {
		boolean enabled = false;
		if (null != context) {
			enabled = getBoolFromAttr(context, "enabled", true);
		}
		if (enabled) {
			this.serviceManager = new DefaultServiceManager(mlc);
			this.serviceManager.init(null);
			this.includeOne = true;
			log.info(lang("manager.enabled", "service"));
		}
	}

	private void buildAccessManagerNode(XmlNodeWrapper context) throws Throwable {
		// <auth-manager enabled="true" resource="tangyuan-auth.xml" />
		boolean enabled = false;
		if (null != context) {
			enabled = getBoolFromAttr(context, "enabled", true);
		}
		if (enabled) {
			String tagName  = "access-manager";
			String resource = getStringFromAttr(context, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));

			this.accessControlManager = new DefaultAccessControlManager();
			this.accessControlManager.init(resource);

			XmlGlobalContext.addReloaderVo(new ResourceReloaderVo(resource, (ResourceReloader) this.accessControlManager));

			this.includeOne = true;
			log.info(lang("manager.enabled", "access"));
		}
	}

	private void buildTraceManagerNode(XmlNodeWrapper context) throws Throwable {
		//		<trace-manager enabled="true" resource="tangyuan-trace.properties" />
		boolean enabled = false;
		if (null != context) {
			enabled = getBoolFromAttr(context, "enabled", true);
		}
		if (enabled) {

			String tagName  = "trace-manager";
			String resource = getStringFromAttr(context, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));

			this.traceManager = new DefaultTraceManager(mlc);
			this.traceManager.init(resource);

			XmlGlobalContext.addReloaderVo(new ResourceReloaderVo(resource, (ResourceReloader) this.traceManager));

			this.includeOne = true;
			log.info(lang("manager.enabled", "trace"));
		}

	}

	private void buildMonitorManagerNode(XmlNodeWrapper context) throws Throwable {
		//		<monitor-manager enabled="true" resource="tangyuan-monitor.properties"/>
		boolean enabled = false;
		if (null != context) {
			enabled = getBoolFromAttr(context, "enabled", true);
		}
		if (enabled) {
			String tagName  = "monitor-manager";
			String resource = getStringFromAttr(context, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));

			this.monitorManager = new DefaultMonitorManager(mlc);
			this.monitorManager.init(resource);

			XmlGlobalContext.addReloaderVo(new ResourceReloaderVo(resource, (ResourceReloader) this.monitorManager));

			this.includeOne = true;
			log.info(lang("manager.enabled", "monitor"));
		}
	}

	private void buildConfigManagerNode(XmlNodeWrapper context) throws Throwable {
		boolean enabled = false;
		if (null != context) {
			enabled = getBoolFromAttr(context, "enabled", true);
		}
		if (enabled) {
			//			<reloader class="a.b.c.d" resource="tangyuan-log.properties" order="10"/>
			List<XmlNodeWrapper> reloaderList = context.evalNodes("reloader");
			for (XmlNodeWrapper xNode : reloaderList) {
				String                  tagName       = "reloader";
				String                  className     = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));
				String                  resource      = getStringFromAttr(xNode, "resource", lang("xml.tag.attribute.empty", "resource", tagName, this.resource));
				int                     order         = getIntFromAttr(xNode, "order", ResourceReloaderVo.defaultOrder);
				Class<ResourceReloader> reloaderClass = getClassForName(className, ResourceReloader.class,
						lang("xml.class.impl.interface", className, ResourceReloader.class.getName()));
				XmlGlobalContext.addReloaderVo(new ResourceReloaderVo(resource, reloaderClass, order));
			}
			this.includeOne = true;
			this.configManager = new DefaultConfigManager();
			log.info(lang("manager.enabled", "config"));

		}
	}

	//		buildAppManagerNode(getMostOneNode(this.root, "app-manager", lang("xml.tag.mostone", "app-manager")));
	//		buildServiceManagerNode(getMostOneNode(this.root, "service-manager", lang("xml.tag.mostone", "service-manager")));
	//		buildAccessManagerNode(getMostOneNode(this.root, "access-manager", lang("xml.tag.mostone", "access-manager")));
	//		buildTraceManagerNode(getMostOneNode(this.root, "trace-manager", lang("xml.tag.mostone", "trace-manager")));
	//		buildMonitorManagerNode(getMostOneNode(this.root, "monitor-manager", lang("xml.tag.mostone", "monitor-manager")));
	//		buildConfigManagerNode(getMostOneNode(this.root, "config-manager", lang("xml.tag.mostone", "config-manager")));

	//	if (!(null == this.appManager && null == this.serviceManager && null == this.accessControlManager && null == this.monitorManager
	//			&& null == this.traceManager && null == this.configManager)) {
	//		TangYuanManager tangYuanManager = new TangYuanManager(mlc, appManager, serviceManager, accessControlManager, monitorManager, traceManager,
	//				this.configManager);
	//		this.globalContext.setTangYuanManager(tangYuanManager);
	//	}
}
