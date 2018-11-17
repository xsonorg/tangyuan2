package org.xson.tangyuan.sql.xml.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.CacheComponent;
import org.xson.tangyuan.cache.TangYuanCache;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.vars.parser.LogicalExprParser;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.sql.SqlComponent;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.sql.xml.XmlSqlContext;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.CallNode;
import org.xson.tangyuan.xml.node.CallNode.CallMode;
import org.xson.tangyuan.xml.node.ExceptionNode;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.IfNode;
import org.xson.tangyuan.xml.node.LogNode;
import org.xson.tangyuan.xml.node.MixedNode;
import org.xson.tangyuan.xml.node.ReturnNode;
import org.xson.tangyuan.xml.node.SegmentNode;
import org.xson.tangyuan.xml.node.SetVarNode;
import org.xson.tangyuan.xml.node.TangYuanNode;
import org.xson.tangyuan.xml.node.vo.PropertyItem;

public class XMLSqlNodeBuilder extends XmlNodeBuilder {

	private Log				log					= LogFactory.getLog(getClass());

	private XmlNodeWrapper	root				= null;
	private XmlSqlContext	sqlContext			= null;

	private String			dsKeyWithSqlService	= null;
	private Class<?>		serviceResultType	= null;

	@Override
	public Log getLog() {
		return this.log;
	}

	@Override
	public void parseRef() {
		buildRefNode(this.root.evalNodes("sql"));			// 解析<sql>节点
		buildSegmentNode(this.root.evalNodes("segment"));	// 增加段定义和引用
	}

	@Override
	public void parseService() {
		configurationElement(this.root);
	}

	protected String getFullId(String id) {
		return TangYuanUtil.getQualifiedName(this.ns, id, null, TangYuanContainer.getInstance().getNsSeparator());
	}

	@Override
	public void setContext(XmlNodeWrapper root, XmlContext context) {
		this.sqlContext = (XmlSqlContext) context;
		this.root = root;
		this.ns = this.root.getStringAttribute("ns", "");
		// TODO 需要增加版本号
		if (this.ns.length() > 0) {
			this.sqlContext.getXmlContext().checkNs(this.ns);
		}
	}

	private void existingService(String id) {
		if (null == id || 0 == id.length()) {
			throw new XmlParseException("Service ID can not be empty.");
		}
		String fullId = getFullId(id);
		if (null != this.sqlContext.getXmlContext().getIntegralServiceMap().get(fullId)) {
			throw new XmlParseException("Duplicate service nodes: " + fullId);
		}
		if (null != this.sqlContext.getXmlContext().getIntegralRefMap().get(fullId)) {
			throw new XmlParseException("Duplicate service nodes: " + fullId);
		}
		this.sqlContext.getXmlContext().getIntegralServiceMap().put(fullId, 1);
	}

	private String checkDsKey(String dsKey, String service) {
		if (null == dsKey) {
			dsKey = SqlComponent.getInstance().getDataSourceManager().getDefaultDsKey();
			if (null == dsKey) {
				throw new XmlParseException("service[" + service + "] uses an invalid dsKey: " + dsKey);
			}
		} else {
			if (!SqlComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
				throw new XmlParseException("service[" + service + "] uses an invalid dsKey: " + dsKey);
			}
		}
		return dsKey;
	}

	private void checkInnerDsKey(String dsKey, String method) {
		if (!SqlComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
			throw new XmlParseException("service[" + method + "] uses an invalid dsKey: " + dsKey);
		}
	}

