package org.xson.tangyuan.validate.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.util.ResourceManager;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.validate.Checker;
import org.xson.tangyuan.validate.RuleDef;
import org.xson.tangyuan.validate.RuleGroup;
import org.xson.tangyuan.validate.ValidateComponent;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XMLConfigBuilder {

	private Log						log					= LogFactory.getLog(getClass());
	private XPathParser				parser				= null;
	private XmlNodeWrapper			root				= null;
	private Map<String, Checker>	customCheckerMap	= new HashMap<String, Checker>();

	public XMLConfigBuilder(InputStream inputStream) {
		this.parser = new XPathParser(inputStream);
		this.root = this.parser.evalNode("/validate-component");
	}

	public void parseNode() throws Throwable {
		buildConfigNodes(this.root.evalNodes("config-property"));
		buildCheckerNodes(this.root.evalNodes("checker"));
		buildPluginNodes(this.root.evalNodes("plugin"));
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
			ValidateComponent.getInstance().config(configMap);
		}
	}

	private void buildCheckerNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		for (XmlNodeWrapper context : contexts) {
			String id = StringUtils.trim(context.getStringAttribute("id"));
			String className = StringUtils.trim(context.getStringAttribute("class"));
			if (customCheckerMap.containsKey(id)) {
				throw new XmlParseException("Duplicate <checker> node");
			}
			Class<?> clazz = Class.forName(className);
			if (!clazz.isAssignableFrom(Checker.class)) {
				throw new XmlParseException("Custom checker must implement the interface Checker: " + className);
			}
			customCheckerMap.put(id, (Checker) clazz.newInstance());
			log.info("Add <checker> :" + className);
		}
	}

	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		// <plugin resource="xxx.xml" />
		List<String> resourceList = new ArrayList<String>();
		for (XmlNodeWrapper context : contexts) {
			String resource = StringUtils.trim(context.getStringAttribute("resource"));
			if (null == resource) {
				throw new XmlParseException("<plugin> missing resource");
			}
			resourceList.add(resource);
		}

		XMLRuleBuilder[] builders = new XMLRuleBuilder[resourceList.size()];
		Map<String, RuleDef> globleDefMap = new HashMap<String, RuleDef>();
		Map<String, RuleGroup> ruleGroupMap = new HashMap<String, RuleGroup>();
		int i = 0;
		for (String resource : resourceList) {
			log.info("Start parsing(ref): " + resource);
			//InputStream inputStream = Resources.getResourceAsStream(resource);
			InputStream inputStream = ResourceManager.getInputStream(resource, false);
			builders[i] = new XMLRuleBuilder(inputStream, globleDefMap, ruleGroupMap, customCheckerMap);
			builders[i].parseDefNode();
			i++;
		}
		i = 0;
		for (String resource : resourceList) {
			log.info("Start parsing(rule): " + resource);
			builders[i].parseRuleGroupNode();
			builders[i].clear();
			i++;
		}

		globleDefMap.clear();
		globleDefMap = null;
		ValidateComponent.getInstance().setRuleGroupsMap(ruleGroupMap);
		ruleGroupMap = null;
		customCheckerMap.clear();
		customCheckerMap = null;
	}
}
