package org.xson.tangyuan.sql.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.mapping.ColumnValueHandler;
import org.xson.tangyuan.mapping.ColumnVo;
import org.xson.tangyuan.mapping.MappingHandler;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.sql.SqlComponent;
import org.xson.tangyuan.type.BigDecimalTypeHandler;
import org.xson.tangyuan.type.BigIntegerTypeHandler;
import org.xson.tangyuan.type.BooleanTypeHandler;
import org.xson.tangyuan.type.ByteTypeHandler;
import org.xson.tangyuan.type.DoubleTypeHandler;
import org.xson.tangyuan.type.FloatTypeHandler;
import org.xson.tangyuan.type.IntegerTypeHandler;
import org.xson.tangyuan.type.LongTypeHandler;
import org.xson.tangyuan.type.ShortTypeHandler;
import org.xson.tangyuan.type.TypeHandler;
import org.xson.tangyuan.type.TypeHandlerRegistry;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlSqlMapperBuilder2 extends DefaultXmlComponentBuilder {

	private XmlSqlContext                   componentContext    = null;
	private Map<String, MappingHandler>     mappingClassMap     = new HashMap<String, MappingHandler>();
	private Map<String, ColumnValueHandler> columnValueClassMap = new HashMap<String, ColumnValueHandler>();
	private Map<String, MappingVo>          mappingVoMap        = new HashMap<String, MappingVo>();

	@Override
	protected void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing.type", "mapper", resource));
		this.componentContext = (XmlSqlContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "mapper", true);
		this.configurationElement();
		this.componentContext.setMappingVoMap(mappingVoMap);
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();
		this.mappingClassMap = null;
		this.columnValueClassMap = null;
		this.mappingVoMap = null;
	}

	private void configurationElement() throws Throwable {
		buildDataTypeMappingNode(getMostOneNode(this.root, "dataTypeMapping", lang("xml.tag.mostone", "dataTypeMapping")));
		buildMappingClassNode(this.root.evalNodes("mappingClass"));
		buildColumnValueClassNode(this.root.evalNodes("columnValueClass"));
		buildResultMapNode(this.root.evalNodes("resultMap"));
	}

	private void buildDataTypeMappingNode(XmlNodeWrapper xNode) {
		if (null == xNode) {
			return;
		}
		String                      tagName     = "dataTypeMapping";
		Map<String, TypeHandler<?>> jdbcTypeMap = new HashMap<String, TypeHandler<?>>();
		List<XmlNodeWrapper>        relations   = xNode.evalNodes("relation");
		for (XmlNodeWrapper propertyNode : relations) {
			String jdbcType = getStringFromAttr(propertyNode, "jdbcType", lang("xml.tag.attribute.empty", "jdbcType", tagName + ".property", this.resource));
			String javaType = getStringFromAttr(propertyNode, "javaType", lang("xml.tag.attribute.empty", "javaType", tagName + ".property", this.resource));
			jdbcTypeMap.put(jdbcType.toUpperCase(), getJdbcTypeHandler(javaType));
			//log.info("add database type mapping: " + jdbcType.toLowerCase() + " TO " + javaType.toLowerCase());
			log.info(lang("sql.mapper.datatype.mapping", jdbcType.toLowerCase(), javaType.toLowerCase()));
		}

		TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
		typeHandlerRegistry.init(jdbcTypeMap);
		SqlComponent.getInstance().setTypeHandlerRegistry(typeHandlerRegistry);
	}

	private void buildMappingClassNode(List<XmlNodeWrapper> contexts) throws Throwable {
		// <mappingClass id="default_bean_mapping" class="org.xson.tangyuan.mapping.DefaultMappingHandler"/>
		int    size    = contexts.size();
		String tagName = "mappingClass";
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode     = contexts.get(i);
			String         id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String         className = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));
			MappingHandler handler   = getInstanceForName(className, MappingHandler.class, lang("xml.class.impl.interface", className, MappingHandler.class.getName()));
			if (mappingClassMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			mappingClassMap.put(id, handler);
			//			log.info("add mapping handler: " + className);
			log.info(lang("add.tag.class"), tagName, className);
		}
	}

	private void buildColumnValueClassNode(List<XmlNodeWrapper> contexts) throws Throwable {
		int    size    = contexts.size();
		String tagName = "columnValueClass";
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper     xNode     = contexts.get(i);
			String             id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String             className = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));
			ColumnValueHandler handler   = getInstanceForName(className, ColumnValueHandler.class, lang("xml.class.impl.interface", className, ColumnValueHandler.class.getName()));
			if (columnValueClassMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			columnValueClassMap.put(id, handler);
			log.info(lang("add.tag.class"), tagName, className);
		}
	}

	private void buildResultMapNode(List<XmlNodeWrapper> contexts) {
		int    size    = contexts.size();
		String tagName = "resultMap";
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);

			String         id    = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String         type  = getStringFromAttr(xNode, "type");
			String         rule  = getStringFromAttr(xNode, "rule");

			if (mappingVoMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}

			Class<?> beanClass = null;
			if (null == type) {
				// beanClass = Map.class;// 这里需要看resultType
			} else if (!"*".equals(type)) {
				beanClass = ClassUtils.forName(type);
			}
			MappingHandler        handler    = mappingClassMap.get(rule);

			Map<String, ColumnVo> columnMap  = new HashMap<String, ColumnVo>();
			List<XmlNodeWrapper>  properties = xNode.evalNodes("result");
			for (XmlNodeWrapper propertyNode : properties) {
				String             column   = getStringFromAttr(propertyNode, "column", lang("xml.tag.attribute.empty", "column", tagName + ".property", this.resource));
				String             property = getStringFromAttr(propertyNode, "property");
				String             cvhId    = getStringFromAttr(propertyNode, "valueClass");                                                                             // TODO
				ColumnValueHandler cvh      = this.columnValueClassMap.get(cvhId);
				ColumnVo           columnVo = new ColumnVo(property, cvh);
				columnMap.put(column, columnVo);
			}

			if (null == handler && 0 == columnMap.size()) {
				throw new XmlParseException(lang("xml.tag.attribute.empty", "rule && result", tagName, this.resource));
			}

			MappingVo mVo = new MappingVo(id, type, beanClass, handler, null, columnMap);
			mappingVoMap.put(id, mVo);
			log.info(lang("add.tag.class"), tagName, id);
		}
	}

	//	private void buildResultMapNode(List<XmlNodeWrapper> contexts) {
	//		int    size    = contexts.size();
	//		String tagName = "resultMap";
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode = contexts.get(i);
	//
	//			String         id    = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
	//			String         type  = getStringFromAttr(xNode, "type");
	//			String         rule  = getStringFromAttr(xNode, "rule");
	//
	//			if (mappingVoMap.containsKey(id)) {
	//				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
	//			}
	//
	//			Class<?> beanClass = null;
	//			if (null == type) {
	//				// beanClass = Map.class;// 这里需要看resultType
	//			} else if (!"*".equals(type)) {
	//				beanClass = ClassUtils.forName(type);
	//			}
	//			MappingHandler       handler    = mappingClassMap.get(rule);
	//
	//			Map<String, String>  columnMap  = new HashMap<String, String>();
	//			List<XmlNodeWrapper> properties = xNode.evalNodes("result");
	//			for (XmlNodeWrapper propertyNode : properties) {
	//				String column   = getStringFromAttr(propertyNode, "column", lang("xml.tag.attribute.empty", "column", tagName + ".property", this.resource));
	//				String property = getStringFromAttr(propertyNode, "property", lang("xml.tag.attribute.empty", "property", tagName + ".property", this.resource));
	//				columnMap.put(column, property);
	//			}
	//
	//			if (null == handler && 0 == columnMap.size()) {
	//				throw new XmlParseException(lang("xml.tag.attribute.empty", "rule && result", tagName, this.resource));
	//			}
	//
	//			MappingVo mVo = new MappingVo(id, type, beanClass, handler, columnMap);
	//			mappingVoMap.put(id, mVo);
	//			log.info(lang("add.tag.class"), tagName, id);
	//		}
	//	}

	private TypeHandler<?> getJdbcTypeHandler(String str) {
		if ("byte".equalsIgnoreCase(str)) {
			return ByteTypeHandler.instance;
		} else if ("boolean".equalsIgnoreCase(str)) {
			return BooleanTypeHandler.instance;
		} else if ("short".equalsIgnoreCase(str)) {
			return ShortTypeHandler.instance;
		} else if ("int".equalsIgnoreCase(str)) {
			return IntegerTypeHandler.instance;
		} else if ("long".equalsIgnoreCase(str)) {
			return LongTypeHandler.instance;
		} else if ("float".equalsIgnoreCase(str)) {
			return FloatTypeHandler.instance;
		} else if ("double".equalsIgnoreCase(str)) {
			return DoubleTypeHandler.instance;
		} else if ("bigInteger".equalsIgnoreCase(str)) {
			return BigIntegerTypeHandler.instance;
		} else if ("bigDecimal".equalsIgnoreCase(str)) {
			return BigDecimalTypeHandler.instance;
		}
		//		throw new TangYuanException("Unsupported JdbcType: " + str);
		throw new XmlParseException(lang("unsupported.type.n", "jdbc", str));
	}

	////////////////////////////////////////////////////////////////////// 

	//	private void buildResultMapNode(List<XmlNodeWrapper> contexts) {
	//		int size = contexts.size();
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode = contexts.get(i);
	//			String         id    = StringUtils.trim(xNode.getStringAttribute("id"));// xmlV
	//			if (mappingVoMap.containsKey(id)) {
	//				throw new XmlParseException("Duplicate mapping: " + id);
	//			}
	//			String   type      = StringUtils.trim(xNode.getStringAttribute("type"));
	//			Class<?> beanClass = null;
	//
	//			if (null == type) {
	//				// beanClass = Map.class;// 这里需要看resultType
	//			} else if (!"*".equals(type)) {
	//				beanClass = ClassUtils.forName(type);
	//			}
	//
	//			String               rule       = StringUtils.trim(xNode.getStringAttribute("rule"));
	//			MappingHandler       handler    = mappingClassMap.get(rule);
	//
	//			Map<String, String>  columnMap  = new HashMap<String, String>();
	//
	//			List<XmlNodeWrapper> properties = xNode.evalNodes("result");
	//			for (XmlNodeWrapper propertyNode : properties) {
	//				columnMap.put(StringUtils.trim(propertyNode.getStringAttribute("column")),
	//						StringUtils.trim(propertyNode.getStringAttribute("property")));
	//			}
	//
	//			if (null == handler && 0 == columnMap.size()) {
	//				throw new XmlParseException("<resultMap> rule and result can not be empty: " + id);
	//			}
	//
	//			MappingVo mVo = new MappingVo(id, type, beanClass, handler, columnMap);
	//			mappingVoMap.put(id, mVo);
	//			log.info("add resultMap: " + id);
	//		}
	//	}
	//	private void buildDataTypeMappingNode(List<XmlNodeWrapper> contexts) {
	//		int size = contexts.size();
	//		if (size > 1) {
	//			throw new XmlParseException("dataTypeMapping can have at most one entry");
	//		}
	//		Map<String, TypeHandler<?>> jdbcTypeMap = new HashMap<String, TypeHandler<?>>();
	//		if (size == 1) {
	//			List<XmlNodeWrapper> relations = contexts.get(0).evalNodes("relation");
	//			for (XmlNodeWrapper node : relations) {
	//				String jdbcType = StringUtils.trim(node.getStringAttribute("jdbcType")); // XMLV
	//				String javaType = StringUtils.trim(node.getStringAttribute("javaType")); // XMLV
	//				if (null == jdbcType || null == javaType) {
	//					throw new XmlParseException("jdbcType or javaType is null.");
	//				}
	//				jdbcTypeMap.put(jdbcType.toUpperCase(), getJdbcTypeHandler(javaType));
	//				log.info("add database type mapping: " + jdbcType.toLowerCase() + " TO " + javaType.toLowerCase());
	//			}
	//		}
	//
	//		TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
	//		typeHandlerRegistry.init(jdbcTypeMap);
	//		SqlComponent.getInstance().setTypeHandlerRegistry(typeHandlerRegistry);
	//	}

	//	private void buildMappingClassNode(List<XmlNodeWrapper> contexts) throws Exception {
	//		int size = contexts.size();
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode = contexts.get(i);
	//			// <mappingClass id="default_bean_mapping"
	//			// class="org.xson.tangyuan.mapping.DefaultMappingHandler"/>
	//			String         id    = StringUtils.trim(xNode.getStringAttribute("id"));
	//			if (null != mappingClassMap.get(id)) {
	//				throw new XmlParseException("Duplicate mappingClass: " + id);
	//			}
	//			String   className    = StringUtils.trim(xNode.getStringAttribute("class"));
	//			Class<?> handlerClass = ClassUtils.forName(className);
	//			if (!MappingHandler.class.isAssignableFrom(handlerClass)) {
	//				throw new XmlParseException("mapping class not implement the MappingHandler interface: " + className);
	//			}
	//			MappingHandler handler = (MappingHandler) handlerClass.newInstance();
	//			mappingClassMap.put(id, handler);
	//			log.info("add mapping handler: " + className);
	//		}
	//	}

}
