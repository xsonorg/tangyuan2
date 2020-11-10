package org.xson.tangyuan.mongo.xml;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.apply.CacheCleanVo;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.mongo.MongoComponent;
import org.xson.tangyuan.mongo.xml.node.InternalMongoCommandNode;
import org.xson.tangyuan.mongo.xml.node.InternalMongoDeleteNode;
import org.xson.tangyuan.mongo.xml.node.InternalMongoInsertNode;
import org.xson.tangyuan.mongo.xml.node.InternalMongoSelectOneNode;
import org.xson.tangyuan.mongo.xml.node.InternalMongoSelectSetNode;
import org.xson.tangyuan.mongo.xml.node.InternalMongoSelectVarNode;
import org.xson.tangyuan.mongo.xml.node.InternalMongoUpdateNode;
import org.xson.tangyuan.mongo.xml.node.MongoCommandNode;
import org.xson.tangyuan.mongo.xml.node.MongoDeleteNode;
import org.xson.tangyuan.mongo.xml.node.MongoForEachNode;
import org.xson.tangyuan.mongo.xml.node.MongoForNode;
import org.xson.tangyuan.mongo.xml.node.MongoInsertNode;
import org.xson.tangyuan.mongo.xml.node.MongoSelectOneNode;
import org.xson.tangyuan.mongo.xml.node.MongoSelectSetNode;
import org.xson.tangyuan.mongo.xml.node.MongoSelectVarNode;
import org.xson.tangyuan.mongo.xml.node.MongoServiceNode;
import org.xson.tangyuan.mongo.xml.node.MongoTextNode;
import org.xson.tangyuan.mongo.xml.node.MongoUpdateNode;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.xml.DefaultXmlPluginBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.ForEachNode;
import org.xson.tangyuan.xml.node.ForNode;
import org.xson.tangyuan.xml.node.SegmentNode;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class XmlMongoPluginBuilder extends DefaultXmlPluginBuilder {

	private XmlMongoContext	componentContext	= null;
	private String			dsKeyWithSqlService	= null;
	private String			idWithSqlService	= null;
	private Class<?>		serviceResultType	= null;

	@Override
	public void setContext(String resource, XmlContext xmlContext) throws Throwable {
		this.componentContext = (XmlMongoContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "mongoservices", false);
		if (this.ns.length() > 0) {
			checkNs(this.ns);
		}
		initNodeHandler();
	}

	@Override
	public void clean() {
		super.clean();

		this.dsKeyWithSqlService = null;
		this.idWithSqlService = null;
		this.serviceResultType = null;
		this.componentContext = null;
	}

	public void parseRef() {
		log.info(lang("xml.start.parsing.type", "plugin[ref]", this.resource));
		buildSqlNode(this.root.evalNodes("sql"));
		// 增加段定义和引用
		buildSegmentNode(this.root.evalNodes("segment"));
	}

	public void parseService() {
		log.info(lang("xml.start.parsing.type", "plugin[service]", this.resource));
		configurationElement();
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
		nodeHandlers.put("call", new CallHandler());
		nodeHandlers.put("command", new CommandHandler());
	}

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

	private String checkReferencedDsKey(String dsKey, String tagName, String id) {
		if (null == dsKey) {
			String defaultDsKey = this.componentContext.getDefaultDataSource();
			if (null == defaultDsKey) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", "null", id, "dsKey", tagName, this.resource));
			}
			return defaultDsKey;
		}
		if (!MongoComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
			throw new XmlParseException(lang("xml.tag.attribute.reference.id.invalid", dsKey, id, "dsKey", tagName, this.resource));
		}
		return dsKey;
	}

	private void checkInnerDsKey(String dsKey, String tagName) {
		if (!MongoComponent.getInstance().getDataSourceManager().isValidDsKey(dsKey)) {
			throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", dsKey, "dsKey", tagName, this.resource));
		}
	}

	/** 扫描REF */
	private void buildSqlNode(List<XmlNodeWrapper> contexts) {
		String tagName = "sql";
		for (XmlNodeWrapper xNode : contexts) {
			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
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
			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
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
		List<AbstractServiceNode> selectSetList = buildSelectSetNode(this.root.evalNodes("selectSet"));
		List<AbstractServiceNode> selectOneList = buildSelectOneNode(this.root.evalNodes("selectOne"));
		List<AbstractServiceNode> selectVarList = buildSelectVarNode(this.root.evalNodes("selectVar"));
		List<AbstractServiceNode> insertList = buildInsertNode(this.root.evalNodes("insert"));
		List<AbstractServiceNode> updateList = buildUpdateNode(this.root.evalNodes("update"));
		List<AbstractServiceNode> deleteList = buildDeleteNode(this.root.evalNodes("delete"));
		// List<AbstractServiceNode> commandList = buildCommandNodes(context.evalNodes("mongo-command"));
		List<AbstractServiceNode> commandList = buildCommandNode(this.root.evalNodes("command"));
		List<AbstractServiceNode> mongoServiceList = buildMongoServiceNode(this.root.evalNodes("mongo-service"));

		registerService(selectSetList, "selectSet");
		registerService(selectOneList, "selectOne");
		registerService(selectVarList, "selectVar");
		registerService(insertList, "insert");
		registerService(updateList, "update");
		registerService(deleteList, "delete");
		// registerService(commandList, "mongo-command");
		registerService(commandList, "command");
		registerService(mongoServiceList, "mongo-service");
	}

	@Override
	protected TangYuanNode getTextNode(String data) {
		return new MongoTextNode(data);
	}

	private List<AbstractServiceNode> buildSelectSetNode(List<XmlNodeWrapper> contexts) {
		String tagName = "selectSet";
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String resultType = getStringFromAttr(xNode, "resultType");
			String resultMap = getStringFromAttr(xNode, "resultMap");
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");

			// 新增,每个服务节点都需要包含的
			String desc = getStringFromAttr(xNode, "desc");
			String[] groups = getStringArrayFromAttr(xNode, "group");

			Integer fetchSize = null;
			// Integer fetchSize = getIntegerFromAttr(xNode, "fetchSize");
			// String txRef = getStringFromAttr(xNode, "txRef");

			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			SelectResult selectResult = parseSelectResult(resultType, resultMap, tagName, this.componentContext);
			CacheUseVo cacheUse = parseCacheUse(_cacheUse, id);

			MongoSelectSetNode selectSetNode = new MongoSelectSetNode(id, this.ns, getFullId(id), selectResult.resultType, selectResult.resultMap,
					dsKey, fetchSize, sqlNode, cacheUse, desc, groups);

			list.add(selectSetNode);

		}
		return list;
	}

	private List<AbstractServiceNode> buildSelectOneNode(List<XmlNodeWrapper> contexts) {
		String tagName = "selectOne";
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String resultType = getStringFromAttr(xNode, "resultType");
			String resultMap = getStringFromAttr(xNode, "resultMap");
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			// 新增,每个服务节点都需要包含的
			String desc = getStringFromAttr(xNode, "desc");
			String[] groups = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			SelectResult selectResult = parseSelectResult(resultType, resultMap, tagName, this.componentContext);
			CacheUseVo cacheUse = parseCacheUse(_cacheUse, id);

			MongoSelectOneNode selectOneNode = new MongoSelectOneNode(id, this.ns, getFullId(id), selectResult.resultType, selectResult.resultMap,
					dsKey, sqlNode, cacheUse, desc, groups);

			list.add(selectOneNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildSelectVarNode(List<XmlNodeWrapper> contexts) {
		String tagName = "selectVar";
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			// 新增,每个服务节点都需要包含的
			String desc = getStringFromAttr(xNode, "desc");
			String[] groups = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			CacheUseVo cacheUse = parseCacheUse(_cacheUse, id);

			MongoSelectVarNode selectVarNode = new MongoSelectVarNode(id, this.ns, getFullId(id), dsKey, sqlNode, cacheUse, desc, groups);
			list.add(selectVarNode);
		}

		return list;
	}

	private List<AbstractServiceNode> buildInsertNode(List<XmlNodeWrapper> contexts) {
		String tagName = "insert";
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");

			// String rowCount = parseVariableKey(xNode, "rowCount", tagName);
			// String incrementKey = parseVariableKey(xNode, "incrementKey", tagName);

			// 新增,每个服务节点都需要包含的
			String desc = getStringFromAttr(xNode, "desc");
			String[] groups = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, id);

			MongoInsertNode insertNode = new MongoInsertNode(id, this.ns, getFullId(id), null, null, dsKey, sqlNode, cacheClean, desc, groups);
			list.add(insertNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildUpdateNode(List<XmlNodeWrapper> contexts) {
		String tagName = "update";
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String desc = getStringFromAttr(xNode, "desc");
			String[] groups = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, id);

			MongoUpdateNode updateNode = new MongoUpdateNode(id, this.ns, getFullId(id), dsKey, sqlNode, cacheClean, desc, groups);
			list.add(updateNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildDeleteNode(List<XmlNodeWrapper> contexts) {
		String tagName = "delete";
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String desc = getStringFromAttr(xNode, "desc");
			String[] groups = getStringArrayFromAttr(xNode, "group");

			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, id);

			MongoDeleteNode deleteNode = new MongoDeleteNode(id, this.ns, getFullId(id), dsKey, sqlNode, cacheClean, desc, groups);
			list.add(deleteNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildCommandNode(List<XmlNodeWrapper> contexts) {
		String tagName = "command";
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String resultType = getStringFromAttr(xNode, "resultType");
			String resultMap = getStringFromAttr(xNode, "resultMap");
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String desc = getStringFromAttr(xNode, "desc");
			String[] groups = getStringArrayFromAttr(xNode, "group");

			// String rowCount = parseVariableKey(xNode, "rowCount", tagName);
			// String incrementKey = parseVariableKey(xNode, "incrementKey", tagName);
			// Integer fetchSize = getIntegerFromAttr(xNode, "fetchSize");

			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			SelectResult selectResult = parseSelectResult(resultType, resultMap, tagName, this.componentContext);

			CacheUseVo cacheUse = parseCacheUse(_cacheUse, id);
			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, id);

			MongoCommandNode commandNode = new MongoCommandNode(id, this.ns, getFullId(id), dsKey, sqlNode, cacheUse, cacheClean,
					selectResult.resultType, selectResult.resultMap, null, null, desc, groups);

			list.add(commandNode);
		}
		return list;
	}

	private List<AbstractServiceNode> buildMongoServiceNode(List<XmlNodeWrapper> contexts) {
		String tagName = "mongo-service";
		List<AbstractServiceNode> list = new ArrayList<AbstractServiceNode>();
		for (XmlNodeWrapper xNode : contexts) {

			String id = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");
			// 新增,每个服务节点都需要包含的
			String desc = getStringFromAttr(xNode, "desc");
			String[] groups = getStringArrayFromAttr(xNode, "group");

			checkServiceRepeated(id, tagName);
			dsKey = checkReferencedDsKey(dsKey, tagName, id);

			CacheUseVo cacheUse = parseCacheUse(_cacheUse, id);
			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, id);

			this.idWithSqlService = id;
			this.dsKeyWithSqlService = dsKey;
			this.serviceResultType = TangYuanContainer.getInstance().getDefaultResultType();

			TangYuanNode sqlNode = parseNode(xNode, false);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", id, tagName, this.resource));
			}

			MongoServiceNode serviceNode = new MongoServiceNode(id, this.ns, getFullId(id), dsKey, sqlNode, cacheUse, cacheClean,
					this.serviceResultType, desc, groups);
			list.add(serviceNode);
		}
		return list;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private class SelectSetHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "selectSet";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				// throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String resultMap = getStringFromAttr(xNode, "resultMap");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String resultKey = parseVariableKey(xNode, "resultKey", tagName);

			// Integer fetchSize = getIntegerFromAttr(xNode, "fetchSize");
			Integer fetchSize = null;

			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			SelectResult selectResult = parseSelectResult(null, resultMap, tagName, componentContext);
			CacheUseVo cacheUse = parseCacheUse(_cacheUse, idWithSqlService);
			InternalMongoSelectSetNode selectSetNode = new InternalMongoSelectSetNode(dsKey, resultKey, sqlNode, serviceResultType,
					selectResult.resultMap, fetchSize, cacheUse);
			targetContents.add(selectSetNode);
		}
	}

	private class SelectOneHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "selectOne";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String resultMap = getStringFromAttr(xNode, "resultMap");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String resultKey = parseVariableKey(xNode, "resultKey", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			SelectResult selectResult = parseSelectResult(null, resultMap, tagName, componentContext);
			CacheUseVo cacheUse = parseCacheUse(_cacheUse, idWithSqlService);

			InternalMongoSelectOneNode selectOneNode = new InternalMongoSelectOneNode(dsKey, resultKey, sqlNode, serviceResultType,
					selectResult.resultMap, cacheUse);
			targetContents.add(selectOneNode);
		}
	}

	private class SelectVarHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "selectVar";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String resultKey = parseVariableKey(xNode, "resultKey", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			CacheUseVo cacheUse = parseCacheUse(_cacheUse, idWithSqlService);
			InternalMongoSelectVarNode selectVarNode = new InternalMongoSelectVarNode(dsKey, resultKey, sqlNode, cacheUse);
			targetContents.add(selectVarNode);
		}
	}

	private class DeleteHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "delete";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");
			String resultKey = parseVariableKey(xNode, "rowCount", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, idWithSqlService);
			InternalMongoDeleteNode deleteNode = new InternalMongoDeleteNode(dsKey, resultKey, sqlNode, cacheClean);
			targetContents.add(deleteNode);
		}
	}

	private class UpdateHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "update";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");
			String resultKey = parseVariableKey(xNode, "rowCount", tagName);
			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, idWithSqlService);
			InternalMongoUpdateNode updateNode = new InternalMongoUpdateNode(dsKey, resultKey, sqlNode, cacheClean);
			targetContents.add(updateNode);
		}
	}

	private class InsertHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "insert";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}
			String dsKey = getStringFromAttr(xNode, "dsKey");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");

			// String resultKey = parseVariableKey(xNode, "resultKey", tagName);
			// String rowCount = parseVariableKey(xNode, "rowCount", tagName);
			String incrementKey = parseVariableKey(xNode, "incrementKey", tagName);

			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, idWithSqlService);
			InternalMongoInsertNode insertNode = new InternalMongoInsertNode(dsKey, null, incrementKey, sqlNode, cacheClean);
			targetContents.add(insertNode);
		}
	}

	private class CommandHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {
			String tagName = "command";
			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content-id.empty", idWithSqlService, tagName, resource));
			}

			String dsKey = getStringFromAttr(xNode, "dsKey");
			String resultMap = getStringFromAttr(xNode, "resultMap");
			String _cacheUse = getStringFromAttr(xNode, "cacheUse");
			String _cacheClean = getStringFromAttr(xNode, "cacheClean");

			// insert, update, delete
			String resultKey = parseVariableKey(xNode, "resultKey", tagName);
			// insert 专用
			// String rowCount = parseVariableKey(xNode, "rowCount", tagName);
			// String incrementKey = parseVariableKey(xNode, "incrementKey", tagName);

			if (null == dsKey) {
				dsKey = dsKeyWithSqlService;
			} else {
				checkInnerDsKey(dsKey, tagName);
			}
			SelectResult selectResult = parseSelectResult(null, resultMap, tagName, componentContext);
			CacheUseVo cacheUse = parseCacheUse(_cacheUse, idWithSqlService);
			CacheCleanVo cacheClean = parseCacheClean(_cacheClean, idWithSqlService);
			InternalMongoCommandNode commandNode = new InternalMongoCommandNode(dsKey, resultKey, null, null, sqlNode, serviceResultType,
					selectResult.resultMap, cacheUse, cacheClean);
			targetContents.add(commandNode);
		}
	}

	protected class ForEachHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {

			String tagName = "foreach";

			String collection = getStringFromAttr(xNode, "collection", lang("xml.tag.attribute.empty", "collection", tagName, resource));
			String index = parseVariableKey(xNode, "index", tagName);
			String open = getStringFromAttr(xNode, "open");
			String close = getStringFromAttr(xNode, "close");
			String separator = getStringFromAttr(xNode, "separator");

			collection = parseVariableKey(xNode, "collection", tagName);

			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			}

			ForEachNode forEachNode = new MongoForEachNode(sqlNode, new NormalParser().parse(collection), index, open, close, separator);
			targetContents.add(forEachNode);
		}
	}

	protected class ForHandler implements NodeHandler {
		public void handleNode(XmlNodeWrapper xNode, List<TangYuanNode> targetContents) {

			String tagName = "for";

			String index = getStringFromAttr(xNode, "index", lang("xml.tag.attribute.empty", "index", tagName, resource));
			String open = getStringFromAttr(xNode, "open");
			String close = getStringFromAttr(xNode, "close");
			String separator = getStringFromAttr(xNode, "separator");
			String _start = getStringFromAttr(xNode, "start");
			String _end = getStringFromAttr(xNode, "end", lang("xml.tag.attribute.empty", "end", tagName, resource));

			index = parseVariableKey(xNode, "index", tagName);

			Object start = 0;
			Object end = null;

			if (null != _start) {
				if (checkVar(_start)) {
					start = parseVariableUseGA(_start);
				} else {
					start = Integer.parseInt(_start);
				}
			}

			if (checkVar(_end)) {
				end = parseVariableUseGA(_end);
			} else {
				end = Integer.parseInt(_end);
			}

			TangYuanNode sqlNode = parseNode(xNode, true);
			if (null == sqlNode) {
				throw new XmlParseException(lang("xml.tag.content.empty", tagName, resource));
			}

			ForNode forNode = new MongoForNode(sqlNode, index, start, end, open, close, separator);
			targetContents.add(forNode);
		}
	}

}
