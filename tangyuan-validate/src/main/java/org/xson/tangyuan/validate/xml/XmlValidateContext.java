package org.xson.tangyuan.validate.xml;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.validate.Checker;
import org.xson.tangyuan.validate.RuleDef;
import org.xson.tangyuan.validate.RuleGroup;
import org.xson.tangyuan.xml.DefaultXmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;

/**
 * 组件中的解析上下文
 */
public class XmlValidateContext extends DefaultXmlContext {

	private XmlGlobalContext       xmlContext       = null;
	private Map<String, Checker>   customCheckerMap = new HashMap<String, Checker>();
	private Map<String, RuleDef>   globleDefMap     = new HashMap<String, RuleDef>();
	private Map<String, RuleGroup> ruleGroupMap     = new HashMap<String, RuleGroup>();

	public XmlGlobalContext getXmlContext() {
		return xmlContext;
	}

	public void setXmlContext(XmlGlobalContext xmlContext) {
		this.xmlContext = xmlContext;
	}

	public Map<String, RuleGroup> getRuleGroupMap() {
		return ruleGroupMap;
	}

	public Map<String, Checker> getCustomCheckerMap() {
		return customCheckerMap;
	}

	public Map<String, RuleDef> getGlobleDefMap() {
		return globleDefMap;
	}

	@Override
	public void clean() {
		// clear
		this.globleDefMap.clear();
		this.customCheckerMap.clear();
		// set null
		this.xmlContext = null;
		this.customCheckerMap = null;
		this.globleDefMap = null;
		this.ruleGroupMap = null;
	}
}
