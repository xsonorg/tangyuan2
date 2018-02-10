package org.xson.tangyuan.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.bootstrap.StartupAndShutdownHandler;
import org.xson.tangyuan.bootstrap.StartupAndShutdownVo;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.PlaceholderResourceSupport;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanAssert;
import org.xson.tangyuan.util.TangYuanUtil;

public class XmlTangYuanBuilder implements XmlExtendBuilder {

	private Log							log					= LogFactory.getLog(getClass());
	private XPathParser					xPathParser			= null;

	private List<StartupAndShutdownVo>	startingBeforeList	= new ArrayList<StartupAndShutdownVo>();
	private List<StartupAndShutdownVo>	startingAfterList	= new ArrayList<StartupAndShutdownVo>();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		this.xPathParser = new XPathParser(inputStream);
		configurationElement(xPathParser.evalNode("/tangyuan-component"));
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {

		buildPlaceholderNodes(context.evalNodes("placeholder"));// 解析占位项
		buildConfigNodes(context.evalNodes("config-property"));// 解析配置项

		buildSSNodes(context.evalNodes("ss-aop"));
		executeSSAop(startingBeforeList, false);
		buildComponentNodes(context.evalNodes("component"));
		executeSSAop(startingAfterList, false);

		clean();
	}

	private void clean() {
		startingBeforeList.clear();
		startingAfterList.clear();
		startingBeforeList = null;
		startingAfterList = null;
	}

	private void executeSSAop(List<StartupAndShutdownVo> ssList, boolean ignoreException) throws Throwable {
		for (StartupAndShutdownVo ssVo : ssList) {
			try {
				ssVo.getHandler().execute(ssVo.getProperties());
				log.info("execute ss-aop class: " + ssVo.getClassName());
			} catch (Throwable e) {
				if (ignoreException) {
					log.error("execute ss-aop exception.", e);
				} else {
					throw e;
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void buildPlaceholderNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size > 1) {
			throw new XmlParseException("The <placeholder> node can have at most one.");
		}
		if (size == 0) {
			return;
		}

		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource"));
		TangYuanAssert.stringEmpty(resource, "in the <placeholder> tag, the 'resource' property can not be empty.");

		Properties props = null;
		if (resource.toLowerCase().startsWith("http://") || resource.toLowerCase().startsWith("https://")) {
			// TODO 这个要考虑加密
			props = Resources.getUrlAsProperties(resource);
		} else {
			props = Resources.getResourceAsProperties(resource);
		}
		TangYuanContainer.getInstance().getXmlGlobalContext().setPlaceholderMap((Map) props);
		log.info("add placeholder properties: " + resource);
	}

	private void buildConfigNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		// <config-property name="A" value="B" />
		Map<String, String> configMap = new HashMap<String, String>();
		for (XmlNodeWrapper context : contexts) {
			String name = StringUtils.trim(context.getStringAttribute("name"));
			String value = StringUtils.trim(context.getStringAttribute("value"));
			if (null == name || null == value) {
				throw new XmlParseException("<config-property> missing name or value");
			}
			configMap.put(name.toUpperCase(), value);
		}
		if (configMap.size() > 0) {
			PlaceholderResourceSupport.processMap(configMap, TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholderMap());
			TangYuanContainer.getInstance().config(configMap);
		}
	}

	/**
	 * build StartupAndShutdownHandler
	 */
	private void buildSSNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		int size = contexts.size();
		if (size == 0) {
			return;
		}

		List<StartupAndShutdownVo> closingBeforeList = new ArrayList<StartupAndShutdownVo>();
		List<StartupAndShutdownVo> closingAfterList = new ArrayList<StartupAndShutdownVo>();

		for (XmlNodeWrapper context : contexts) {
			String className = StringUtils.trim(context.getStringAttribute("class"));
			String type = StringUtils.trim(context.getStringAttribute("type"));

			TangYuanAssert.stringEmpty(className, "in the <ss-aop> tag, the 'class' property can not be empty.");
			TangYuanAssert.stringEmpty(type, "in the <ss-aop> tag, the 'type' property can not be empty.");

			Class<?> handlerClass = ClassUtils.forName(className);
			if (!StartupAndShutdownHandler.class.isAssignableFrom(handlerClass)) {
				throw new XmlParseException("ss-aop class must implement 'StartupAndShutdownHandler' interface: " + className);
			}

			Map<String, String> properties = new HashMap<String, String>();
			List<XmlNodeWrapper> innerContexts = context.evalNodes("property");
			for (XmlNodeWrapper innerContext : innerContexts) {
				String name = StringUtils.trim(innerContext.getStringAttribute("name"));
				String value = StringUtils.trim(innerContext.getStringAttribute("value"));
				TangYuanAssert.stringEmpty(name, "in the <ss-aop->property> tag, the 'name' property can not be empty.");
				TangYuanAssert.stringEmpty(value, "in the <ss-aop->property> tag, the 'value' property can not be empty.");
				properties.put(name, value);
			}
			PlaceholderResourceSupport.processMap(properties, TangYuanContainer.getInstance().getXmlGlobalContext().getPlaceholderMap());

			StartupAndShutdownHandler handler = (StartupAndShutdownHandler) TangYuanUtil.newInstance(handlerClass);
			if ("starting_before".equalsIgnoreCase(type)) {
				this.startingBeforeList.add(new StartupAndShutdownVo(handler, properties, className));
			} else if ("starting_after".equalsIgnoreCase(type)) {
				this.startingAfterList.add(new StartupAndShutdownVo(handler, properties, className));
			} else if ("closing_before".equalsIgnoreCase(type)) {
				closingBeforeList.add(new StartupAndShutdownVo(handler, properties, className));
			} else if ("closing_after".equalsIgnoreCase(type)) {
				closingAfterList.add(new StartupAndShutdownVo(handler, properties, className));
			} else {
				throw new XmlParseException(TangYuanUtil.format("Unsupported ss-aop type: {}", type));
			}
			log.info("add ss-aop class: " + className);
		}

		TangYuanContainer.getInstance().setClosingList(closingBeforeList, closingAfterList);
	}

