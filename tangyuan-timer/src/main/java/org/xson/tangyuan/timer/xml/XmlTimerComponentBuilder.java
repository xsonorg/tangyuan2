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

	private XmlTimerContext	componentContext	= null;

	private List<TimerVo>	timerList			= new ArrayList<TimerVo>();

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

		// buildSingleLiveNode(this.root.evalNodes("singleLiveController"));
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
			String className = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));
			TimerComponentSingleLiveController controller = getInstanceForName(className, TimerComponentSingleLiveController.class,
					lang("xml.class.impl.interface", className, TimerComponentSingleLiveController.class.getName()));
			Map<String, Object> properties = buildPropertyMap(xNode.evalNodes("property"), tagName + ".property", false);
			SingleLiveControllerVo slcVo = new SingleLiveControllerVo(controller, properties);
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

			int start = getIntFromAttr(xNode, "start", 0);
			int end = getIntFromAttr(xNode, "end", lang("xml.tag.attribute.empty", "end", tagName, this.resource));
			String indexPattern = getStringFromAttr(xNode, "index", lang("xml.tag.attribute.empty", "index", tagName, this.resource));

			if (!checkVar(indexPattern)) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid.should", "index", "{xxx}", tagName, this.resource));
			}
			String indexPatternVal = getRealVal(indexPattern);

			List<XmlNodeWrapper> timerNodeList = xNode.evalNodes("timer");
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
		String tagName = "timer";

		String scheduled = getStringFromAttr(xNode, "scheduled", lang("xml.tag.attribute.empty", "scheduled", tagName, this.resource));
		String service = getStringFromAttr(xNode, "service");
		String desc = getStringFromAttr(xNode, "desc");
		boolean sync = getBoolFromAttr(xNode, "sync", true);
		String custom = getStringFromAttr(xNode, "custom");

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
		TimerVo vo = new TimerVo(scheduled, service, sync, false, desc, customJob, propertiesMap);
		timerList.add(vo);
	}

	private Map<String, Object> parseProperty(List<XmlNodeWrapper> contexts, Object obj) {
		String tagName = "timer.property";
		Map<String, Object> properties = new HashMap<String, Object>();
		for (XmlNodeWrapper xNode : contexts) {
			String name = getStringFromAttr(xNode, "name", lang("xml.tag.attribute.empty", "name", tagName, this.resource));
			String value = getStringFromAttr(xNode, "value", lang("xml.tag.attribute.empty", "value", tagName, this.resource));
			// properties.put(name, TimerUtil.parseValue(value, obj));
			properties.put(name, parseValue(value, obj));
		}
		if (properties.isEmpty()) {
			return null;
		}
		return properties;
	}

	private Object parseValue(String val, Object obj) {
		// if (0 == val.length()) {
		// throw new OgnlException("Illegal value: " + val);
		// }
		// if ("null".equalsIgnoreCase(val)) {
		// return null;
		// } else
		if (val.length() > 1 && val.startsWith("'") && val.endsWith("'")) {// fix bug
			return val.substring(1, val.length() - 1);
		} else if (NumberUtils.isNumber(val)) {
			// return getNumber(val);
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
			// Variable variable = new GAParserWarper().parse(getRealVal(val));
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

}