	/**
	 * 解析: ID:xxx; key:xxx; time:1000; ignore:a,b <br />
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

	/**
	 * 解析: ID:xxx; key:xxx; ignore=a,b <br />
	 * 解析: ID:xxx; key:xxx;
	 */
	private CacheCleanVo parseCacheClean(String cacheUse, String service) {
		CacheCleanVo cacheCleanVo = null;
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
			// String[] ignore = null;
			// if (map.containsKey("ignore".toUpperCase())) {
			// ignore = map.get("ignore".toUpperCase()).split(",");
			// }
			// cacheCleanVo = new CacheCleanVo(cacheVo, key, ignore, service);
			cacheCleanVo = new CacheCleanVo(cache, key, service);
		}
		return cacheCleanVo;
	}

	private void buildRefNode(List<XmlNodeWrapper> contexts) {
		for (XmlNodeWrapper context : contexts) {
			String id = StringUtils.trim(context.getStringAttribute("id")); // xml V
			String fullId = getFullId(id);
			if (null == this.sqlContext.getXmlContext().getIntegralRefMap().get(fullId)) {
				TangYuanNode sqlNode = parseNode(context, false);
				if (null != sqlNode) {
					this.sqlContext.getXmlContext().getIntegralRefMap().put(fullId, sqlNode);
					log.info("add <sql> node: " + fullId);
				}
			} else {
				throw new XmlParseException("Duplicate <sql> nodes: " + id);
			}
		}
	}

	// 扫描段
	private void buildSegmentNode(List<XmlNodeWrapper> contexts) {
		for (XmlNodeWrapper context : contexts) {
			String id = StringUtils.trim(context.getStringAttribute("id")); // xml V
			String fullId = getFullId(id);
			if (null == this.sqlContext.getXmlContext().getIntegralRefMap().get(fullId)) {
				TangYuanNode sqlNode = new SegmentNode(context);
				if (null != sqlNode) {
					this.sqlContext.getXmlContext().getIntegralRefMap().put(fullId, sqlNode);
					log.info("add <segment> node: " + fullId);
				}
			} else {
				throw new XmlParseException("Duplicate <segment> nodes: " + id);
			}
		}
	}

	private void configurationElement(XmlNodeWrapper context) {
		List<AbstractServiceNode> selectSetList = buildSelectSetNodes(context.evalNodes("selectSet"));
		List<AbstractServiceNode> selectOneList = buildSelectOneNodes(context.evalNodes("selectOne"));
		List<AbstractServiceNode> selectVarList = buildSelectVarNodes(context.evalNodes("selectVar"));
		List<AbstractServiceNode> insertList = buildInsertNodes(context.evalNodes("insert"));
		List<AbstractServiceNode> updateList = buildUpdateNodes(context.evalNodes("update"));
		List<AbstractServiceNode> deleteList = buildDeleteNodes(context.evalNodes("delete"));
		List<AbstractServiceNode> sqlServiceList = buildSqlServiceNodes(context.evalNodes("sql-service"));

		registerService(selectSetList, "selectSet");
		registerService(selectOneList, "selectOne");
		registerService(selectVarList, "selectVar");
		registerService(insertList, "insert");
		registerService(updateList, "update");
		registerService(deleteList, "delete");
		registerService(sqlServiceList, "sql-service");
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
				contents.add(new SqlTextNode(data));
				// log.info("-----------data:" + data);
			} else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = child.getNode().getNodeName();
				// log.info("-----------name:" + nodeName);
				NodeHandler handler = nodeHandlers.get(nodeName);
				if (handler == null) {
					throw new XmlParseException("Unknown element <" + nodeName + "> in SQL statement.");
				}
				handler.handleNode(child, contents);
			}
		}
		return contents;
	}

	private SelectResult parseSelectResult(String _resultType, String _resultMap) {
		Class<?> resultType = null;
		MappingVo resultMap = null;
		if (null == _resultType && null == _resultMap) {// 都没有值的情况下
			resultType = TangYuanContainer.getInstance().getDefaultResultType();// 这里是简单服务,直接只用系统默认即可
		} else if (null != _resultType && null != _resultMap) {// 都存在值的情况下
			// resultType处理
			if ("map".equalsIgnoreCase(_resultType)) {
				resultType = Map.class;
			} else if ("xco".equalsIgnoreCase(_resultType)) {
				resultType = XCO.class;
			} else {
				resultType = ClassUtils.forName(_resultType);
			}
			// resultMap处理
			resultMap = this.sqlContext.getMappingVoMap().get(_resultMap);
			if (null == resultMap) {
				throw new XmlParseException("Non-existent resultMap: " + _resultMap);
			}
			// 检测是否冲突
			if (null != resultMap.getBeanClass() && resultType != resultMap.getBeanClass()) {
				throw new XmlParseException("resultMap[" + resultMap.getBeanClass() + "] and resultType[" + resultType + "]类型冲突");
			}
		} else if (null == _resultType && null != _resultMap) {
			resultMap = this.sqlContext.getMappingVoMap().get(_resultMap);
			if (null == resultMap) {
				throw new XmlParseException("Non-existent resultMap: " + _resultMap);
			}
			if (null == resultMap.getBeanClass()) {
				resultType = TangYuanContainer.getInstance().getDefaultResultType();
			}
			// 具体的类型看resultMap.type
		} else if (null != _resultType && null == _resultMap) {
			if ("map".equalsIgnoreCase(_resultType)) {
				resultType = Map.class;
			} else if ("xco".equalsIgnoreCase(_resultType)) {
				resultType = XCO.class;
			} else {
				resultType = ClassUtils.forName(_resultType);
				// 默认Bean Result Mapping
			}
		}
		return new SelectResult(resultType, resultMap);
	}

	private List<AbstractServiceNode> buildSelectSetNodes(List<XmlNodeWrapper> contexts) {
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper context : contexts) {
			TangYuanNode sqlNode = parseNode(context, false);
			if (null != sqlNode) {
				String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
				existingService(id);

				String _resultType = StringUtils.trim(context.getStringAttribute("resultType"));
				String _resultMap = StringUtils.trim(context.getStringAttribute("resultMap"));
				SelectResult selectResult = parseSelectResult(_resultType, _resultMap);

				String _fetchSize = StringUtils.trim(context.getStringAttribute("fetchSize")); // xml v
				Integer fetchSize = null;
				if (null != _fetchSize) {
					fetchSize = Integer.valueOf(_fetchSize);
				}

				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
				dsKey = checkDsKey(dsKey, id);

				String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
				XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
				if (null == txDef) {
					throw new XmlParseException("service txRef is invalid: " + id);
				}

				String _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
				CacheUseVo cacheUse = null;
				if (null != _cacheUse && _cacheUse.length() > 0) {
					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
				}

				SelectSetNode selectSetNode = new SelectSetNode(id, ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey,
						fetchSize, txDef, sqlNode, cacheUse);
				list.add(selectSetNode);
			}
		}
		return list;
	}

	private List<AbstractServiceNode> buildSelectOneNodes(List<XmlNodeWrapper> contexts) {
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper context : contexts) {
			TangYuanNode sqlNode = parseNode(context, false);
			if (null != sqlNode) {
				String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
				existingService(id);

				String _resultType = StringUtils.trim(context.getStringAttribute("resultType"));
				String _resultMap = StringUtils.trim(context.getStringAttribute("resultMap"));
				SelectResult selectResult = parseSelectResult(_resultType, _resultMap);

				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
				dsKey = checkDsKey(dsKey, id);

				String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
				XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
				if (null == txDef) {
					throw new XmlParseException("service txRef is invalid: " + id);
				}

				String _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
				CacheUseVo cacheUse = null;
				if (null != _cacheUse && _cacheUse.length() > 0) {
					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
				}

				SelectOneNode selectOneNode = new SelectOneNode(id, ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey, txDef,
						sqlNode, cacheUse);
				list.add(selectOneNode);
			}
		}
		return list;
	}

	private List<AbstractServiceNode> buildSelectVarNodes(List<XmlNodeWrapper> contexts) {
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper context : contexts) {
			TangYuanNode sqlNode = parseNode(context, false);
			if (null != sqlNode) {
				String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
				existingService(id);

				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
				dsKey = checkDsKey(dsKey, id);

				String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
				XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
				if (null == txDef) {
					throw new XmlParseException("service txRef is invalid: " + id);
				}

				String _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
				CacheUseVo cacheUse = null;
				if (null != _cacheUse && _cacheUse.length() > 0) {
					cacheUse = parseCacheUse(_cacheUse, getFullId(id));
				}

				SelectVarNode selectVarNode = new SelectVarNode(id, ns, getFullId(id), dsKey, txDef, sqlNode, cacheUse);
				list.add(selectVarNode);
			}
		}
		return list;
	}

	private List<AbstractServiceNode> buildInsertNodes(List<XmlNodeWrapper> contexts) {
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper context : contexts) {
			TangYuanNode sqlNode = parseNode(context, false);
			if (null != sqlNode) {
				String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
				existingService(id);

				String _resultType = StringUtils.trim(context.getStringAttribute("resultType"));// 仅作标示作用
				Class<?> resultType = null;
				if (null != _resultType) {
					resultType = Object.class;
				}

				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
				dsKey = checkDsKey(dsKey, id);

				String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
				XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
				if (null == txDef) {
					throw new XmlParseException("service txRef is invalid: " + id);
				}

				String _cacheClean = StringUtils.trim(context.getStringAttribute("cacheClean"));
				CacheCleanVo cacheClean = null;
				if (null != _cacheClean && _cacheClean.length() > 0) {
					cacheClean = parseCacheClean(_cacheClean, getFullId(id));
				}

				InsertNode insertNode = new InsertNode(id, ns, getFullId(id), resultType, dsKey, txDef, sqlNode, cacheClean);
				list.add(insertNode);
			}
		}
		return list;
	}

	private List<AbstractServiceNode> buildUpdateNodes(List<XmlNodeWrapper> contexts) {
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper context : contexts) {
			TangYuanNode sqlNode = parseNode(context, false);
			if (null != sqlNode) {
				String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
				existingService(id);

				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
				dsKey = checkDsKey(dsKey, id);

				String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
				XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
				if (null == txDef) {
					throw new XmlParseException("service txRef is invalid: " + id);
				}

				String _cacheClean = StringUtils.trim(context.getStringAttribute("cacheClean"));
				CacheCleanVo cacheClean = null;
				if (null != _cacheClean && _cacheClean.length() > 0) {
					cacheClean = parseCacheClean(_cacheClean, getFullId(id));
				}

				UpdateNode updateNode = new UpdateNode(id, ns, getFullId(id), dsKey, txDef, sqlNode, cacheClean);
				list.add(updateNode);
			}
		}
		return list;
	}

	private List<AbstractServiceNode> buildDeleteNodes(List<XmlNodeWrapper> contexts) {
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper context : contexts) {
			TangYuanNode sqlNode = parseNode(context, false);
			if (null != sqlNode) {
				String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
				existingService(id);

				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
				dsKey = checkDsKey(dsKey, id);

				String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
				XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
				if (null == txDef) {
					throw new XmlParseException("service txRef is invalid: " + id);
				}

				String _cacheClean = StringUtils.trim(context.getStringAttribute("cacheClean"));
				CacheCleanVo cacheClean = null;
				if (null != _cacheClean && _cacheClean.length() > 0) {
					cacheClean = parseCacheClean(_cacheClean, getFullId(id));
				}

				DeleteNode deleteNode = new DeleteNode(id, ns, getFullId(id), dsKey, txDef, sqlNode, cacheClean);
				list.add(deleteNode);
			}
		}
		return list;
	}

	private List<AbstractServiceNode> buildSqlServiceNodes(List<XmlNodeWrapper> contexts) {
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper context : contexts) {
			String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
			existingService(id);

			String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
			XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
			if (null == txDef) {
				throw new XmlParseException("service txRef is invalid: " + id);
			}

			String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
			if (null != dsKey && !SqlComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
				throw new XmlParseException("service[" + id + "] uses an invalid dsKey: " + dsKey);
			}

			this.dsKeyWithSqlService = dsKey;

			String _resultType = StringUtils.trim(context.getStringAttribute("resultType"));
			this.serviceResultType = null;
			if ("map".equalsIgnoreCase(_resultType)) {
				this.serviceResultType = Map.class;
			} else if ("xco".equalsIgnoreCase(_resultType)) {
				this.serviceResultType = XCO.class;
			} else {
				this.serviceResultType = TangYuanContainer.getInstance().getDefaultResultType();
			}

			String _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
			CacheUseVo cacheUse = null;
			if (null != _cacheUse && _cacheUse.length() > 0) {
				cacheUse = parseCacheUse(_cacheUse, getFullId(id));
			}
			String _cacheClean = StringUtils.trim(context.getStringAttribute("cacheClean"));
			CacheCleanVo cacheClean = null;
			if (null != _cacheClean && _cacheClean.length() > 0) {
				cacheClean = parseCacheClean(_cacheClean, getFullId(id));
			}

			TangYuanNode sqlNode = parseNode(context, true);
			if (null != sqlNode) {
				ServiceNode serviceNode = new ServiceNode(id, txRef, getFullId(id), dsKey, txDef, sqlNode, cacheUse, cacheClean,
						this.serviceResultType);
				list.add(serviceNode);
			}
		}
		return list;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected interface NodeHandler {
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

	private class IncludeHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			String refKey = nodeToHandle.getStringAttribute("ref"); // xml V
			TangYuanNode refNode = sqlContext.getXmlContext().getIntegralRefMap().get(refKey);
			if (null == refNode) {
				throw new XmlParseException("The referenced node is null: " + refKey);
			}

			// 增加段的引用
			if (refNode instanceof SegmentNode) {
				XmlNodeWrapper innerNode = ((SegmentNode) refNode).getNode();
				refNode = parseNode(innerNode, true);
				if (null == refNode) {
					log.warn("The referenced segment is empty, ref: " + refKey);
					return;
				}
			}

			targetContents.add(refNode);
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

			if (null == sqlNode && null == open && null == close && null == separator) {
				open = "(";
				close = ")";
				separator = ",";
			}
			if (null == sqlNode) {
				if (null == index) {
					index = "i";
				}
				sqlNode = new SqlTextNode("#{" + collection + "[" + index + "]}");
			}

			ForEachNode forEachNode = new SqlForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator);
			targetContents.add(forEachNode);
		}
	}

	private class SetVarHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			// <setvar key="{x}" value="100" type="Integer" />
			String key = StringUtils.trim(nodeToHandle.getStringAttribute("key")); // xml v
			String _value = StringUtils.trim(nodeToHandle.getStringAttribute("value")); // xml v
			String type = StringUtils.trim(nodeToHandle.getStringAttribute("type")); // xml v
			if (!checkVar(key)) {
				throw new XmlParseException("<setvar> node key is not legal, should be {xxx}.");
			}
			key = getRealVal(key);
			Object value = null;
			boolean constant = true;
			if (checkVar(_value)) {
				constant = false;
				value = new NormalParser().parse(getRealVal(_value));
			} else {
				value = getSetVarValue(_value, type);
			}
			SetVarNode setVarNode = new SetVarNode(key, value, constant);
			targetContents.add(setVarNode);
		}
	}

	private class LogHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			String message = StringUtils.trim(nodeToHandle.getStringAttribute("message")); // xml v
			String _level = StringUtils.trim(nodeToHandle.getStringAttribute("level")); // xml c
			int level = 3;
			if (null != _level) {
				level = getLogLevel(_level);
			}
			LogNode logNode = new LogNode(level, message);
			targetContents.add(logNode);
		}
	}

	private class ReturnHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			// Variable result = null;
			Object result = null;
			String _result = StringUtils.trim(nodeToHandle.getStringAttribute("value"));
			if (null != _result) {
				if (checkVar(_result)) {
					result = new NormalParser().parse(getRealVal(_result));
				} else {
					result = parseValue(_result);
				}
			}

			List<XmlNodeWrapper> properties = nodeToHandle.evalNodes("property");
			List<PropertyItem> resultList = buildPropertyItem(properties, "return");

			if (null != result && null != resultList) {
				throw new XmlParseException("<return> node in the result | property can only choose a way.");
			}

			ReturnNode returnNode = new ReturnNode(result, resultList, serviceResultType);
			targetContents.add(returnNode);
		}
	}

	private class ThrowHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			String test = StringUtils.trim(nodeToHandle.getStringAttribute("test")); // xml v
			String code = StringUtils.trim(nodeToHandle.getStringAttribute("code")); // xml v
			String message = StringUtils.trim(nodeToHandle.getStringAttribute("message"));
			String i18n = StringUtils.trim(nodeToHandle.getStringAttribute("i18n"));
			if (null == test || null == code) {
				throw new XmlParseException("In the Exception node, the test, code attribute can not be empty.");
			}
			targetContents.add(new ExceptionNode(new LogicalExprParser().parse(test), Integer.parseInt(code), message, i18n));
		}
	}

	private class CallHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			String serviceId = StringUtils.trim(nodeToHandle.getStringAttribute("service"));
			if (null == serviceId) {
				throw new XmlParseException("The service attribute in the call node can not be empty");
			}

			// fix: 新增变量调用功能
			Object service = serviceId;
			if (checkVar(serviceId)) {
				// service = new NormalParser().parse(serviceId);
				service = new NormalParser().parse(getRealVal(serviceId));
			}

			String resultKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("resultKey")));
			String _mode = StringUtils.trim(nodeToHandle.getStringAttribute("mode"));// xml v

			CallMode mode = null;// 增加新的默认模式
			if (null != _mode) {
				mode = getCallMode(_mode);
			}

			// String exResultKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("exResultKey")));

			String codeKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("codeKey")));
			String messageKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("messageKey")));

			List<XmlNodeWrapper> properties = nodeToHandle.evalNodes("property");
			List<PropertyItem> itemList = buildPropertyItem(properties, "call");

			// service id可以放在运行期间检查
			// targetContents.add(new CallNode(service, resultKey, mode, itemList, exResultKey));
			targetContents.add(new CallNode(service, resultKey, mode, itemList, codeKey, messageKey));
		}
	}

	private class SelectSetHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
			if (null != sqlNode) {
				String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
				if (null == dsKey) {
					dsKey = dsKeyWithSqlService;
				} else {
					checkInnerDsKey(dsKey, "SelectSet");
				}

				String resultKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("resultKey")));
				Integer fetchSize = null;
				String _fetchSize = StringUtils.trim(nodeToHandle.getStringAttribute("fetchSize"));
				if (null != _fetchSize) {
					fetchSize = Integer.valueOf(_fetchSize);
				}

				String _cacheUse = StringUtils.trim(nodeToHandle.getStringAttribute("cacheUse"));
				CacheUseVo cacheUse = null;
				if (null != _cacheUse && _cacheUse.length() > 0) {
					cacheUse = parseCacheUse(_cacheUse, "");
				}

				InternalSelectSetNode selectSetNode = new InternalSelectSetNode(dsKey, resultKey, sqlNode, serviceResultType, fetchSize, cacheUse);
				targetContents.add(selectSetNode);
			}
		}
	}

	private class SelectOneHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
			if (null != sqlNode) {
				String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
				if (null == dsKey) {
					dsKey = dsKeyWithSqlService;
				} else {
					checkInnerDsKey(dsKey, "SelectOne");
				}
				String resultKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("resultKey")));

				String _cacheUse = StringUtils.trim(nodeToHandle.getStringAttribute("cacheUse"));
				CacheUseVo cacheUse = null;
				if (null != _cacheUse && _cacheUse.length() > 0) {
					cacheUse = parseCacheUse(_cacheUse, "");
				}

				InternalSelectOneNode selectOneNode = new InternalSelectOneNode(dsKey, resultKey, sqlNode, serviceResultType, cacheUse);
				targetContents.add(selectOneNode);
			}
		}
	}

	private class SelectVarHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
			if (null != sqlNode) {
				String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
				if (null == dsKey) {
					dsKey = dsKeyWithSqlService;
				} else {
					checkInnerDsKey(dsKey, "SelectVar");
				}
				String resultKey = getResultKey(StringUtils.trim(nodeToHandle.getStringAttribute("resultKey")));

				String _cacheUse = StringUtils.trim(nodeToHandle.getStringAttribute("cacheUse"));
				CacheUseVo cacheUse = null;
				if (null != _cacheUse && _cacheUse.length() > 0) {
					cacheUse = parseCacheUse(_cacheUse, "");
				}

				InternalSelectVarNode selectVarNode = new InternalSelectVarNode(dsKey, resultKey, sqlNode, cacheUse);
				targetContents.add(selectVarNode);
			}
		}
	}

	private class DeleteHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
			if (null != sqlNode) {
				String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
				if (null == dsKey) {
					dsKey = dsKeyWithSqlService;
				} else {
					checkInnerDsKey(dsKey, "Delete");
				}
				String resultKey = StringUtils.trim(nodeToHandle.getStringAttribute("rowCount"));
				if (null != resultKey) {
					if (!checkVar(resultKey)) {
						throw new XmlParseException("<delete> rowCount is not legal, should be {xxx}.");
					}
					resultKey = getRealVal(resultKey);
				}

				String _cacheClean = StringUtils.trim(nodeToHandle.getStringAttribute("cacheClean"));
				CacheCleanVo cacheClean = null;
				if (null != _cacheClean && _cacheClean.length() > 0) {
					cacheClean = parseCacheClean(_cacheClean, "");
				}

				InternalDeleteNode deleteNode = new InternalDeleteNode(dsKey, resultKey, sqlNode, cacheClean);
				targetContents.add(deleteNode);
			}
		}
	}

	private class UpdateHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
			if (null != sqlNode) {
				String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
				if (null == dsKey) {
					dsKey = dsKeyWithSqlService;
				} else {
					checkInnerDsKey(dsKey, "update");
				}
				String resultKey = StringUtils.trim(nodeToHandle.getStringAttribute("rowCount"));
				if (null != resultKey) {
					if (!checkVar(resultKey)) {
						throw new XmlParseException("<update> rowCount is not legal, should be {xxx}.");
					}
					resultKey = getRealVal(resultKey);
				}

				String _cacheClean = StringUtils.trim(nodeToHandle.getStringAttribute("cacheClean"));
				CacheCleanVo cacheClean = null;
				if (null != _cacheClean && _cacheClean.length() > 0) {
					cacheClean = parseCacheClean(_cacheClean, "");
				}

				InternalUpdateNode updateNode = new InternalUpdateNode(dsKey, resultKey, sqlNode, cacheClean);
				targetContents.add(updateNode);
			}
		}
	}

	private class InsertHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
			if (null != sqlNode) {
				String dsKey = StringUtils.trim(nodeToHandle.getStringAttribute("dsKey"));
				if (null == dsKey) {
					dsKey = dsKeyWithSqlService;
				} else {
					checkInnerDsKey(dsKey, "insert");
				}
				String resultKey = StringUtils.trim(nodeToHandle.getStringAttribute("rowCount"));
				if (null != resultKey) {
					if (!checkVar(resultKey)) {
						throw new XmlParseException("<insert> rowCount is not legal, should be {xxx}.");
					}
					resultKey = getRealVal(resultKey);
				}

				String incrementKey = StringUtils.trim(nodeToHandle.getStringAttribute("incrementKey"));
				if (null != incrementKey) {
					if (!checkVar(incrementKey)) {
						throw new XmlParseException("<insert> incrementKey is not legal, should be {xxx}.");
					}
					incrementKey = getRealVal(incrementKey);
				}

				String _cacheClean = StringUtils.trim(nodeToHandle.getStringAttribute("cacheClean"));
				CacheCleanVo cacheClean = null;
				if (null != _cacheClean && _cacheClean.length() > 0) {
					cacheClean = parseCacheClean(_cacheClean, "");
				}

				InternalInsertNode insertNode = new InternalInsertNode(dsKey, resultKey, incrementKey, sqlNode, cacheClean);
				targetContents.add(insertNode);
			}
		}
	}

	private class TransGroupHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
			String txRef = StringUtils.trim(nodeToHandle.getStringAttribute("txRef"));
			XTransactionDefinition txDef = sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, null, null);
			if (null == txDef) {
				throw new XmlParseException("txRef is invalid:" + txRef);
			}
			if (!txDef.isNewTranscation()) {
				throw new XmlParseException("TransGroup中的事务定义必须为[REQUIRES_NEW|NOT_SUPPORTED]");
			}
			TangYuanNode sqlNode = parseNode(nodeToHandle, true);
			if (null != sqlNode) {
				TransGroupNode transGroupNode = new TransGroupNode(txDef, sqlNode);
				targetContents.add(transGroupNode);
			}
		}
	}

	private Map<String, NodeHandler> nodeHandlers = new HashMap<String, NodeHandler>() {
		private static final long serialVersionUID = 1L;

		{
			put("foreach", new ForEachHandler());
			put("if", new IfHandler());
			put("else", new ElseHandler());
			put("elseif", new ElseIfHandler());
			put("include", new IncludeHandler());
			put("exception", new ThrowHandler());
			put("return", new ReturnHandler());
			put("setvar", new SetVarHandler());
			put("log", new LogHandler());
			put("selectSet", new SelectSetHandler());
			put("selectOne", new SelectOneHandler());
			put("selectVar", new SelectVarHandler());
			put("update", new UpdateHandler());
			put("delete", new DeleteHandler());
			put("insert", new InsertHandler());
			// put("procedure", new ProcedureHandler());
			put("transGroup", new TransGroupHandler());
			put("call", new CallHandler());
		}
	};

}
