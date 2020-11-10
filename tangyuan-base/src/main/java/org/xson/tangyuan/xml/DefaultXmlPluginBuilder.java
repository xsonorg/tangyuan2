package org.xson.tangyuan.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.vars.parser.LogicalExprParser;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.CallNode;
import org.xson.tangyuan.xml.node.CallNode.CallMode;
import org.xson.tangyuan.xml.node.ExceptionNode;
import org.xson.tangyuan.xml.node.IfNode;
import org.xson.tangyuan.xml.node.LogNode;
import org.xson.tangyuan.xml.node.MixedNode;
import org.xson.tangyuan.xml.node.ReturnNode;
import org.xson.tangyuan.xml.node.SegmentNode;
import org.xson.tangyuan.xml.node.SetVarNode;
import org.xson.tangyuan.xml.node.TangYuanNode;
import org.xson.tangyuan.xml.node.vo.PropertyItem;

public class DefaultXmlPluginBuilder extends DefaultXmlBuilder {

	protected String					ns						= "";
	protected Map<String, TangYuanNode>	integralRefMap			= null;
	protected Map<String, Integer>		integralServiceMap		= null;
	protected Map<String, Integer>		integralServiceNsMap	= null;
	protected Map<String, Integer>		integralServiceClassMap	= null;

	protected Map<String, NodeHandler>	nodeHandlers			= new HashMap<String, NodeHandler>();

	protected class SelectResult {
		public Class<?>		resultType;
		public MappingVo	resultMap;

		public SelectResult(Class<?> resultType, MappingVo resultMap) {
			this.resultType = resultType;
			this.resultMap = resultMap;
		}
	}

	@Override
	protected void clean() {
		super.clean();
		this.integralRefMap = null;
		this.integralServiceMap = null;
		this.integralServiceNsMap = null;
		this.integralServiceClassMap = null;

		this.nodeHandlers.clear();
		this.nodeHandlers = null;
	}

