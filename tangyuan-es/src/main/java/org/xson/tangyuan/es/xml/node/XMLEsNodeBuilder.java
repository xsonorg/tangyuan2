package org.xson.tangyuan.es.xml.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.es.ResultConverter;
import org.xson.tangyuan.es.converters.JSONConverter;
import org.xson.tangyuan.es.datasource.EsSourceManager;
import org.xson.tangyuan.es.xml.XmlEsContext;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.ognl.vars.parser.LogicalExprParser;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.IfNode;
import org.xson.tangyuan.xml.node.MixedNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class XMLEsNodeBuilder extends XmlNodeBuilder {

	private Log				log		= LogFactory.getLog(getClass());

	private XmlNodeWrapper	root	= null;
	private XmlEsContext	context	= null;

	@Override
	public Log getLog() {
		return this.log;
	}

	@Override
	public void setContext(XmlNodeWrapper root, XmlContext context) {
		this.context = (XmlEsContext) context;
		this.root = root;
		this.ns = this.root.getStringAttribute("ns", "");
		// TODO 需要增加版本号
		if (this.ns.length() > 0) {
			this.context.getXmlContext().checkNs(this.ns);
		}
	}

	public void parseRef() {
	}

	public void parseService() {
		configurationElement(this.root);
	}

	protected String getFullId(String id) {
		return TangYuanUtil.getQualifiedName(this.ns, id, null, TangYuanContainer.getInstance().getNsSeparator());
	}

	private String checkDsKey(String dsKey, String service) {
		if (null == dsKey) {
			dsKey = EsSourceManager.getDefaultEsKey();
			if (null == dsKey) {
				throw new XmlParseException("service[" + service + "] uses an invalid esKey: " + dsKey);
			}
		} else {
			if (!EsSourceManager.isValidEsKey(dsKey)) {
				throw new XmlParseException("service[" + service + "] uses an invalid esKey: " + dsKey);
			}
		}
		return dsKey;
	}

	private void existingService(String id) {
		if (null == id || 0 == id.length()) {
			throw new XmlParseException("Service ID can not be empty.");
		}
		String fullId = getFullId(id);
		if (null != this.context.getXmlContext().getIntegralServiceMap().get(fullId)) {
			throw new XmlParseException("Duplicate service nodes: " + fullId);
		}
		if (null != this.context.getXmlContext().getIntegralRefMap().get(fullId)) {
			throw new XmlParseException("Duplicate service nodes: " + fullId);
		}
		this.context.getXmlContext().getIntegralServiceMap().put(fullId, 1);
	}

	/**
	 * 解析: ID:xxx; key:xxx; expiry:10(秒)
	 */
	private CacheUseVo parseCacheUse(String cacheUse, String service) {
		CacheUseVo cacheUseVo = null;
		String[] array = cacheUse.split(";");
		if (array.length > 0) {
			Map<String, String> map = new HashMap<String, String>();
			for (int i = 0; i < array.length; i++) {
				String[] item = array[i].split(":");
				map.put(item[0].trim().toUpperCase(), item[1].trim());
			}
			TangYuanCache cache = CacheComponent.getInstance().getCache(map.get("id".toUpperCase()));
			if (null == cache) {
				throw new XmlParseException("Non-existent cache: " + cacheUse);
			}
			String key = map.get("key".toUpperCase());
			if (null == key) {
				throw new XmlParseException("Missing cache.key: " + cacheUse);
			}
			Long expiry = null;
			if (map.containsKey("expiry".toUpperCase())) {
				expiry = Long.parseLong(map.get("expiry".toUpperCase()));
			}
			cacheUseVo = new CacheUseVo(cache, key, expiry, service);
		}
		return cacheUseVo;
	}

	private void configurationElement(XmlNodeWrapper context) {

		List<AbstractServiceNode> getList = buildGetNodes(context.evalNodes("get"));
		List<AbstractServiceNode> postList = buildPostNodes(context.evalNodes("post"));
		List<AbstractServiceNode> putList = buildPutNodes(context.evalNodes("put"));
		List<AbstractServiceNode> deleteList = buildDeleteNodes(context.evalNodes("delete"));

		registerService(getList, "get");
		registerService(postList, "post");
		registerService(putList, "put");
		registerService(deleteList, "delete");
	}

	private TangYuanNode parseNode(XmlNodeWrapper context, boolean internal) {
		List<TangYuanNode> contents = parseDynamicTags(context);
		int size = contents.size();
		TangYuanNode sqlNode = null;
		if (size == 1) {
			sqlNode = contents.get(0);
		} else if (size > 1) {
			sqlNode = new MixedNode(contents);
		} else {
			log.warn("节点内容为空, 将被忽略:" + context.getName());
		}
		return sqlNode;
	}

	private List<TangYuanNode> parseDynamicTags(XmlNodeWrapper node) {
		List<TangYuanNode> contents = new ArrayList<TangYuanNode>();
		NodeList children = node.getNode().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			XmlNodeWrapper child = node.newXMlNode(children.item(i));
			if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
				String data = child.getStringBody("");
				if (isEmpty(data)) {
					continue;
				}
				// 使用新的sqlText节点
				contents.add(new EsTextNode(data));
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

	private List<AbstractServiceNode> buildGetNodes(List<XmlNodeWrapper> contexts) {
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper context : contexts) {
			// TangYuanNode sqlNode = parseNode(context, false);
			TangYuanNode sqlNode = getURLNode(context, "get");
			if (null != sqlNode) {
				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
				existingService(id);

				String dsKey = StringUtils.trim(context.getStringAttribute("esKey"));
				dsKey = checkDsKey(dsKey, id);

				String _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
				CacheUseVo cacheUse = null;
				if (null != _cacheUse && _cacheUse.length() > 0) {
					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
				}

				String _converter = StringUtils.trim(context.getStringAttribute("converter"));
				ResultConverter converter = getConverter(_converter);

				EsGetNode selectSetNode = new EsGetNode(id, ns, getFullId(id), dsKey, sqlNode, cacheUse, converter);
				list.add(selectSetNode);
			}
		}
		return list;
	}

	private List<AbstractServiceNode> buildPostNodes(List<XmlNodeWrapper> contexts) {
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper context : contexts) {
			TangYuanNode sqlNode = getURLNode(context, "post");
			TangYuanNode bodyNode = getBodyNode(context, "post");
			if (null != sqlNode) {
				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
				existingService(id);

				String dsKey = StringUtils.trim(context.getStringAttribute("esKey"));
				dsKey = checkDsKey(dsKey, id);

				String _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
				CacheUseVo cacheUse = null;
				if (null != _cacheUse && _cacheUse.length() > 0) {
					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
				}

				String _converter = StringUtils.trim(context.getStringAttribute("converter"));
				ResultConverter converter = getConverter(_converter);

				EsPostNode selectSetNode = new EsPostNode(id, ns, getFullId(id), dsKey, sqlNode, bodyNode, cacheUse, converter);
				list.add(selectSetNode);
			}
		}
		return list;
	}

	private List<AbstractServiceNode> buildPutNodes(List<XmlNodeWrapper> contexts) {
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper context : contexts) {
			TangYuanNode sqlNode = getURLNode(context, "put");
			TangYuanNode bodyNode = getBodyNode(context, "put");
			if (null != sqlNode) {
				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
				existingService(id);

				String dsKey = StringUtils.trim(context.getStringAttribute("esKey"));
				dsKey = checkDsKey(dsKey, id);

				String _converter = StringUtils.trim(context.getStringAttribute("converter"));
				ResultConverter converter = getConverter(_converter);

				EsPutNode selectSetNode = new EsPutNode(id, ns, getFullId(id), dsKey, sqlNode, bodyNode, converter);
				list.add(selectSetNode);
			}
		}
		return list;
	}

	private List<AbstractServiceNode> buildDeleteNodes(List<XmlNodeWrapper> contexts) {
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper context : contexts) {
			TangYuanNode sqlNode = getURLNode(context, "delete");
			if (null != sqlNode) {
				String id = StringUtils.trim(context.getStringAttribute("id")); // xml
				existingService(id);

				String dsKey = StringUtils.trim(context.getStringAttribute("esKey"));
				dsKey = checkDsKey(dsKey, id);

				String _converter = StringUtils.trim(context.getStringAttribute("converter"));
				ResultConverter converter = getConverter(_converter);

				EsDeleteNode selectSetNode = new EsDeleteNode(id, ns, getFullId(id), dsKey, sqlNode, converter);
				list.add(selectSetNode);
			}
		}
		return list;
	}

	private TangYuanNode getURLNode(XmlNodeWrapper context, String xName) {
		List<XmlNodeWrapper> contexts = context.evalNodes("url");
		if (contexts.size() != 1) {
			throw new XmlParseException("In the <" + xName + "> node, the <url> node is missing.");
		}
		return parseNode(contexts.get(0), false);
	}

	private TangYuanNode getBodyNode(XmlNodeWrapper context, String xName) {
		List<XmlNodeWrapper> contexts = context.evalNodes("body");
		if (contexts.size() != 1) {
			throw new XmlParseException("In the <" + xName + "> node, the <body> node is missing.");
		}
		return parseNode(contexts.get(0), false);
	}

	private ResultConverter getConverter(String converter) {
		if (null == converter) {
			return this.context.getConverter(JSONConverter.key);
		}
		ResultConverter rc = this.context.getConverter(converter);
		if (null == rc) {
			throw new XmlParseException("Nonexistent converter: " + converter);
		}
		return rc;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private interface NodeHandler {
		void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents);
	}

	private class IfHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			String test = nodeToHandle.getStringAttribute("test");
			if (null == test) {
				throw new XmlParseException("<if> node test == null");
			}
			List<TangYuanNode> contents = parseDynamicTags(nodeToHandle);
			int size = contents.size();
			IfNode ifNode = null;
			if (1 == size) {
				ifNode = new IfNode(contents.get(0), new LogicalExprParser().parse(test));
			} else if (size > 1) {
				ifNode = new IfNode(new MixedNode(contents), new LogicalExprParser().parse(test));
			} else { // size == 0
				throw new XmlParseException("<if> node contents == null");
			}
			targetContents.add(ifNode);
		}
	}

	private class ElseIfHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			if (0 == targetContents.size()) {
				throw new XmlParseException("<elseIf> node is not legal.");
			}
			TangYuanNode previousNode = targetContents.get(targetContents.size() - 1);
			if (!(previousNode instanceof IfNode)) {
				throw new XmlParseException("The node before the <elseIf> node must be an <if> node.");
			}
			String test = nodeToHandle.getStringAttribute("test");
			if (null == test) {
				throw new XmlParseException("<elseIf> node test == null");
			}

			List<TangYuanNode> contents = parseDynamicTags(nodeToHandle);
			int size = contents.size();

			IfNode ifNode = null;
			if (1 == size) {
				ifNode = new IfNode(contents.get(0), new LogicalExprParser().parse(test));
			} else if (size > 1) {
				ifNode = new IfNode(new MixedNode(contents), new LogicalExprParser().parse(test));
			} else {
				throw new XmlParseException("<elseIf> node contents == null");
			}
			((IfNode) previousNode).addElseIfNode(ifNode);
		}
	}

	private class ElseHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			if (0 == targetContents.size()) {
				throw new XmlParseException("<else> node is not legal.");
			}
			TangYuanNode previousNode = targetContents.get(targetContents.size() - 1);
			if (!(previousNode instanceof IfNode)) {
				throw new XmlParseException("<else> node is not legal.");
			}
			List<TangYuanNode> contents = parseDynamicTags(nodeToHandle);
			int size = contents.size();
			IfNode ifNode = null;
			if (1 == size) {
				ifNode = new IfNode(contents.get(0), null);
			} else if (size > 1) {
				ifNode = new IfNode(new MixedNode(contents), null);
			} else {
				throw new XmlParseException("<else> node contents == null");
			}
			((IfNode) previousNode).addElseNode(ifNode);
		}
	}

	private class ForEachHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			String collection = StringUtils.trim(nodeToHandle.getStringAttribute("collection"));
			if (!checkVar(collection)) {
				throw new XmlParseException("<forEach> collection is not legal, Should be {xxx}");
			}
			collection = getRealVal(collection);

			String index = StringUtils.trim(nodeToHandle.getStringAttribute("index"));
			if (null != index) {
				if (!checkVar(index)) {
					throw new XmlParseException("<forEach> index is not legal, Should be {xxx}");
				}
				index = getRealVal(index);
			}

			String open = StringUtils.trim(nodeToHandle.getStringAttribute("open"));
			String close = StringUtils.trim(nodeToHandle.getStringAttribute("close"));
			String separator = StringUtils.trim(nodeToHandle.getStringAttribute("separator"));

			List<TangYuanNode> contents = parseDynamicTags(nodeToHandle);
			int size = contents.size();
			TangYuanNode sqlNode = null;
			if (1 == size) {
				sqlNode = contents.get(0);
			} else if (size > 1) {
				sqlNode = new MixedNode(contents);
			}

			// if (null == sqlNode && null == open && null == close && null == separator) {
			// open = "(";
			// close = ")";
			// separator = ",";
			// }
			// if (null == sqlNode) {
			// if (null == index) {
			// index = "i";
			// }
			// sqlNode = new EsTextNode("#{" + collection + "[" + index + "]}");
			// }

			if (null == sqlNode) {
				throw new XmlParseException("<forEach> node missing child nodes.");
			}

			ForEachNode forEachNode = new EsForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator);
			targetContents.add(forEachNode);
		}
	}

	private Map<String, NodeHandler> nodeHandlers = new HashMap<String, NodeHandler>() {
		private static final long serialVersionUID = 1L;

		{
			put("foreach", new ForEachHandler());
			put("if", new IfHandler());
			put("else", new ElseHandler());
			put("elseif", new ElseIfHandler());
		}
	};

}
