package org.xson.tangyuan.validate.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.validate.Checker;
import org.xson.tangyuan.validate.Rule;
import org.xson.tangyuan.validate.RuleDef;
import org.xson.tangyuan.validate.RuleEnum;
import org.xson.tangyuan.validate.RuleGroup;
import org.xson.tangyuan.validate.RuleGroupItem;
import org.xson.tangyuan.validate.TypeEnum;
import org.xson.tangyuan.validate.ValidateComponent;
import org.xson.tangyuan.validate.xml.node.ValidateServiceNode;
import org.xson.tangyuan.xml.DefaultXmlPluginBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlValidatePluginBuilder extends DefaultXmlPluginBuilder {

	private XmlValidateContext		componentContext	= null;
	private Map<String, RuleDef>	localDefMap			= new HashMap<String, RuleDef>();

	@Override
	public void setContext(String resource, XmlContext xmlContext) throws Throwable {
		this.componentContext = (XmlValidateContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "validate", false);
		if (this.ns.length() > 0) {
			checkNs(this.ns);
		}
	}

	@Override
	public void clean() {
		super.clean();
		this.componentContext = null;
		this.localDefMap.clear();
		this.localDefMap = null;
	}

	@Override
	public void parseRef() throws Throwable {
		log.info(lang("xml.start.parsing.type", "plugin[ref]", this.resource));
		buildDefNode(this.root.evalNodes("def"));
	}

	@Override
	public void parseService() throws Throwable {
		log.info(lang("xml.start.parsing.type", "plugin[service]", this.resource));
		buildRuleGroupNode(this.root.evalNodes("ruleGroup"));
	}

	private void checkDefRepeated(String id, String tagName) {
		if (null != this.localDefMap.get(id)) {
			throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
		}
		if (null != this.componentContext.getGlobleDefMap().get(getFullId(id))) {
			throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
		}
	}

	private void checkGroupRepeated(String id, String tagName) {
		String fullId = getFullId(id);
		if (null != this.componentContext.getRuleGroupMap().get(fullId)) {
			throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
		}
		// 如果是服务，还需要检测服务
		if (ValidateComponent.getInstance().isValidateAsService()) {
			fullId = getServiceFullId(fullId);
			checkServiceRepeated(id, fullId, tagName);
		}
	}

	private String getServiceFullId(String fullId) {
		String validateServicePrefix = ValidateComponent.getInstance().getValidateServicePrefix();
		if (StringUtils.isEmptySafe(fullId)) {
			return fullId;
		}
		return validateServicePrefix + fullId;
	}

	private void addDefNode(RuleDef ruleDef) {
		String fullId = getFullId(ruleDef.getId());
		this.localDefMap.put(ruleDef.getId(), ruleDef);
		this.componentContext.getGlobleDefMap().put(fullId, ruleDef);
		log.info(lang("add.tag", "def", fullId));
	}

	private void addRuleGroup(RuleGroup ruleGroup) {
		String id = ruleGroup.getId();
		String serviceKey = getFullId(id);
		this.componentContext.getRuleGroupMap().put(serviceKey, ruleGroup);
		log.info(lang("add.tag", "ruleGroup", serviceKey));
	}

	protected void registerValidateAsService() {
		if (!ValidateComponent.getInstance().isValidateAsService()) {
			return;
		}

		log.info(lang("register.as.service", "ruleGroup"));

		Map<String, RuleGroup> ruleGroupMap = this.componentContext.getRuleGroupMap();
		for (RuleGroup ruleGroup : ruleGroupMap.values()) {
			String serviceKey = getServiceFullId(getFullId(ruleGroup.getNs(), ruleGroup.getId()));
			ValidateServiceNode service = new ValidateServiceNode(serviceKey, ruleGroup);
			registerService(service, "ruleGroup");
		}
	}

	private void buildDefNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "def";
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			checkDefRepeated(id, tagName);

			List<Rule> ruleList = buildRuleNode(xNode.evalNodes("rule"), tagName + ".rule");
			if (0 == ruleList.size()) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, resource));
			}

			RuleDef ruleDef = new RuleDef(id, ruleList);
			addDefNode(ruleDef);
		}
	}

	private List<Rule> buildRuleNode(List<XmlNodeWrapper> contexts, String tagName) throws Throwable {
		return buildRuleNode(contexts, tagName, null, null);
	}

	/**
	 * group|def use
	 */
	private List<Rule> buildRuleNode(List<XmlNodeWrapper> contexts, String tagName, TypeEnum fieldType, String groupId) throws Throwable {
		List<Rule> ruleList = new ArrayList<>();
		for (XmlNodeWrapper xNode : contexts) {
			Rule rule = null;
			String checkerId = getStringFromAttr(xNode, "checker");
			if (null == checkerId) {
				String name = getStringFromAttr(xNode, "name");
				RuleEnum ruleEnum = RuleEnum.getEnum(name);
				if (null == ruleEnum) {
					throw new XmlParseException(lang("xml.tag.attribute.value.invalid", name, "name", tagName, resource));
				}
				String value = getStringFromAttr(xNode, "value");
				if (null == value) {
					value = getNodeBody(xNode);
				}
				if (null == value) {
					throw new XmlParseException(lang("xml.tag.attribute.empty", "value", tagName, this.resource));
				}
				rule = new Rule(ruleEnum.getEnValue(), value);
				// rule.parseValue(fieldType, ruleEnum, id + ":" + name);
				if (null != fieldType) {
					rule.parseValue(fieldType, ruleEnum, groupId + ":" + name);
				}
			} else {
				// checkerId
				Checker checker = this.componentContext.getCustomCheckerMap().get(checkerId);
				if (null == checker) {
					throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", checkerId, "checker", tagName, this.resource));
				}
				rule = new Rule(checker);
			}
			ruleList.add(rule);
		}
		return ruleList;
	}

	private List<RuleGroupItem> buildItemNode(List<XmlNodeWrapper> contexts, String tagName, String groupId, String groupMessage, int groupCode)
			throws Throwable {
		List<RuleGroupItem> ruleGroupItemList = new ArrayList<RuleGroupItem>();

		for (XmlNodeWrapper xNode : contexts) {

			String fieldName = getStringFromAttr(xNode, "name", lang("xml.tag.attribute.empty", "name", tagName, this.resource));
			String _type = getStringFromAttr(xNode, "type");
			boolean require = getBoolFromAttr(xNode, "require", true);
			String ref = getStringFromAttr(xNode, "ref");

			// 允许没有规则的验证

			String defaultValue = getStringFromAttr(xNode, "defaultValue");
			String itemDesc = getStringFromAttr(xNode, "desc");
			String itemMessage = getStringFromAttr(xNode, "message", groupMessage, null);
			int itemCode = getIntFromAttr(xNode, "code", groupCode);

			TypeEnum fieldType = TypeEnum.getEnum(_type); // xml validation
			if (null == fieldType) {
				throw new XmlParseException(lang("xml.tag.attribute.value.invalid", _type, "type", tagName, resource));
			}

			List<Rule> ruleList = buildRuleNode(xNode.evalNodes("rule"), tagName + ".rule", fieldType, groupId);

			if (null != ref) {
				List<RuleDef> ruleDefList = getRuleDefs(ref, groupId);
				copyRule(ruleList, ruleDefList, fieldType);// 复制RULE
			}

			// support nested item
			List<RuleGroupItem> nestedRuleGroupItemList = null;
			if (TypeEnum.XCO == fieldType || TypeEnum.XCO_ARRAY == fieldType || TypeEnum.XCO_LIST == fieldType || TypeEnum.XCO_SET == fieldType) {
				nestedRuleGroupItemList = buildItemNode(xNode.evalNodes("item"), tagName + ".item", groupId, itemMessage, itemCode);
			}

			// RuleGroupItem ruleGroupItem = new RuleGroupItem(fieldName, fieldType, ruleList, require, defaultValue, itemDesc, itemMessage,
			// itemCode);
			RuleGroupItem ruleGroupItem = new RuleGroupItem(fieldName, fieldType, ruleList, require, defaultValue, itemDesc, itemMessage, itemCode,
					nestedRuleGroupItemList);
			ruleGroupItemList.add(ruleGroupItem);
		}

		return ruleGroupItemList;
	}

	private void buildRuleGroupNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "ruleGroup";
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String groupDesc = getStringFromAttr(xNode, "desc");
			String groupMessage = getStringFromAttr(xNode, "message", ValidateComponent.getInstance().getErrorMessage(), null);
			int groupCode = getIntFromAttr(xNode, "code", ValidateComponent.getInstance().getErrorCode());
			String[] groups = getStringArrayFromAttr(xNode, "group");

			// checkServiceId(id, "Duplicate <ruleGroup>: " + id);
			checkGroupRepeated(id, tagName);

			List<RuleGroupItem> ruleGroupItemList = buildItemNode(xNode.evalNodes("item"), tagName + ".item", id, groupMessage, groupCode);
			if (0 == ruleGroupItemList.size()) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, resource));
			}

			RuleGroup ruleGroup = new RuleGroup(id, this.ns, ruleGroupItemList, groupDesc, groupMessage, groupCode, groups);
			addRuleGroup(ruleGroup);
		}
	}

	private List<RuleDef> getRuleDefs(String ref, String id) {
		List<RuleDef> list = new ArrayList<RuleDef>();
		String[] array = ref.split(",");
		for (String temp : array) {
			String refId = temp.trim();
			RuleDef ruleDef = null;
			if (temp.indexOf(TangYuanContainer.getInstance().getNsSeparator()) == -1) {
				ruleDef = this.localDefMap.get(refId);
			} else {
				ruleDef = this.componentContext.getGlobleDefMap().get(refId);
			}
			if (null == ruleDef) {
				// throw new XmlParseException("Ref does not exist: " + refId + ", in ruleGroup: " + id);
				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", refId, "ref", "ruleGroup.item", this.resource));
			}
			// xvUtil.objectEmpty(ruleDef, "Ref does not exist: " + refId + ", in ruleGroup: " + id);
			list.add(ruleDef);
		}

		return list;
	}

	private void copyRule(List<Rule> ruleList, List<RuleDef> ruleDefList, TypeEnum fieldType) {
		for (RuleDef ruleDef : ruleDefList) {
			List<Rule> srcRuleList = ruleDef.getRule();
			for (Rule rule : srcRuleList) {
				Rule newRule = rule.copy();
				if (null == newRule.getChecker()) {
					newRule.parseValue(fieldType, RuleEnum.getEnum(newRule.getName()), ruleDef.getId());
				}
				ruleList.add(newRule);
			}
		}
	}

	private String getNodeBody(XmlNodeWrapper ruleNode) {
		NodeList children = ruleNode.getNode().getChildNodes();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < children.getLength(); i++) {
			XmlNodeWrapper child = ruleNode.newXMlNode(children.item(i));
			if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
				String data = child.getStringBody("").trim();
				if (data.length() > 0) {
					builder.append(data);
				}
			}
		}
		String value = builder.toString();
		if (value.length() > 0) {
			return value;
		}
		return null;
	}

}