	protected interface NodeHandler {
		void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents);
	}

	public void setContext(String resource, XmlContext xmlContext) throws Throwable {
		throw new XmlParseException(lang("method.need.override"));
	}

	public void parseRef() throws Throwable {
		throw new XmlParseException(lang("method.need.override"));
	}

	public void parseService() throws Throwable {
		throw new XmlParseException(lang("method.need.override"));
	}

	protected Class<?> getServiceResultType() {
		return TangYuanContainer.getInstance().getDefaultResultType();
	}

	protected void init(String resource, String rootName, boolean placeholder) throws Throwable {
		super.init(resource, rootName, placeholder);

		this.ns = this.root.getStringAttribute("ns", "");
		this.integralRefMap = this.globalContext.getIntegralRefMap();
		this.integralServiceMap = this.globalContext.getIntegralServiceMap();
		this.integralServiceNsMap = this.globalContext.getIntegralServiceNsMap();
		this.integralServiceClassMap = this.globalContext.getIntegralServiceClassMap();
	}

	protected String getFullId(String id) {
		return TangYuanUtil.getQualifiedName(this.ns, id, null, TangYuanContainer.getInstance().getNsSeparator());
	}

	protected String getFullId(String ns, String id) {
		return TangYuanUtil.getQualifiedName(ns, id, null, TangYuanContainer.getInstance().getNsSeparator());
	}

	protected void checkNs(String ns) {
		if (integralServiceNsMap.containsKey(ns)) {
			// throw new XmlParseException("Duplicate ns: " + ns);
			throw new XmlParseException(lang("xml.tag.repeated", "ns", this.root.getName(), this.resource));
		}
		this.integralServiceNsMap.put(ns, 1);
	}

	protected void checkServiceRepeated(String id, String tagName) {
		String fullId = getFullId(id);
		if (this.integralServiceMap.containsKey(fullId)) {
			throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
		}
		if (this.integralRefMap.containsKey(fullId)) {
			throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
		}
		this.integralServiceMap.put(fullId, 1);
	}

	protected void checkServiceRepeated(String id, String fullId, String tagName) {
		if (this.integralServiceMap.containsKey(fullId)) {
			throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
		}
		if (this.integralRefMap.containsKey(fullId)) {
			throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
		}
		this.integralServiceMap.put(fullId, 1);
	}

	protected void registerService(List<AbstractServiceNode> list, String tagName) {
		for (AbstractServiceNode serviceNode : list) {
			registerService(serviceNode, tagName);
		}
	}

	protected void registerService(AbstractServiceNode serviceNode, String tagName) {
		TangYuanContainer.getInstance().addService(serviceNode);
		log.info(lang("add.tag.service", tagName, serviceNode.getServiceKey()));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////

	protected TangYuanNode getTextNode(String data) {
		return null;
	}

	protected TangYuanNode parseNode(XmlNodeWrapper context, boolean internal) {
		List<TangYuanNode> contents = parseDynamicTags(context);
		int size = contents.size();
		TangYuanNode sqlNode = null;
		if (size == 1) {
			sqlNode = contents.get(0);
		} else if (size > 1) {
			sqlNode = new MixedNode(contents);
		} else {
			// log.warn("节点内容为空, 将被忽略:" + context.getName());
			log.warn(lang("xml.tag.ignored", context.getName(), this.resource));
		}
		return sqlNode;
	}

	protected List<TangYuanNode> parseDynamicTags(XmlNodeWrapper node) {
		List<TangYuanNode> contents = new ArrayList<TangYuanNode>();
		NodeList children = node.getNode().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			XmlNodeWrapper child = node.newXMlNode(children.item(i));
			if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
				String data = child.getStringBody("");
				if (StringUtils.isEmptySafe(data)) {
					continue;
				}
				// contents.add(new EsTextNode(data));
				contents.add(getTextNode(data));
			} else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = child.getNode().getNodeName();
				NodeHandler handler = nodeHandlers.get(nodeName);
				if (handler == null) {
					throw new XmlParseException("Unknown element <" + nodeName + "> in SQL statement.");
				}
				handler.handleNode(child, contents);
			}
		}
		return contents;
	}

	protected List<PropertyItem> buildPropertyItem(List<XmlNodeWrapper> properties, String tagName) {
		List<PropertyItem> resultList = null;
		if (properties.size() > 0) {
			resultList = new ArrayList<PropertyItem>();
			for (XmlNodeWrapper xNode : properties) {
				String name = getStringFromAttr(xNode, "name");
				String value = getStringFromAttr(xNode, "value", lang("xml.tag.attribute.empty", "value", tagName, resource));
				if (null == name) {
					name = value;
				}
				Object valueObj = null;
				if (checkVar(value)) {
					// valueObj = new NormalParser().parse(getRealVal(value));
					// valueObj = new GAParserWarper().parse(getRealVal(value));
					valueObj = parseVariableUseGA(value);
				} else {
					valueObj = parseValue(value);
				}
				if (!checkVar(name)) {
					throw new XmlParseException(lang("xml.tag.attribute.invalid.should", name, "{xxx}", tagName, resource));
				}
				name = getRealVal(name);
				resultList.add(new PropertyItem(name, valueObj));
			}
		}
		return resultList;
	}

	protected SelectResult parseSelectResult(String resultType, String resultMap, String tagName, XmlContext context) {

		// TODO: 考虑支持Bean

		Class<?> resultTypeClass = null;
		MappingVo resultMapVo = null;

		if (null != resultType && null != resultMap) {// 都存在值的情况下
			if ("xco".equalsIgnoreCase(resultType)) {
				resultTypeClass = XCO.class;
			} else if ("map".equalsIgnoreCase(resultType)) {
				resultTypeClass = Map.class;
			}
			resultMapVo = context.getMappingVoMap().get(resultMap);
			if (null == resultMapVo) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", resultMap, "resultMap", tagName, this.resource));
			}
		} else if (null == resultType && null != resultMap) {
			resultMapVo = context.getMappingVoMap().get(resultMap);
			if (null == resultMapVo) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", resultMap, "resultMap", tagName, this.resource));
			}
			resultTypeClass = resultMapVo.getBeanClass();
		} else if (null != resultType && null == resultMap) {
			if ("xco".equalsIgnoreCase(resultType)) {
				resultTypeClass = XCO.class;
			} else if ("map".equalsIgnoreCase(resultType)) {
				resultTypeClass = Map.class;
			}
		}

		if (null == resultTypeClass) {
			resultTypeClass = TangYuanContainer.getInstance().getDefaultResultType();
		}
		return new SelectResult(resultTypeClass, resultMapVo);
	}

	public class IfHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "if";
			String test = getStringFromAttr(xNode, "test", lang("xml.tag.attribute.empty", "test", tagName, resource));
			List<TangYuanNode> contents = parseDynamicTags(xNode);
			int size = contents.size();
			IfNode ifNode = null;
			if (1 == size) {
				ifNode = new IfNode(contents.get(0), new LogicalExprParser().parse(test));
			} else if (size > 1) {
				ifNode = new IfNode(new MixedNode(contents), new LogicalExprParser().parse(test));
			}
			if (null == ifNode) {
				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			}
			targetContents.add(ifNode);
		}
	}

	public class ElseIfHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "elseif";
			if (CollectionUtils.isEmpty(targetContents)) {
				throw new XmlParseException(lang("xml.tag.invalid", tagName, resource));
			}
			TangYuanNode previousNode = targetContents.get(targetContents.size() - 1);
			if (!(previousNode instanceof IfNode)) {
				// the tag before the '{}' tag must be an '{}' tag----"elseIf", "if"
				throw new XmlParseException(lang("xml.tag.invalid", tagName, resource));
			}
			String test = getStringFromAttr(xNode, "test", lang("xml.tag.attribute.empty", "test", tagName, resource));
			List<TangYuanNode> contents = parseDynamicTags(xNode);
			int size = contents.size();

			IfNode ifNode = null;
			if (1 == size) {
				ifNode = new IfNode(contents.get(0), new LogicalExprParser().parse(test));
			} else if (size > 1) {
				ifNode = new IfNode(new MixedNode(contents), new LogicalExprParser().parse(test));
			}
			if (null == ifNode) {
				// isTrue(size == 0, "the content in the tag[{}] cannot be empty", "elseIf");
				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			}
			((IfNode) previousNode).addElseIfNode(ifNode);
		}
	}

	public class ElseHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "else";
			if (CollectionUtils.isEmpty(targetContents)) {
				throw new XmlParseException(lang("xml.tag.invalid", tagName, resource));
			}
			TangYuanNode previousNode = targetContents.get(targetContents.size() - 1);
			if (!(previousNode instanceof IfNode)) {
				throw new XmlParseException(lang("xml.tag.invalid", tagName, resource));
			}

			List<TangYuanNode> contents = parseDynamicTags(xNode);
			int size = contents.size();
			IfNode ifNode = null;
			if (1 == size) {
				ifNode = new IfNode(contents.get(0), null);
			} else if (size > 1) {
				ifNode = new IfNode(new MixedNode(contents), null);
			}
			if (null == ifNode) {
				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			}
			((IfNode) previousNode).addElseNode(ifNode);
		}
	}

	public class IncludeHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "include";
			String refKey = getStringFromAttr(xNode, "ref", lang("xml.tag.attribute.empty", "ref", tagName, resource));
			TangYuanNode refNode = globalContext.getIntegralRefMap().get(refKey);
			if (null == refNode) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", refKey, "ref", tagName, resource));
			}
			// 增加段的引用
			if (refNode instanceof SegmentNode) {
				XmlNodeWrapper innerNode = ((SegmentNode) refNode).getNode();
				refNode = parseNode(innerNode, true);
				if (null == refNode) {
					// log.warn("The referenced segment is empty, ref: " + refKey);
					log.warn(lang("xml.tag.attribute.reference.invalid", refKey, "ref", tagName, resource));
					return;
				}
			}
			targetContents.add(refNode);
		}
	}

	public class LogHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "log";
			String message = getStringFromAttr(xNode, "message", lang("xml.tag.attribute.empty", "message", tagName, resource));
			String _level = getStringFromAttr(xNode, "level");
			Integer level = 3;
			if (null != _level) {
				level = getLogLevel(_level);
			}
			if (null == level) {
				throw new XmlParseException(lang("xml.tag.attribute.value.invalid", _level, "level", tagName, resource));
			}
			LogNode logNode = new LogNode(level.intValue(), message);
			targetContents.add(logNode);
		}
	}

	public class ThrowHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "exception";
			String test = getStringFromAttr(xNode, "test", lang("xml.tag.attribute.empty", "test", tagName, resource));
			int code = getIntFromAttr(xNode, "code", lang("xml.tag.attribute.empty", "code", tagName, resource));
			String message = getStringFromAttr(xNode, "message");
			String i18n = getStringFromAttr(xNode, "i18n");
			targetContents.add(new ExceptionNode(new LogicalExprParser().parse(test), code, message, i18n));
		}
	}

	public class SetVarHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			// <setvar key="{x}" value="100" type="Integer" />
			String tagName = "setvar";

			String key = getStringFromAttr(xNode, "key", lang("xml.tag.attribute.empty", "key", tagName, resource));
			String _value = getStringFromAttr(xNode, "value", lang("xml.tag.attribute.empty", "value", tagName, resource));
			String type = getStringFromAttr(xNode, "type");

			if (!checkVar(key)) {
				throw new XmlParseException(lang("xml.tag.attribute.invalid.should", key, "{xxx}", tagName, resource));
			}
			key = getRealVal(key);
			Object value = null;
			boolean constant = true;
			if (checkVar(_value)) {
				constant = false;
				// value = new GAParserWarper().parse(getRealVal(_value));
				value = parseVariableUseGA(_value);
			} else {
				value = getSetVarValue(_value, type);
			}
			SetVarNode setVarNode = new SetVarNode(key, value, constant);
			targetContents.add(setVarNode);
		}
	}

	public class ReturnHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "return";
			String _result = getStringFromAttr(xNode, "value");
			Object result = null;
			if (null != _result) {
				if (checkVar(_result)) {
					// result = new NormalParser().parse(getRealVal(_result));
					// result = new GAParserWarper().parse(getRealVal(_result));
					result = parseVariableUseGA(_result);
				} else {
					result = parseValue(_result);
				}
			}
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			List<PropertyItem> resultList = buildPropertyItem(properties, tagName);
			if (null != result && null != resultList) {
				// throw new XmlParseException("<return> node in the result | property can only choose a way.");
				throw new XmlParseException(lang("xml.tag.attribute.empty", "value|property", tagName, resource));
			}
			ReturnNode returnNode = new ReturnNode(result, resultList, getServiceResultType());
			targetContents.add(returnNode);
		}
	}

	public class CallHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "call";

			String serviceId = getStringFromAttr(xNode, "service", lang("xml.tag.attribute.empty", "service", tagName, resource));
			String resultKey = getStringFromAttr(xNode, "resultKey");
			String _mode = getStringFromAttr(xNode, "mode");
			String codeKey = getStringFromAttr(xNode, "codeKey");
			String messageKey = getStringFromAttr(xNode, "messageKey");

			// fix: 新增变量调用功能
			Object service = serviceId;
			if (checkVar(serviceId)) {
				// service = new NormalParser().parse(getRealVal(serviceId));
				// service = new GAParserWarper().parse(getRealVal(serviceId));
				service = parseVariableUseGA(serviceId);
			}
			// 增加新的默认模式
			CallMode mode = CallMode.SYNC;
			if (null != _mode) {
				mode = getCallMode(_mode);
			}
			if (null == mode) {
				throw new XmlParseException(lang("xml.tag.attribute.value.invalid", _mode, "mode", tagName, resource));
			}

			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			List<PropertyItem> itemList = buildPropertyItem(properties, tagName);

			// service id可以放在运行期间检查
			targetContents.add(new CallNode(service, resultKey, mode, itemList, codeKey, messageKey));
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	protected Integer getLogLevel(String str) {
		if ("ERROR".equalsIgnoreCase(str)) {
			return 5;
		} else if ("WARN".equalsIgnoreCase(str)) {
			return 4;
		} else if ("INFO".equalsIgnoreCase(str)) {
			return 3;
		} else if ("DEBUG".equalsIgnoreCase(str)) {
			return 2;
		} else if ("TRACE".equalsIgnoreCase(str)) {
			return 1;
		}
		return null;
	}

	protected CallMode getCallMode(String str) {
		try {
			return CallMode.valueOf(str.toUpperCase());
		} catch (Throwable e) {
		}
		return null;
	}

}
