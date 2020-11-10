package org.xson.tangyuan.manager.access.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.manager.access.acr.AppRuleItemVo;
import org.xson.tangyuan.manager.access.acr.AppRuleItemVo.MatchType;
import org.xson.tangyuan.manager.access.acr.AppRuleItemVo.RuleType;
import org.xson.tangyuan.manager.access.acr.DefaultACRManager;
import org.xson.tangyuan.manager.access.acr.ServiceRuleItemVo;
import org.xson.tangyuan.manager.access.acr.ServiceRuleItemVo.ServiceMatchType;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlAccessBuilder extends DefaultXmlComponentBuilder {

	private List<AppRuleItemVo>     appItems     = null;
	private List<ServiceRuleItemVo> serviceItems = null;

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.globalContext = (XmlGlobalContext) xmlContext;
		this.init(resource, "access-manager", false);
		// Init other
		// build
		this.configurationElement();
		// process other
	}

	public void parse(XmlContext xmlContext, String resource, InputStream inputStream) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.globalContext = (XmlGlobalContext) xmlContext;
		// Init
		this.resource = resource;
		this.xPathParser = new XPathParser(inputStream);
		this.root = this.xPathParser.evalNode("/" + "access-manager");
		inputStream.close();
		// Init other
		// build
		this.configurationElement();
		// process other
	}

	@Override
	public void clean() {
		super.clean();
		this.appItems = null;
		this.serviceItems = null;
	}

	public DefaultACRManager getDefaultACRManager() {
		DefaultACRManager defaultACRManager = new DefaultACRManager();
		defaultACRManager.init(appItems, serviceItems);
		return defaultACRManager;
	}

	private void configurationElement() throws Throwable {
		buildAppRuleNode(this.root.evalNodes("app-rule"));
		buildServiceRuleNode(this.root.evalNodes("service-rule"));
	}

	private void buildAppRuleNode(List<XmlNodeWrapper> contexts) {
		if (CollectionUtils.isEmpty(contexts)) {
			return;
		}
		// <app-rule type="黑名单:不允许的域名" value="*.aixbx.com" />
		String              tagName  = "app-rule";
		List<AppRuleItemVo> appItems = new ArrayList<AppRuleItemVo>();
		for (XmlNodeWrapper xNode : contexts) {
			String   type     = getStringFromAttr(xNode, "type", lang("xml.tag.attribute.empty", "type", tagName, this.resource));
			String   value    = getStringFromAttr(xNode, "value", lang("xml.tag.attribute.empty", "value", tagName, this.resource));

			RuleType ruleType = RuleType.parse(type);
			if (null == ruleType) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid", "type", tagName, this.resource));
			}
			MatchType matchType = MatchType.parse(value);
			if (null == matchType) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid", "value", tagName, this.resource));
			}
			AppRuleItemVo ariVo = new AppRuleItemVo(ruleType, value, matchType);
			appItems.add(ariVo);
			log.info(lang("manager.xml.app.rule.add", type, value));
		}
		if (appItems.size() > 0) {
			this.appItems = appItems;
		}
	}

	private void buildServiceRuleNode(List<XmlNodeWrapper> contexts) {
		if (CollectionUtils.isEmpty(contexts)) {
			return;
		}
		String                  tagName      = "service-rule";
		List<ServiceRuleItemVo> serviceItems = new ArrayList<>();
		for (XmlNodeWrapper xNode : contexts) {
			String   service  = getStringFromAttr(xNode, "service", lang("xml.tag.attribute.empty", "service", tagName, this.resource));
			String   type     = getStringFromAttr(xNode, "type", lang("xml.tag.attribute.empty", "type", tagName, this.resource));
			String   value    = getStringFromAttr(xNode, "value", lang("xml.tag.attribute.empty", "value", tagName, this.resource));

			RuleType ruleType = RuleType.parse(type);
			if (null == ruleType) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid", "type", tagName, this.resource));
			}
			MatchType matchType = MatchType.parse(value);
			if (null == matchType) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid", "value", tagName, this.resource));
			}
			ServiceMatchType serviceMatchType = ServiceMatchType.parse(service);
			if (null == serviceMatchType) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid", "service", tagName, this.resource));
			}

			ServiceRuleItemVo sriVo = new ServiceRuleItemVo(ruleType, value, matchType, service, serviceMatchType);
			serviceItems.add(sriVo);
			log.info(lang("manager.xml.sevice.rule.add", service, type, value));
		}
		if (serviceItems.size() > 0) {
			this.serviceItems = serviceItems;
		}
	}

}
