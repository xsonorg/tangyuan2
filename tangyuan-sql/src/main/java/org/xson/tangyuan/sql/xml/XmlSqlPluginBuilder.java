package org.xson.tangyuan.sql.xml;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.sql.SqlComponent;
import org.xson.tangyuan.sql.transaction.XTransactionDefinition;
import org.xson.tangyuan.sql.xml.node.DeleteNode;
import org.xson.tangyuan.sql.xml.node.InsertNode;
import org.xson.tangyuan.sql.xml.node.InternalDeleteNode;
import org.xson.tangyuan.sql.xml.node.InternalInsertNode;
import org.xson.tangyuan.sql.xml.node.InternalSelectOneNode;
import org.xson.tangyuan.sql.xml.node.InternalSelectSetNode;
import org.xson.tangyuan.sql.xml.node.InternalSelectVarNode;
import org.xson.tangyuan.sql.xml.node.InternalUpdateNode;
import org.xson.tangyuan.sql.xml.node.SelectOneNode;
import org.xson.tangyuan.sql.xml.node.SelectSetNode;
import org.xson.tangyuan.sql.xml.node.SelectVarNode;
import org.xson.tangyuan.sql.xml.node.ServiceNode;
import org.xson.tangyuan.sql.xml.node.SqlForEachNode;
import org.xson.tangyuan.sql.xml.node.SqlForNode;
import org.xson.tangyuan.sql.xml.node.SqlTextNode;
import org.xson.tangyuan.sql.xml.node.TransGroupNode;
import org.xson.tangyuan.sql.xml.node.UpdateNode;
import org.xson.tangyuan.xml.DefaultXmlPluginBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.SegmentNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class XmlSqlPluginBuilder extends DefaultXmlPluginBuilder {

	private XmlSqlContext componentContext    = null;
	private String        idWithSqlService    = null;
	private String        dsKeyWithSqlService = null;
	private Class<?>      serviceResultType   = null;

	@Override
	public void setContext(String resource, XmlContext xmlContext) throws Throwable {
		this.componentContext = (XmlSqlContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "sqlservices", false);
		if (this.ns.length() > 0) {
			checkNs(this.ns);
		}
		initNodeHandler();
	}

	@Override
	public void clean() {
		super.clean();

		this.idWithSqlService = null;
		this.dsKeyWithSqlService = null;
		this.serviceResultType = null;
		this.componentContext = null;
	}

	@Override
	public void parseRef() {
		log.info(lang("xml.start.parsing.type", "plugin[ref]", this.resource));
		// 解析sql节点
		buildSqlNode(this.root.evalNodes("sql"));
		// 解析segment节点, 增加段定义和引用
		buildSegmentNode(this.root.evalNodes("segment"));
	}

	@Override
	public void parseService() {
		//		info("*** Start parsing(service): {}", resource);
		log.info(lang("xml.start.parsing.type", "plugin[service]", this.resource));
		configurationElement();
	}

	@Override
	protected Class<?> getServiceResultType() {
		return this.serviceResultType;
	}

	private void initNodeHandler() {
		nodeHandlers.put("foreach", new ForEachHandler());
		nodeHandlers.put("for", new ForHandler());
		nodeHandlers.put("if", new IfHandler());
		nodeHandlers.put("else", new ElseHandler());
		nodeHandlers.put("elseif", new ElseIfHandler());
		nodeHandlers.put("include", new IncludeHandler());
		nodeHandlers.put("exception", new ThrowHandler());
		nodeHandlers.put("return", new ReturnHandler());
		nodeHandlers.put("setvar", new SetVarHandler());
		nodeHandlers.put("log", new LogHandler());
		nodeHandlers.put("selectSet", new SelectSetHandler());
		nodeHandlers.put("selectOne", new SelectOneHandler());
		nodeHandlers.put("selectVar", new SelectVarHandler());
		nodeHandlers.put("update", new UpdateHandler());
		nodeHandlers.put("delete", new DeleteHandler());
		nodeHandlers.put("insert", new InsertHandler());
		// put("procedure", new ProcedureHandler());
		nodeHandlers.put("transGroup", new TransGroupHandler());
		nodeHandlers.put("call", new CallHandler());
	}

	private String checkReferencedDsKey(String dsKey, String tagName, String id) {
		if (null == dsKey) {
			String defaultDsKey = SqlComponent.getInstance().getDataSourceManager().getDefaultDsKey();
			if (null == defaultDsKey) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", "null", id, "dsKey", tagName, this.resource));
			}
			return defaultDsKey;
		}
		if (!SqlComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
			throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", dsKey, id, "dsKey", tagName, this.resource));
		}
		return dsKey;
	}

	private void checkInnerDsKey(String dsKey, String tagName) {
		if (!SqlComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
			throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", dsKey, "dsKey", tagName, this.resource));
		}
	}

	/** 扫描REF */
	private void buildSqlNode(List<XmlNodeWrapper> contexts) {
		String tagName = "sql";
		for (XmlNodeWrapper xNode : contexts) {
			String id     = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String fullId = getFullId(id);
			if (this.integralRefMap.containsKey(fullId)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null != sqlNode) {
				this.integralRefMap.put(fullId, sqlNode);
				log.info(lang("add.tag.service", tagName, fullId));
			}
		}
	}

	/** 扫描Segment */
	private void buildSegmentNode(List<XmlNodeWrapper> contexts) {
		String tagName = "segment";
		for (XmlNodeWrapper xNode : contexts) {
			String id     = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String fullId = getFullId(id);
			if (this.integralRefMap.containsKey(fullId)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			TangYuanNode sqlNode = new SegmentNode(xNode);
			this.integralRefMap.put(fullId, sqlNode);
			log.info(lang("add.tag.service", tagName, fullId));
		}
	}

	private void configurationElement() {
		List<AbstractServiceNode> selectSetList  = buildSelectSetNode(this.root.evalNodes("selectSet"));
		List<AbstractServiceNode> selectOneList  = buildSelectOneNode(this.root.evalNodes("selectOne"));
		List<AbstractServiceNode> selectVarList  = buildSelectVarNode(this.root.evalNodes("selectVar"));
		List<AbstractServiceNode> insertList     = buildInsertNode(this.root.evalNodes("insert"));
		List<AbstractServiceNode> updateList     = buildUpdateNode(this.root.evalNodes("update"));
		List<AbstractServiceNode> deleteList     = buildDeleteNode(this.root.evalNodes("delete"));
		List<AbstractServiceNode> sqlServiceList = buildSqlServiceNode(this.root.evalNodes("sql-service"));

		registerService(selectSetList, "selectSet");
		registerService(selectOneList, "selectOne");
		registerService(selectVarList, "selectVar");
		registerService(insertList, "insert");
		registerService(updateList, "update");
		registerService(deleteList, "delete");
		registerService(sqlServiceList, "sql-service");
	}

	@Override
	protected TangYuanNode getTextNode(String data) {
		return new SqlTextNode(data);
	}

	//	protected List<TangYuanNode> parseDynamicTags(XmlNodeWrapper node) {
	//		List<TangYuanNode> contents = new ArrayList<TangYuanNode>();
	//		NodeList           children = node.getNode().getChildNodes();
	//		for (int i = 0; i < children.getLength(); i++) {
	//			XmlNodeWrapper child = node.newXMlNode(children.item(i));
	//			if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
	//				String data = child.getStringBody("");
	//				if (StringUtils.isEmptySafe(data)) {
	//					continue;
	//				}
	//				// 使用新的sqlText节点
	//				contents.add(new SqlTextNode(data));
	//				// log.info("-----------data:" + data);
	//			} else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) {
	//				String      nodeName = child.getNode().getNodeName();
	//				// log.info("-----------name:" + nodeName);
	//				NodeHandler handler  = nodeHandlers.get(nodeName);
	//				if (handler == null) {
	//					// throw new XmlParseException("Unknown element <" + nodeName + "> in SQL statement.");
	//					throw new XmlParseException(lang("xml.tag.unsupported", nodeName, this.resource));
	//				}
	//				handler.handleNode(child, contents);
	//			}
	//		}
	//		return contents;
	//	}
	//	private String parseVariableKey(XmlNodeWrapper xNode, String tagName) {
	//		return parseVariableKey(xNode, "resultType", tagName);
	//	}
	//	private String parseVariableKey(XmlNodeWrapper xNode, String attributeName, String tagName) {
	//		String resultKey = getStringFromAttr(xNode, attributeName);
	//		if (null != resultKey) {
	//			if (!checkVar(resultKey)) {
	//				throw new XmlParseException(lang("xml.tag.attribute.invalid.should", resultKey, "{xxx}", tagName, this.resource));
	//			}
	//			resultKey = getRealVal(resultKey);
	//		}
	//		return resultKey;
	//	}

	private CacheUseVo parseCacheUse(String cacheUse, String id) {
		if (null == cacheUse) {
			return null;
		}
		return CacheUseVo.parseCacheUse(cacheUse, getFullId(id));
	}

	private CacheCleanVo parseCacheClean(String cacheClean, String id) {
		if (null == cacheClean) {
			return null;
		}
		return CacheCleanVo.parseCacheClean(cacheClean, getFullId(id));
	}

	private List<AbstractServiceNode> buildSelectSetNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "selectSet";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {
			//			TangYuanNode sqlNode = parseNode(xNode, false);
			//			if (null == sqlNode) {
			//				continue;
			//			}

			String       id         = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       resultType = getStringFromAttr(xNode, "resultType");
			String       resultMap  = getStringFromAttr(xNode, "resultMap");
			Integer      fetchSize  = getIntegerFromAttr(xNode, "fetchSize");
			String       dsKey      = getStringFromAttr(xNode, "dsKey");
			String       txRef      = getStringFromAttr(xNode, "txRef");
			String       _cacheUse  = getStringFromAttr(xNode, "cacheUse");
			// 新增,每个服务节点都需要包含的
			String       desc       = getStringFromAttr(xNode, "desc");
			String[]     groups     = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode    = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			XTransactionDefinition txDef = this.componentContext.getTransactionMatcher().getTransactionDefinition(txRef, id, tagName);
			if (null == txDef) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", txRef, id, "txRef", tagName, this.resource));
			}

			SelectResult  selectResult  = parseSelectResult(resultType, resultMap, tagName, this.componentContext);
			CacheUseVo    cacheUse      = parseCacheUse(_cacheUse, id);
			//					CacheUseVo.parseCacheUse(_cacheUse, getFullId(id));

			SelectSetNode selectSetNode = new SelectSetNode(id, this.ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey, fetchSize, txDef, sqlNode, cacheUse,
					desc, groups);
			list.add(selectSetNode);

		}
		return list;
	}

	private List<AbstractServiceNode> buildSelectOneNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "selectOne";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			//			TangYuanNode sqlNode = parseNode(xNode, false);
			//			if (null == sqlNode) {
			//				continue;
			//			}

			String       id         = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       resultType = getStringFromAttr(xNode, "resultType");
			String       resultMap  = getStringFromAttr(xNode, "resultMap");
			String       dsKey      = getStringFromAttr(xNode, "dsKey");
			String       txRef      = getStringFromAttr(xNode, "txRef");
			String       _cacheUse  = getStringFromAttr(xNode, "cacheUse");
			// 新增,每个服务节点都需要包含的
			String       desc       = getStringFromAttr(xNode, "desc");
			String[]     groups     = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode    = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);
			XTransactionDefinition txDef = this.componentContext.getTransactionMatcher().getTransactionDefinition(txRef, id, tagName);
			if (null == txDef) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", txRef, id, "txRef", tagName, this.resource));
			}

			SelectResult  selectResult  = parseSelectResult(resultType, resultMap, tagName, this.componentContext);
			CacheUseVo    cacheUse      = parseCacheUse(_cacheUse, id);

			SelectOneNode selectOneNode = new SelectOneNode(id, this.ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey, txDef, sqlNode, cacheUse, desc,
					groups);

			list.add(selectOneNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildSelectVarNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "selectVar";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {
			//			TangYuanNode sqlNode = parseNode(xNode, false);
			//			if (null == sqlNode) {
			//				continue;
			//			}

			String       id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       dsKey     = getStringFromAttr(xNode, "dsKey");
			String       txRef     = getStringFromAttr(xNode, "txRef");
			String       _cacheUse = getStringFromAttr(xNode, "cacheUse");
			// 新增,每个服务节点都需要包含的
			String       desc      = getStringFromAttr(xNode, "desc");
			String[]     groups    = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode   = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);
			XTransactionDefinition txDef = this.componentContext.getTransactionMatcher().getTransactionDefinition(txRef, id, tagName);
			if (null == txDef) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", txRef, id, "txRef", tagName, this.resource));
			}

			CacheUseVo    cacheUse      = parseCacheUse(_cacheUse, id);
			SelectVarNode selectVarNode = new SelectVarNode(id, this.ns, getFullId(id), dsKey, txDef, sqlNode, cacheUse, desc, groups);
			list.add(selectVarNode);
		}

		return list;
	}

	private List<AbstractServiceNode> buildInsertNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "insert";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String       id           = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       dsKey        = getStringFromAttr(xNode, "dsKey");
			String       txRef        = getStringFromAttr(xNode, "txRef");
			//			String   _resultType = getStringFromAttr(xNode, "resultType");

			String       resultKey    = parseVariableKey(xNode, "rowCount", tagName);
			String       incrementKey = parseVariableKey(xNode, "incrementKey", tagName);

			String       _cacheClean  = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String       desc         = getStringFromAttr(xNode, "desc");
			String[]     groups       = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode      = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);
			XTransactionDefinition txDef = this.componentContext.getTransactionMatcher().getTransactionDefinition(txRef, id, tagName);
			if (null == txDef) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", txRef, id, "txRef", tagName, this.resource));
			}
			//			Class<?> resultType = null;
			//			if (null != _resultType) {
			//				resultType = Object.class;
			//			}
			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, id);

			//InsertNode   insertNode = new InsertNode(id, this.ns, getFullId(id), resultType, dsKey, txDef, sqlNode, cacheClean, desc, groups);
			InsertNode   insertNode = new InsertNode(id, this.ns, getFullId(id), resultKey, incrementKey, dsKey, txDef, sqlNode, cacheClean, desc, groups);
			list.add(insertNode);

		}
		return list;
	}

	private List<AbstractServiceNode> buildUpdateNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "update";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			//			TangYuanNode sqlNode = parseNode(xNode, false);
			//			if (null == sqlNode) {
			//				continue;
			//			}

			String       id          = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       dsKey       = getStringFromAttr(xNode, "dsKey");
			String       txRef       = getStringFromAttr(xNode, "txRef");
			String       _cacheClean = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String       desc        = getStringFromAttr(xNode, "desc");
			String[]     groups      = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode     = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);
			XTransactionDefinition txDef = this.componentContext.getTransactionMatcher().getTransactionDefinition(txRef, id, tagName);
			if (null == txDef) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", txRef, id, "txRef", tagName, this.resource));
			}
			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, id);

			UpdateNode   updateNode = new UpdateNode(id, this.ns, getFullId(id), dsKey, txDef, sqlNode, cacheClean, desc, groups);
			list.add(updateNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildDeleteNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "delete";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			//			TangYuanNode sqlNode = parseNode(xNode, false);
			//			if (null == sqlNode) {
			//				continue;
			//			}

			String       id          = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String       dsKey       = getStringFromAttr(xNode, "dsKey");
			String       txRef       = getStringFromAttr(xNode, "txRef");
			String       _cacheClean = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String       desc        = getStringFromAttr(xNode, "desc");
			String[]     groups      = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode     = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);
			XTransactionDefinition txDef = this.componentContext.getTransactionMatcher().getTransactionDefinition(txRef, id, tagName);
			if (null == txDef) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", txRef, id, "txRef", tagName, this.resource));
			}
			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, id);

			DeleteNode   deleteNode = new DeleteNode(id, this.ns, getFullId(id), dsKey, txDef, sqlNode, cacheClean, desc, groups);
			list.add(deleteNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildSqlServiceNode(List<XmlNodeWrapper> contexts) {
		String                    tagName = "sql-service";
		List<AbstractServiceNode> list    = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String   id          = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			//			String   resultType  = getStringFromAttr(xNode, "resultType");
			String   dsKey       = getStringFromAttr(xNode, "dsKey");
			String   txRef       = getStringFromAttr(xNode, "txRef");
			String   _cacheUse   = getStringFromAttr(xNode, "cacheUse");
			String   _cacheClean = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String   desc        = getStringFromAttr(xNode, "desc");
			String[] groups      = getStringArrayFromAttr(xNode, "group");

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);
			XTransactionDefinition txDef = this.componentContext.getTransactionMatcher().getTransactionDefinition(txRef, id, tagName);
			if (null == txDef) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", txRef, id, "txRef", tagName, this.resource));
			}

			CacheUseVo   cacheUse   = parseCacheUse(_cacheUse, id);
			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, id);

			//			this.serviceResultType = null;
			//			if ("xco".equalsIgnoreCase(resultType)) {
			//				this.serviceResultType = XCO.class;
			//			} else if ("map".equalsIgnoreCase(resultType)) {
			//				this.serviceResultType = Map.class;
			//			} else {
			//				this.serviceResultType = TangYuanContainer.getInstance().getDefaultResultType();
			//			}

			this.idWithSqlService = id;
			this.dsKeyWithSqlService = dsKey;
			this.serviceResultType = TangYuanContainer.getInstance().getDefaultResultType();

			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			//			TangYuanNode sqlNode = parseNode(xNode, false);
			//			if (null != sqlNode) {
			//				ServiceNode serviceNode = new ServiceNode(id, txRef, getFullId(id), dsKey, txDef, sqlNode, cacheUse, cacheClean, this.serviceResultType, desc, groups);
			//				list.add(serviceNode);
			//			}

			ServiceNode serviceNode = new ServiceNode(id, this.ns, getFullId(id), dsKey, txDef, sqlNode, cacheUse, cacheClean, this.serviceResultType, desc, groups);
			list.add(serviceNode);
		}
		return list;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private class SelectSetHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "selectSet";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				//				return;
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			Integer fetchSize = getIntegerFromAttr(xNode, "fetchSize");
			String  dsKey     = getStringFromAttr(xNode, "dsKey");
			String  resultMap = getStringFromAttr(xNode, "resultMap");
			String  _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String  resultKey = parseVariableKey(xNode, "resultKey", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			SelectResult          selectResult  = parseSelectResult(null, resultMap, tagName, componentContext);
			//			CacheUseVo            cacheUse      = parseCacheUse(_cacheUse, "");
			CacheUseVo            cacheUse      = parseCacheUse(_cacheUse, idWithSqlService);
			InternalSelectSetNode selectSetNode = new InternalSelectSetNode(dsKey, resultKey, sqlNode, serviceResultType, selectResult.resultMap, fetchSize, cacheUse);
			targetContents.add(selectSetNode);
		}
	}

	private class SelectOneHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "selectOne";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey     = getStringFromAttr(xNode, "dsKey");
			String resultMap = getStringFromAttr(xNode, "resultMap");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String  resultKey = parseVariableKey(xNode, "resultKey", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			SelectResult          selectResult  = parseSelectResult(null, resultMap, tagName, componentContext);
			CacheUseVo            cacheUse      = parseCacheUse(_cacheUse, idWithSqlService);
			InternalSelectOneNode selectOneNode = new InternalSelectOneNode(dsKey, resultKey, sqlNode, serviceResultType, selectResult.resultMap, cacheUse);
			targetContents.add(selectOneNode);
		}
	}

	private class SelectVarHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "selectVar";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey     = getStringFromAttr(xNode, "dsKey");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String  resultKey = parseVariableKey(xNode, "resultKey", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			CacheUseVo            cacheUse      = parseCacheUse(_cacheUse, idWithSqlService);
			InternalSelectVarNode selectVarNode = new InternalSelectVarNode(dsKey, resultKey, sqlNode, cacheUse);
			targetContents.add(selectVarNode);
		}
	}

	private class DeleteHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "delete";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey       = getStringFromAttr(xNode, "dsKey");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");
			String resultKey   = parseVariableKey(xNode, "rowCount", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			CacheCleanVo       cacheClean = parseCacheClean(_cacheClean, idWithSqlService);
			InternalDeleteNode deleteNode = new InternalDeleteNode(dsKey, resultKey, sqlNode, cacheClean);
			targetContents.add(deleteNode);
		}
	}

	private class UpdateHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "update";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey       = getStringFromAttr(xNode, "dsKey");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");
			String resultKey   = parseVariableKey(xNode, "rowCount", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			CacheCleanVo       cacheClean = parseCacheClean(_cacheClean, idWithSqlService);
			InternalUpdateNode updateNode = new InternalUpdateNode(dsKey, resultKey, sqlNode, cacheClean);
			targetContents.add(updateNode);
		}
	}

	private class InsertHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String       tagName = "insert";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey        = getStringFromAttr(xNode, "dsKey");
			String _cacheClean  = getStringFromAttr(xNode, "cacheClean");
			String resultKey    = parseVariableKey(xNode, "rowCount", tagName);
			String incrementKey = parseVariableKey(xNode, "incrementKey", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			CacheCleanVo       cacheClean = parseCacheClean(_cacheClean, idWithSqlService);
			InternalInsertNode insertNode = new InternalInsertNode(dsKey, resultKey, incrementKey, sqlNode, cacheClean);
			targetContents.add(insertNode);
		}
	}

	private class TransGroupHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String                 tagName = "transGroup";
			String                 txRef   = getStringFromAttr(xNode, "txRef");
			XTransactionDefinition txDef   = componentContext.getTransactionMatcher().getTransactionDefinition(txRef, null, null);
			if (null == txDef) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", txRef, "txRef", tagName, resource));
			}
			if (!txDef.isNewTranscation()) {
				// TODO
				throw new XmlParseException("TransGroup中的事务定义必须为[REQUIRES_NEW|NOT_SUPPORTED]");
			}
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null != sqlNode) {
				TransGroupNode transGroupNode = new TransGroupNode(txDef, sqlNode);
				targetContents.add(transGroupNode);
			}
		}
	}

	protected class ForEachHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {

			String tagName    = "foreach";

			String collection = getStringFromAttr(xNode, "collection", lang("xml.tag.attribute.empty", "collection", tagName, resource));
			String index      = parseVariableKey(xNode, "index", tagName);
			String open       = getStringFromAttr(xNode, "open");
			String close      = getStringFromAttr(xNode, "close");
			String separator  = getStringFromAttr(xNode, "separator");

			collection = parseVariableKey(xNode, "collection", tagName);

			//			List<TangYuanNode> contents = parseDynamicTags(xNode);
			//			int                size     = contents.size();
			//			TangYuanNode       sqlNode  = null;
			//			if (1 == size) {
			//				sqlNode = contents.get(0);
			//			} else if (size > 1) {
			//				sqlNode = new MixedNode(contents);
			//			} else {
			//				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			//			}
			// ForEachNode forEachNode = new SqlForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator, start, end, pLen, ignoreIOOB, indexMode);

			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			}

			ForEachNode forEachNode = new SqlForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator);
			targetContents.add(forEachNode);
		}
	}

	protected class ForHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {

			String tagName   = "for";

			String index     = getStringFromAttr(xNode, "index", lang("xml.tag.attribute.empty", "index", tagName, resource));
			String open      = getStringFromAttr(xNode, "open");
			String close     = getStringFromAttr(xNode, "close");
			String separator = getStringFromAttr(xNode, "separator");
			String _start    = getStringFromAttr(xNode, "start");
			String _end      = getStringFromAttr(xNode, "end", lang("xml.tag.attribute.empty", "end", tagName, resource));

			index = parseVariableKey(xNode, "index", tagName);

			Object start = 0;
			Object end   = null;

			if (null != _start) {
				if (checkVar(_start)) {
					//start = new GAParserWarper().parse(getRealVal(_start));
					start = parseVariableUseGA(_start);
				} else {
					start = Integer.parseInt(_start);
				}
			}
			//			else {
			//				start = 0;
			//			}

			if (checkVar(_end)) {
				end = parseVariableUseGA(_end);
			} else {
				end = Integer.parseInt(_end);
			}

			//			List<TangYuanNode> contents = parseDynamicTags(xNode);
			//			int                size     = contents.size();
			//			TangYuanNode       sqlNode  = null;
			//			if (1 == size) {
			//				sqlNode = contents.get(0);
			//			} else if (size > 1) {
			//				sqlNode = new MixedNode(contents);
			//			} else {
			//				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			//			}

			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			}

			SqlForNode forNode = new SqlForNode(sqlNode, index, start, end, open, close, separator);
			targetContents.add(forNode);
		}
	}

	//	protected class ForEachHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
	//
	//			String  tagName    = "foreach";
	//
	//			String  collection = getStringFromAttr(xNode, "collection", lang("xml.tag.attribute.empty", "collection", tagName, resource));
	//			String  index      = parseVariableKey(xNode, "index", tagName);
	//			String  open       = getStringFromAttr(xNode, "open");
	//			String  close      = getStringFromAttr(xNode, "close");
	//			String  separator  = getStringFromAttr(xNode, "separator");
	//			String  _start     = getStringFromAttr(xNode, "start");
	//			String  _end       = getStringFromAttr(xNode, "end");
	//			String  _pLen      = getStringFromAttr(xNode, "len");
	//			boolean ignoreIOOB = getBoolFromAttr(xNode, "ignoreIOOB", false);
	//
	//			collection = parseVariableKey(xNode, "collection", tagName);
	//
	//			Object start = null;
	//			Object end   = null;
	//			Object pLen  = null;
	//
	//			if (null != _start) {
	//				if (checkVar(_start)) {
	//					start = new GAParserWarper().parse(getRealVal(_start));
	//				} else {
	//					start = Integer.parseInt(_start);
	//				}
	//			}
	//			if (null != _end) {
	//				if (checkVar(_end)) {
	//					end = new GAParserWarper().parse(getRealVal(_end));
	//				} else {
	//					end = Integer.parseInt(_end);
	//				}
	//			}
	//			if (null != _pLen) {
	//				if (checkVar(_pLen)) {
	//					pLen = new GAParserWarper().parse(getRealVal(_pLen));
	//				} else {
	//					pLen = Integer.parseInt(_pLen);
	//				}
	//			}
	//			int                indexMode = ForEachNode.getAndCheckIndexMode(start, end, pLen);
	//
	//			//
	//			List<TangYuanNode> contents  = parseDynamicTags(xNode);
	//			int                size      = contents.size();
	//			TangYuanNode       sqlNode   = null;
	//			if (1 == size) {
	//				sqlNode = contents.get(0);
	//			} else if (size > 1) {
	//				sqlNode = new MixedNode(contents);
	//			}
	//			if (null == sqlNode && null == open && null == close && null == separator) {
	//				open = "(";
	//				close = ")";
	//				separator = ",";
	//			}
	//			if (null == sqlNode) {
	//				if (null == index) {
	//					index = "i";
	//				}
	//				sqlNode = new SqlTextNode("#{" + collection + "[" + index + "]}");
	//			}
	//
	//			// ForEachNode forEachNode = new SqlForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator);
	//			ForEachNode forEachNode = new SqlForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator, start, end, pLen, ignoreIOOB, indexMode);
	//			targetContents.add(forEachNode);
	//		}
	//	}

	////////////////////////////////////////// 
	//	private void buildRefNode(List<XmlNodeWrapper> contexts) {
	//		for (XmlNodeWrapper context : contexts) {
	//			String id     = trim(context.getStringAttribute("id")); // xml V
	//			String fullId = getFullId(id);
	//			// duplicate 'sql' tags: {}
	//			// isTrue(this.globalContext.getIntegralRefMap().containsKey(fullId), "duplicate 'sql' tags: {}", fullId);
	//			isTrue(this.globalContext.getIntegralRefMap().containsKey(fullId), "duplicate '{}' tags: {}", "sql",
	//					fullId);
	//			TangYuanNode sqlNode = parseNode(context, false);
	//			if (null != sqlNode) {
	//				this.globalContext.getIntegralRefMap().put(fullId, sqlNode);
	//				// info("add <sql> node: {}", fullId);
	//				info("add <{}> node: {}", "sql", fullId);
	//			}
	//		}
	//	}

	//	/** 扫描Segment */
	//	private void buildSegmentNode(List<XmlNodeWrapper> contexts) {
	//		String tagName = "segment";
	//		for (XmlNodeWrapper xNode : contexts) {
	//			String id     = StringUtils.trim(context.getStringAttribute("id")); // xml V
	//			String fullId = getFullId(id);
	//			isTrue(this.globalContext.getIntegralRefMap().containsKey(fullId), "duplicate '{}' tags: {}", "segment",
	//					fullId);
	//			TangYuanNode sqlNode = new SegmentNode(context);
	//			this.globalContext.getIntegralRefMap().put(fullId, sqlNode);
	//			info("add <{}> node: {}", "segment", fullId);
	//		}
	//	}

	//	private String checkDsKey(String dsKey, String tagName) {
	//		if (null == dsKey) {
	//			String defaultDsKey = SqlComponent.getInstance().getDataSourceManager().getDefaultDsKey();
	//			if (null == defaultDsKey) {
	//				throw new XmlParseException(
	//						lang("xml.tag.attribute.reference.invalid", dsKey, "dsKey", tagName, this.resource));
	//			}
	//			return defaultDsKey;
	//		}
	//		if (!SqlComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
	//			// if (!SqlComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
	//			// throw new XmlParseException("service[" + service + "] uses an invalid dsKey: " + dsKey);
	//			// }
	//			//			isTrue(!SqlComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey),
	//			//					"service[{}] uses an invalid dsKey: {}", service, dsKey);
	//			throw new XmlParseException(
	//					lang("xml.tag.attribute.reference.invalid", dsKey, "dsKey", tagName, this.resource));
	//		}
	//		return dsKey;
	//	}

	//	private List<AbstractServiceNode> buildSelectSetNodes(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			TangYuanNode sqlNode = parseNode(context, false);
	//			if (null != sqlNode) {
	//				String id = trimEmpty(context.getStringAttribute("id")); // xml v
	//				existingService(id);
	//
	//				String       resultType   = trimEmpty(context.getStringAttribute("resultType"));
	//				String       resultMap    = trimEmpty(context.getStringAttribute("resultMap"));
	//				SelectResult selectResult = parseSelectResult(resultType, resultMap, this.componentContext);
	//
	//				// String _fetchSize = StringUtils.trim(context.getStringAttribute("fetchSize")); // xml v
	//				// Integer fetchSize = null;
	//				// if (null != _fetchSize) {
	//				// fetchSize = Integer.valueOf(_fetchSize);
	//				// }
	//
	//				String       group        = trimEmpty(context.getStringAttribute("group"));
	//
	//				Integer      fetchSize    = attributeToInteger(context.getStringAttribute("fetchSize"));
	//
	//				String       dsKey        = trimEmpty(context.getStringAttribute("dsKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				// String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
	//				// XTransactionDefinition txDef = this.componentContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
	//				// if (null == txDef) {
	//				// throw new XmlParseException("service txRef is invalid: " + id);
	//				// }
	//
	//				String                 txRef = trimEmpty(context.getStringAttribute("txRef"));
	//				XTransactionDefinition txDef = this.componentContext.getTransactionMatcher()
	//						.getTransactionDefinition(txRef, id, "selectSet");
	//				isNull(txDef, "invalid attribute '{}' in service {}", "txRef", id);
	//
	//				// String _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//				// CacheUseVo cacheUse = null;
	//				// if (null != _cacheUse && _cacheUse.length() > 0) {
	//				// cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//				// }
	//
	//				CacheUseVo    cacheUse      = CacheUseVo.parseCacheUse(context.getStringAttribute("cacheUse"),
	//						getFullId(id));
	//
	//				SelectSetNode selectSetNode = new SelectSetNode(id, ns, getFullId(id), selectResult.resultType,
	//						selectResult.resultMap, dsKey, fetchSize, txDef, sqlNode, cacheUse);
	//				list.add(selectSetNode);
	//			}
	//		}
	//		return list;
	//	}

	//	private List<AbstractServiceNode> buildSelectOneNodes(List<XmlNodeWrapper> contexts) {
	//	List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//	for (XmlNodeWrapper context : contexts) {
	//		TangYuanNode sqlNode = parseNode(context, false);
	//		if (null != sqlNode) {
	//			String id = trimEmpty(context.getStringAttribute("id")); // xml v
	//			existingService(id);
	//
	//			// String _resultType = trimEmpty(context.getStringAttribute("resultType"));
	//			// String _resultMap = trimEmpty(context.getStringAttribute("resultMap"));
	//			// SelectResult selectResult = parseSelectResult(_resultType, _resultMap);
	//			String       resultType   = trimEmpty(context.getStringAttribute("resultType"));
	//			String       resultMap    = trimEmpty(context.getStringAttribute("resultMap"));
	//			SelectResult selectResult = parseSelectResult(resultType, resultMap, this.componentContext);
	//
	//			String       dsKey        = StringUtils.trim(context.getStringAttribute("dsKey"));
	//			dsKey = checkDsKey(dsKey, id);
	//
	//			// String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
	//			// XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
	//			// if (null == txDef) {
	//			// throw new XmlParseException("service txRef is invalid: " + id);
	//			// }
	//			String                 txRef = trimEmpty(context.getStringAttribute("txRef"));
	//			XTransactionDefinition txDef = this.componentContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
	//			// isNull(txDef, "invalid attribute '{}' in service {}", txRef, id);
	//			isNull(txDef, "invalid attribute '{}' in service {}", "txRef", id);
	//
	//			// String _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//			// CacheUseVo cacheUse = null;
	//			// if (null != _cacheUse && _cacheUse.length() > 0) {
	//			// cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//			// }
	//
	//			CacheUseVo    cacheUse      = CacheUseVo.parseCacheUse(context.getStringAttribute("cacheUse"), getFullId(id));
	//			String        group         = trimEmpty(context.getStringAttribute("group"));
	//
	//			SelectOneNode selectOneNode = new SelectOneNode(id, ns, getFullId(id), selectResult.resultType, selectResult.resultMap, dsKey, txDef,
	//					sqlNode, cacheUse);
	//			list.add(selectOneNode);
	//		}
	//	}
	//	return list;
	//}

	//	private List<AbstractServiceNode> buildSelectVarNodes(List<XmlNodeWrapper> contexts) {
	//		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
	//		for (XmlNodeWrapper context : contexts) {
	//			TangYuanNode sqlNode = parseNode(context, false);
	//			if (null != sqlNode) {
	//				String id = StringUtils.trim(context.getStringAttribute("id")); // xml v
	//				existingService(id);
	//
	//				String dsKey = StringUtils.trim(context.getStringAttribute("dsKey"));
	//				dsKey = checkDsKey(dsKey, id);
	//
	//				// String txRef = StringUtils.trim(context.getStringAttribute("txRef"));
	//				// XTransactionDefinition txDef = this.sqlContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
	//				// if (null == txDef) {
	//				// throw new XmlParseException("service txRef is invalid: " + id);
	//				// }
	//				String                 txRef = trimEmpty(context.getStringAttribute("txRef"));
	//				XTransactionDefinition txDef = this.componentContext.getTransactionMatcher().getTransactionDefinition(txRef, id, "selectSet");
	//				// isNull(txDef, "invalid attribute '{}' in service {}", txRef, id);
	//				isNull(txDef, "invalid attribute '{}' in service {}", "txRef", id);
	//
	//				// String _cacheUse = StringUtils.trim(context.getStringAttribute("cacheUse"));
	//				// CacheUseVo cacheUse = null;
	//				// if (null != _cacheUse && _cacheUse.length() > 0) {
	//				// cacheUse = parseCacheUse(_cacheUse, getFullId(id));
	//				// }
	//				CacheUseVo    cacheUse      = CacheUseVo.parseCacheUse(context.getStringAttribute("cacheUse"), getFullId(id));
	//
	//				String        group         = trimEmpty(context.getStringAttribute("group"));
	//
	//				SelectVarNode selectVarNode = new SelectVarNode(id, ns, getFullId(id), dsKey, txDef, sqlNode, cacheUse);
	//				list.add(selectVarNode);
	//			}
	//		}
	//		return list;
	//	}

	//	protected class ForEachHandler implements NodeHandler {
	//		public void handleNode(XmlNodeWrapper nodeToHandle, List<TangYuanNode> targetContents) {
	//
	//			String collection = trimEmpty(nodeToHandle.getStringAttribute("collection"));
	//			// if (!checkVar(collection)) {
	//			// throw new XmlParseException("<forEach> collection is not legal, Should be {xxx}");
	//			// }
	//
	//			isTrue(!checkVar(collection), "the attribute[{}] in the tag[{}] is not legal, should be {xxx}", "collection", "foreach");
	//			collection = getRealVal(collection);
	//
	//			String index = trimEmpty(nodeToHandle.getStringAttribute("index"));
	//			if (null != index) {
	//				// if (!checkVar(index)) {
	//				// throw new XmlParseException("<forEach> index is not legal, Should be {xxx}");
	//				// }
	//				isTrue(!checkVar(index), "the attribute[{}] in the tag[{}] is not legal, should be {xxx}", "index", "foreach");
	//				index = getRealVal(index);
	//			}
	//
	//			String             open      = trimEmpty(nodeToHandle.getStringAttribute("open"));
	//			String             close     = trimEmpty(nodeToHandle.getStringAttribute("close"));
	//			String             separator = trimEmpty(nodeToHandle.getStringAttribute("separator"));
	//
	//			List<TangYuanNode> contents  = parseDynamicTags(nodeToHandle);
	//			int                size      = contents.size();
	//			TangYuanNode       sqlNode   = null;
	//			if (1 == size) {
	//				sqlNode = contents.get(0);
	//			} else if (size > 1) {
	//				sqlNode = new MixedNode(contents);
	//			}
	//
	//			if (null == sqlNode && null == open && null == close && null == separator) {
	//				open = "(";
	//				close = ")";
	//				separator = ",";
	//			}
	//			if (null == sqlNode) {
	//				if (null == index) {
	//					index = "i";
	//				}
	//				sqlNode = new SqlTextNode("#{" + collection + "[" + index + "]}");
	//			}
	//
	//			// 增加Start && End选项
	//			String _start = StringUtils.trim(nodeToHandle.getStringAttribute("start"));
	//			String _end   = StringUtils.trim(nodeToHandle.getStringAttribute("end"));
	//			String _pLen  = StringUtils.trim(nodeToHandle.getStringAttribute("len"));
	//
	//			Object start  = null;
	//			Object end    = null;
	//			Object pLen   = null;
	//
	//			if (null != _start) {
	//				if (checkVar(_start)) {
	//					start = new GAParserWarper().parse(getRealVal(_start));
	//				} else {
	//					start = Integer.parseInt(_start);
	//				}
	//			}
	//			if (null != _end) {
	//				if (checkVar(_end)) {
	//					end = new GAParserWarper().parse(getRealVal(_end));
	//				} else {
	//					end = Integer.parseInt(_end);
	//				}
	//			}
	//			if (null != _pLen) {
	//				if (checkVar(_pLen)) {
	//					pLen = new GAParserWarper().parse(getRealVal(_pLen));
	//				} else {
	//					pLen = Integer.parseInt(_pLen);
	//				}
	//			}
	//
	//			// Boolean ignoreIOOB = getBooleanValueFromXmlNode(nodeToHandle, "ignoreIOOB", true, false, "in the <foreach> node, ");
	//			Boolean     ignoreIOOB  = attributeToBoolean(nodeToHandle.getStringAttribute("ignoreIOOB"), false);
	//			int         indexMode   = ForEachNode.getAndCheckIndexMode(start, end, pLen);
	//
	//			// ForEachNode forEachNode = new SqlForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator);
	//			ForEachNode forEachNode = new SqlForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator, start, end, pLen, ignoreIOOB, indexMode);
	//			targetContents.add(forEachNode);
	//		}
	//	}
}
