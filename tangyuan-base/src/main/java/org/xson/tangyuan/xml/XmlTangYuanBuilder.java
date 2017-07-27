package org.xson.tangyuan.xml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.bootstrap.TangYuanInitHandler;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;

public class XmlTangYuanBuilder implements XmlExtendBuilder {

	private Log			log			= LogFactory.getLog(getClass());
	private XPathParser	xPathParser	= null;

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		this.xPathParser = new XPathParser(inputStream);
		configurationElement(xPathParser.evalNode("/tangyuan-component"));
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildInitHandler(context.evalNodes("initBefore"));
		buildConfigNodes(context.evalNodes("config-property"));// 解析配置项目
		buildComponentNodes(context.evalNodes("component"));
		buildInitHandler(context.evalNodes("initAfter"));
	}

	private void buildConfigNodes(List<XmlNodeWrapper> contexts) {
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
			TangYuanContainer.getInstance().config(configMap);
		}
	}

	private void buildInitHandler(List<XmlNodeWrapper> contexts) {
		int size = contexts.size();
		if (size > 1) {
			throw new XmlParseException("The <init> node can have at most one.");
		}
		if (size == 0) {
			return;
		}

		XmlNodeWrapper xNode = contexts.get(0);
		String className = StringUtils.trim(xNode.getStringAttribute("class"));
		if (null == className) {
			return;
		}

		Class<?> handlerClass = ClassUtils.forName(className);
		if (!TangYuanInitHandler.class.isAssignableFrom(handlerClass)) {
			throw new XmlParseException("init class not implement the TangYuanInitHandler interface: " + className);
		}

		log.info("execute init class: " + className);

		TangYuanInitHandler handler = (TangYuanInitHandler) TangYuanUtil.newInstance(handlerClass, true);
		handler.execute();
	}

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
		}

		type = "java".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "mongo".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
		}

		type = "mq".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			// TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
			TangYuanContainer.getInstance().getComponent("mq-service").start(typeResourceMap.get(type));
		}

		// 服务启动完毕:自动启动
		// ServiceActuator.start();

		type = "aop".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
			ServiceActuator.openAop();
		}

		type = "mq".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent("mq-listener").start(typeResourceMap.get(type));
		}

		type = "timer".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
			if (0 == TangYuanContainer.getInstance().getServicesKeySet().size()) {
				ServiceActuator.openOnlyProxyMode();
			}
		}

		type = "web".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent(type).start(typeResourceMap.get(type));
			if (0 == TangYuanContainer.getInstance().getServicesKeySet().size()) {
				ServiceActuator.openOnlyProxyMode();
			}
		}

		// 最后启动
		type = "rpc".toUpperCase();
		if (typeResourceMap.containsKey(type)) {
			TangYuanContainer.getInstance().getComponent("rpc-server").start(typeResourceMap.get(type));
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