	// private void buildInitHandler(List<XmlNodeWrapper> contexts) {
	// int size = contexts.size();
	// if (size > 1) {
	// throw new XmlParseException("The <init> node can have at most one.");
	// }
	// if (size == 0) {
	// return;
	// }
	//
	// XmlNodeWrapper xNode = contexts.get(0);
	// String className = StringUtils.trim(xNode.getStringAttribute("class"));
	// if (null == className) {
	// return;
	// }
	//
	// Class<?> handlerClass = ClassUtils.forName(className);
	// if (!TangYuanInitHandler.class.isAssignableFrom(handlerClass)) {
	// throw new XmlParseException("init class not implement the TangYuanInitHandler interface: " + className);
	// }
	//
	// log.info("execute init class: " + className);
	//
	// TangYuanInitHandler handler = (TangYuanInitHandler) TangYuanUtil.newInstance(handlerClass, true);
	// handler.execute();
	// }

	private void buildComponentNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		Map<String, String> resourceMap = new HashMap<String, String>();
		Map<String, String> typeMap = new HashMap<String, String>();
		Map<String, String> typeResourceMap = new HashMap<String, String>();
		for (XmlNodeWrapper context : contexts) {
			String resource = StringUtils.trim(context.getStringAttribute("resource"));
			String type = StringUtils.trim(context.getStringAttribute("type"));
			if (null == resource || null == type) {
				throw new XmlParseException("Startup component parameter is incomplete.");
			}
			if (resourceMap.containsKey(resource)) {
				throw new XmlParseException("Component resources are duplicated:" + resource);
			}
			if (typeMap.containsKey(type)) {
				throw new XmlParseException("Component type are duplicated:" + type);
			}
			preLoadComponent(type);
			resourceMap.put(resource, resource);
			typeMap.put(type, type);
			typeResourceMap.put(type.toUpperCase(), resource);
		}
		startupComponent(typeResourceMap);
		resourceMap = null;
		typeMap = null;
		typeResourceMap = null;
	}

	private void startupComponent(Map<String, String> typeResourceMap) throws Throwable {
		log.info("Ready to start all components.");
		// for (ComponentVo component : ComponentVo.sort(TangYuanContainer.getInstance().getComponents(), true)) {
		// component.getComponent().start(typeResourceMap.get(component.getType()));
		// }

		// int serviceComponent = 0;// TangYuan服务组件

		String type = "validate".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "rpc".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent("rpc-client").start(typeResourceMap.get(type));
		}

		type = "cache".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "sql".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
			// serviceComponent++;
		}

		type = "java".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
			// serviceComponent++;
		}

		type = "mongo".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
			// serviceComponent++;
		}

		type = "hbase".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "hive".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "es".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "mq".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent("mq-service").start(typeResourceMap.get(type));
			// serviceComponent++;
		}

		type = "aop".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
			ServiceActuator.openAop();
		}

		// TODO 在这里启动服务
		// 服务启动完毕:自动启动
		// ServiceActuator.start();

		// fix bug
		if (0 == TangYuanContainer.getInstance().getServicesKeySet().size()) {
			ServiceActuator.openOnlyProxyMode();
		}

		// fix
		type = "rpc".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent("rpc-server").start(typeResourceMap.get(type));
		}

		type = "mq".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent("mq-listener").start(typeResourceMap.get(type));
		}

		type = "timer".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "web".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}
	}

	private void preLoadComponent(String type) {
		try {
			if ("sql".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.sql.SqlComponent");
			} else if ("java".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.java.JavaComponent");
			} else if ("mongo".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.mongo.MongoComponent");
			}

			else if ("hbase".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.hbase.HBaseComponent");
			} else if ("hive".equalsIgnoreCase(type)) {
				// Class.forName("org.xson.tangyuan.mongo.MongoComponent");
			}

			else if ("es".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.es.EsComponent");
			} else if ("mq".equalsIgnoreCase(type)) {
				// Class.forName("org.xson.tangyuan.mq.MqComponent");
				Class.forName("org.xson.tangyuan.mq.MqContainer");
				Class.forName("org.xson.tangyuan.mq.MqServiceComponent");
				Class.forName("org.xson.tangyuan.mq.MqListenerComponent");
			} else if ("timer".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.timer.TimerComponent");
			} else if ("rpc".equalsIgnoreCase(type)) {
				// Class.forName("org.xson.tangyuan.rpc.RpcComponent");
				Class.forName("org.xson.tangyuan.rpc.RpcContainer");
				Class.forName("org.xson.tangyuan.rpc.RpcClientComponent");
				Class.forName("org.xson.tangyuan.rpc.RpcServerComponent");
			} else if ("web".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.web.WebComponent");
			} else if ("cache".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.cache.CacheComponent");
			} else if ("validate".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.validate.ValidateComponent");
			} else if ("aop".equalsIgnoreCase(type)) {
				Class.forName("org.xson.tangyuan.aop.AopComponent");
			} else {
				throw new XmlParseException("Unsupported component type: " + type);
			}
		} catch (ClassNotFoundException e) {
			throw new XmlParseException("missing component dependent jar: " + type, e);
		}
	}

}
