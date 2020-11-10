package org.xson.tangyuan.timer.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.timer.TimerComponent;
import org.xson.tangyuan.timer.TimerComponentSingleLiveController;
import org.xson.tangyuan.timer.job.CustomJob;
import org.xson.tangyuan.timer.xml.vo.SingleLiveControllerVo;
import org.xson.tangyuan.timer.xml.vo.TimerVo;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.util.NumberUtils;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlTimerComponentBuilder extends DefaultXmlComponentBuilder {

	private XmlTimerContext componentContext = null;

	private List<TimerVo>   timerList        = new ArrayList<TimerVo>();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.componentContext = (XmlTimerContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "timer-component", true);
		this.configurationElement();
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();
		this.componentContext = null;
		this.timerList = null;
	}

	private void configurationElement() throws Throwable {
		buildConfigNode(this.root.evalNodes("config-property"), TimerComponent.getInstance());

		//		buildSingleLiveNode(this.root.evalNodes("singleLiveController"));
		buildSingleLiveNode(this.root.evalNodes("single-live"));

		buildTimerNode(this.root.evalNodes("timer"));
		buildTimerGroupNode(this.root.evalNodes("timerGroup"));
		this.componentContext.setTimerList(timerList);
	}

	private void buildSingleLiveNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "single-live";
		if (contexts.size() > 1) {
			throw new XmlParseException(lang("xml.tag.mostone", tagName));
		}
		for (XmlNodeWrapper xNode : contexts) {
			String                             className  = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));
			TimerComponentSingleLiveController controller = getInstanceForName(className, TimerComponentSingleLiveController.class,
					lang("xml.class.impl.interface", className, TimerComponentSingleLiveController.class.getName()));
			Map<String, Object>                properties = buildPropertyMap(xNode.evalNodes("property"), tagName + ".property", false);
			SingleLiveControllerVo             slcVo      = new SingleLiveControllerVo(controller, properties);
			this.componentContext.setSlcVo(slcVo);
			log.info(lang("add.tag.class"), tagName, className);
		}
	}

	private void buildTimerNode(List<XmlNodeWrapper> contexts) throws Throwable {
		for (XmlNodeWrapper xNode : contexts) {
			parseTimerNode(xNode, null, null, 0);
		}
	}

	private void buildTimerGroupNode(List<XmlNodeWrapper> contexts) throws Throwable {
		// <timerGroup start="0" end="10" index="{i}">
		String tagName = "timerGroup";
		for (XmlNodeWrapper xNode : contexts) {

			int    start        = getIntFromAttr(xNode, "start", 0);
			int    end          = getIntFromAttr(xNode, "end", lang("xml.tag.attribute.empty", "end", tagName, this.resource));
			String indexPattern = getStringFromAttr(xNode, "index", lang("xml.tag.attribute.empty", "index", tagName, this.resource));

			if (!checkVar(indexPattern)) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid.should", "index", "{xxx}", tagName, this.resource));
			}
			String               indexPatternVal = getRealVal(indexPattern);

			List<XmlNodeWrapper> timerNodeList   = xNode.evalNodes("timer");
			if (CollectionUtils.isEmpty(timerNodeList)) {
				throw new XmlParseException(lang("xml.tag.content.empty", tagName, this.resource));
			}

			XCO obj = new XCO();
			for (int i = start; i < end; i++) {
				obj.setIntegerValue(indexPatternVal, i);
				for (XmlNodeWrapper timer : timerNodeList) {
					parseTimerNode(timer, obj, indexPattern, i);
				}
			}
		}
	}

	private void parseTimerNode(XmlNodeWrapper xNode, Object obj, String indexPattern, int indexValue) throws Throwable {
		String  tagName   = "timer";

		String  scheduled = getStringFromAttr(xNode, "scheduled", lang("xml.tag.attribute.empty", "scheduled", tagName, this.resource));
		String  service   = getStringFromAttr(xNode, "service");
		String  desc      = getStringFromAttr(xNode, "desc");
		boolean sync      = getBoolFromAttr(xNode, "sync", true);
		String  custom    = getStringFromAttr(xNode, "custom");

		service = repeatIndex(service, indexPattern, indexValue);
		desc = repeatIndex(desc, indexPattern, indexValue);

		CustomJob customJob = null;
		if (null != custom) {
			customJob = getInstanceForName(custom, CustomJob.class, lang("xml.class.impl.interface", custom, CustomJob.class.getName()));
		}
		if (null == customJob && null == service) {
			throw new XmlParseException(lang("xml.tag.attribute.empty", "service|custom", tagName, this.resource));
		}

		Map<String, Object> propertiesMap = parseProperty(xNode.evalNodes("property"), obj);
		TimerVo             vo            = new TimerVo(scheduled, service, sync, false, desc, customJob, propertiesMap);
		timerList.add(vo);
	}

	private Map<String, Object> parseProperty(List<XmlNodeWrapper> contexts, Object obj) {
		String              tagName    = "timer.property";
		Map<String, Object> properties = new HashMap<String, Object>();
		for (XmlNodeWrapper xNode : contexts) {
			String name  = getStringFromAttr(xNode, "name", lang("xml.tag.attribute.empty", "name", tagName, this.resource));
			String value = getStringFromAttr(xNode, "value", lang("xml.tag.attribute.empty", "value", tagName, this.resource));
			//properties.put(name, TimerUtil.parseValue(value, obj));
			properties.put(name, parseValue(value, obj));
		}
		if (properties.isEmpty()) {
			return null;
		}
		return properties;
	}

	private Object parseValue(String val, Object obj) {
		//		if (0 == val.length()) {
		//			throw new OgnlException("Illegal value: " + val);
		//		}
		//		if ("null".equalsIgnoreCase(val)) {
		//			return null;
		//		} else 
		if (val.length() > 1 && val.startsWith("'") && val.endsWith("'")) {// fix bug
			return val.substring(1, val.length() - 1);
		} else if (NumberUtils.isNumber(val)) {
			//			return getNumber(val);
			return NumberUtils.parseNumber(val);
		} else if ("now()".equalsIgnoreCase(val)) {
			return java.util.Date.class;
		} else if ("date()".equalsIgnoreCase(val)) {
			return java.sql.Date.class;
		} else if ("time()".equalsIgnoreCase(val)) {
			return java.sql.Time.class;
		} else if ("timestamp()".equalsIgnoreCase(val)) {
			return java.sql.Timestamp.class;
		}

		if (checkVar(val)) {
			//			Variable variable = new GAParserWarper().parse(getRealVal(val));
			Variable variable = parseVariableUseGA(val);
			return variable.getValue(obj);
		}

		return val;
	}

	private String repeatIndex(String str, String indexPattern, int indexValue) {
		if (null == str || null == indexPattern) {
			return str;
		}
		indexPattern = indexPattern.replaceFirst("\\{", "\\\\{");
		indexPattern = indexPattern.replaceFirst("\\}", "\\\\}");
		return str.replaceAll(indexPattern, indexValue + "");
	}

	////////////////////////////////////////////////////////////////////////////////////

	//	private void buildHaNodes(List<XmlNodeWrapper> contexts) throws Throwable {
	//		// 高可用控制器
	//		int size = contexts.size();
	//		if (size == 0) {
	//			return;
	//		}
	//
	//		isTrue(size > 1, "'ha-config' tags can only be allowed to exist at most one");
	//
	//		XmlNodeWrapper xNode   = contexts.get(0);
	//		String         clazz   = trimEmpty(xNode.getStringAttribute("class"));
	//		Class<?>       haClass = ClassUtils.forName(clazz);
	//		this.haController = (TimerHAController) TangYuanUtil.newInstance(haClass);
	//
	//		Map<String, String>  propertiesMap = new HashMap<String, String>();
	//		List<XmlNodeWrapper> properties    = xNode.evalNodes("property");
	//		for (XmlNodeWrapper propertyNode : properties) {
	//			propertiesMap.put(trimEmpty(propertyNode.getStringAttribute("name")), trimEmpty(propertyNode.getStringAttribute("value")));
	//		}
	//		if (propertiesMap.size() > 0) {
	//			// this.haProperties = TimerUtil.parseProperties(propertiesMap); 
	//		}
	//	}

	//	private List<XmlNodeWrapper> getChildTimerNode(XmlNodeWrapper groupNode) {
	//		List<XmlNodeWrapper> nodeList = groupNode.evalNodes("timer");
	//		int                  size     = nodeList.size();
	//		if (0 == size) {
	//			throw new XmlParseException("missing subtag 'timer' in tag 'timerGroup'");
	//		}
	//		return nodeList;
	//	}

	//	private void parseTimer(XmlNodeWrapper node, String var, int varValue) {
	//
	//		String  scheduled = trimEmpty(node.getStringAttribute("scheduled"));
	//		String  service   = trimEmpty(node.getStringAttribute("service"));
	//		String  desc      = trimEmpty(node.getStringAttribute("desc"));
	//		boolean sync      = attributeToBoolean(node.getStringAttribute("sync"), true);
	//
	//		service = getTimerAttrValue(service, var, varValue);
	//		desc = getTimerAttrValue(desc, var, varValue);
	//
	//		CustomJob customJob = null;
	//		String    custom    = trimEmpty(node.getStringAttribute("custom"));
	//		if (null != custom) {
	//			Class<?> clazz = ClassUtils.forName(custom);
	//			if (!CustomJob.class.isAssignableFrom(clazz)) {
	//				throw new XmlParseException("User-defined JOB must implement org.xson.timer.client.CustomJob: " + custom);
	//			}
	//			customJob = (CustomJob) TangYuanUtil.newInstance(clazz);
	//		}
	//
	//		if (null == customJob && null == service) {
	//			throw new XmlParseException("'customJob' and 'service' can not be empty");
	//		}
	//
	//		// 自定义参数
	//		Map<String, String>  propertiesMap = new HashMap<String, String>();
	//		Map<String, Object>  preProperties = null;
	//		List<XmlNodeWrapper> properties    = node.evalNodes("property");
	//		for (XmlNodeWrapper propertyNode : properties) {
	//			String propName  = trimEmpty(propertyNode.getStringAttribute("name"));
	//			String propValue = trimEmpty(propertyNode.getStringAttribute("value"));
	//			propertiesMap.put(propName, propValue);
	//		}
	//		if (propertiesMap.size() > 0) {
	//			preProperties = TimerUtil.parseProperties(propertiesMap);
	//		} else {
	//			propertiesMap = null;
	//		}
	//
	//		TimerVo vo = new TimerVo(scheduled, service, sync, false, desc, customJob, propertiesMap, preProperties);
	//		timerList.add(vo);
	//	}

	//	private String getTimerAttrValue(String src, String var, int varValue) {
	//		if (null == src || null == var) {
	//			return src;
	//		}
	//		var = var.replaceFirst("\\{", "\\\\{");
	//		var = var.replaceFirst("\\}", "\\\\}");
	//		return src.replaceFirst(var, varValue + "");
	//	}

	//	@Override
	//	public void parse(XmlContext xmlContext, String resource) throws Throwable {
	//		info("*** Start parsing: {}", resource);
	//		init(resource, "timer-component", true);
	//		this.componentContext = (XmlTimerContext) xmlContext;
	//		this.globalContext = this.componentContext.getXmlContext();
	//		parse0();
	//		clean();
	//	}

	//		String  scheduled = trimEmpty(node.getStringAttribute("scheduled"));
	//		String  service   = trimEmpty(node.getStringAttribute("service"));
	//		String  desc      = trimEmpty(node.getStringAttribute("desc"));
	//		boolean sync      = attributeToBoolean(node.getStringAttribute("sync"), true);
	//		String    custom    = trimEmpty(node.getStringAttribute("custom"));
	//		CustomJob customJob = null;
	//		String    custom    = trimEmpty(node.getStringAttribute("custom"));
	//		if (null != custom) {
	//			Class<?> clazz = ClassUtils.forName(custom);
	//			if (!CustomJob.class.isAssignableFrom(clazz)) {
	//				throw new XmlParseException("User-defined JOB must implement org.xson.timer.client.CustomJob: " + custom);
	//			}
	//			customJob = (CustomJob) TangYuanUtil.newInstance(clazz);
	//		}
	//		if (null == customJob && null == service) {
	//			throw new XmlParseException("'customJob' and 'service' can not be empty");
	//		}
	// 自定义参数
	//		Map<String, String>  propertiesMap = new HashMap<String, String>();
	//		Map<String, Object>  preProperties = null;
	//		List<XmlNodeWrapper> properties    = node.evalNodes("property");
	//		for (XmlNodeWrapper propertyNode : properties) {
	//			String propName  = trimEmpty(propertyNode.getStringAttribute("name"));
	//			String propValue = trimEmpty(propertyNode.getStringAttribute("value"));
	//			propertiesMap.put(propName, propValue);
	//		}
	//		if (propertiesMap.size() > 0) {
	//			preProperties = TimerUtil.parseProperties(propertiesMap);
	//		} else {
	//			propertiesMap = null;
	//		}
	//TimerVo vo = new TimerVo(scheduled, service, sync, false, desc, customJob, propertiesMap, preProperties);

	//	private void buildTimerGroupNode(List<XmlNodeWrapper> nodeList) throws Throwable {
	//		// <timerGroup start="0" end="10" index="{i}">
	//		for (XmlNodeWrapper node : nodeList) {
	//			String _start   = trimEmpty(node.getStringAttribute("start"));
	//			String _end     = trimEmpty(node.getStringAttribute("end"));
	//			String indexVar = trimEmpty(node.getStringAttribute("index"));
	//
	//			isNull(_start, "the attribute[{}] in the tag[{}] cannot be empty", "start", "timerGroup");
	//			isNull(_end, "the attribute[{}] in the tag[{}] cannot be empty", "end", "timerGroup");
	//			isNull(indexVar, "the attribute[{}] in the tag[{}] cannot be empty", "index", "timerGroup");
	//			isVar(indexVar, "invalid attribute '{}' in tag '{}'", "index", "timerGroup");
	//
	//			int start = Integer.parseInt(_start);
	//			int end   = Integer.parseInt(_end);
	//			for (int i = start; i < end; i++) {
	//				for (XmlNodeWrapper timerNode : getChildTimerNode(node)) {
	//					parseTimer(timerNode, indexVar, i);
	//				}
	//			}
	//		}
	//	}

	//			String _start   = trimEmpty(node.getStringAttribute("start"));
	//			String _end     = trimEmpty(node.getStringAttribute("end"));
	//			String indexVar = trimEmpty(node.getStringAttribute("index"));
	//isNull(_start, "the attribute[{}] in the tag[{}] cannot be empty", "start", "timerGroup");
	//isNull(_end, "the attribute[{}] in the tag[{}] cannot be empty", "end", "timerGroup");
	//isNull(indexVar, "the attribute[{}] in the tag[{}] cannot be empty", "index", "timerGroup");
	//isVar(indexVar, "invalid attribute '{}' in tag '{}'", "index", "timerGroup");
	//int start = Integer.parseInt(_start);
	//int end   = Integer.parseInt(_end);
	//			for (int i = start; i < end; i++) {
	//				for (XmlNodeWrapper timerNode : getChildTimerNode(node)) {
	//					parseTimer(timerNode, indexVar, i);
	//				}
	//			}

	//		XmlNodeWrapper xNode   = contexts.get(0);
	//		String         clazz   = trimEmpty(xNode.getStringAttribute("class"));
	//		Class<?>       haClass = ClassUtils.forName(clazz);
	//		this.haController = (TimerHAController) TangYuanUtil.newInstance(haClass);
	//
	//		Map<String, String>  propertiesMap = new HashMap<String, String>();
	//		List<XmlNodeWrapper> properties    = xNode.evalNodes("property");
	//		for (XmlNodeWrapper propertyNode : properties) {
	//			propertiesMap.put(trimEmpty(propertyNode.getStringAttribute("name")), trimEmpty(propertyNode.getStringAttribute("value")));
	//		}
}
