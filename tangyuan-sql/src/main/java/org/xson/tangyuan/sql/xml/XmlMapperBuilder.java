package org.xson.tangyuan.sql.xml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
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
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlMapperBuilder {

	private Log							log				= LogFactory.getLog(getClass());
	private XPathParser					xPathParser		= null;
	private Map<String, MappingHandler>	mappingClassMap	= new HashMap<String, MappingHandler>();
	private Map<String, MappingVo>		mappingVoMap	= new HashMap<String, MappingVo>();

	public XmlMapperBuilder(InputStream inputStream) {
		this.xPathParser = new XPathParser(inputStream);
	}

	public void parse(XmlSqlContext sqlContext) {
		configurationElement(xPathParser.evalNode("/mapper"));
		sqlContext.setMappingVoMap(mappingVoMap);
		mappingClassMap = null;
		mappingVoMap = null;
	}

	private void configurationElement(XmlNodeWrapper context) {
		try {
			buildDataTypeMappingNodes(context.evalNodes("dataTypeMapping"));
			buildMappingClassNodes(context.evalNodes("mappingClass"));
			buildResultMapNodes(context.evalNodes("resultMap"));
		} catch (Exception e) {
			throw new XmlParseException(e);
		}
	}

	private void buildDataTypeMappingNodes(List<XmlNodeWrapper> contexts) {
		int size = contexts.size();
		if (size > 1) {
			throw new XmlParseException("dataTypeMapping can have at most one entry");
		}
		Map<String, TypeHandler<?>> jdbcTypeMap = new HashMap<String, TypeHandler<?>>();
		if (size == 1) {
			List<XmlNodeWrapper> relations = contexts.get(0).evalNodes("relation");
			for (XmlNodeWrapper node : relations) {
				String jdbcType = StringUtils.trim(node.getStringAttribute("jdbcType")); // XMLV
				String javaType = StringUtils.trim(node.getStringAttribute("javaType")); // XMLV
				if (null == jdbcType || null == javaType) {
					throw new XmlParseException("jdbcType or javaType is null.");
				}
				jdbcTypeMap.put(jdbcType.toUpperCase(), getJdbcTypeHandler(javaType));
				log.info("add database type mapping: " + jdbcType.toLowerCase() + " TO " + javaType.toLowerCase());
			}
		}

		TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
		typeHandlerRegistry.init(jdbcTypeMap);
		SqlComponent.getInstance().setTypeHandlerRegistry(typeHandlerRegistry);
	}

	private void buildMappingClassNodes(List<XmlNodeWrapper> contexts) throws Exception {
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			// <mappingClass id="default_bean_mapping"
			// class="org.xson.tangyuan.mapping.DefaultMappingHandler"/>
			String id = StringUtils.trim(xNode.getStringAttribute("id"));
			if (null != mappingClassMap.get(id)) {
				throw new XmlParseException("Duplicate mappingClass: " + id);
			}
			String className = StringUtils.trim(xNode.getStringAttribute("class"));
			Class<?> handlerClass = ClassUtils.forName(className);
			if (!MappingHandler.class.isAssignableFrom(handlerClass)) {
				throw new XmlParseException("mapping class not implement the MappingHandler interface: " + className);
			}
			MappingHandler handler = (MappingHandler) handlerClass.newInstance();
			mappingClassMap.put(id, handler);
			log.info("add mapping handler: " + className);
		}
	}

	private void buildResultMapNodes(List<XmlNodeWrapper> contexts) {
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));// xmlV
			if (mappingVoMap.containsKey(id)) {
				throw new XmlParseException("Duplicate mapping: " + id);
			}
			String type = StringUtils.trim(xNode.getStringAttribute("type"));
			Class<?> beanClass = null;

			if (null == type) {
				// beanClass = Map.class;// 这里需要看resultType
			} else if (!"*".equals(type)) {
				beanClass = ClassUtils.forName(type);
			}

			String rule = StringUtils.trim(xNode.getStringAttribute("rule"));
			MappingHandler handler = mappingClassMap.get(rule);

			Map<String, String> columnMap = new HashMap<String, String>();

			List<XmlNodeWrapper> properties = xNode.evalNodes("result");
			for (XmlNodeWrapper propertyNode : properties) {
				columnMap.put(StringUtils.trim(propertyNode.getStringAttribute("column")),
						StringUtils.trim(propertyNode.getStringAttribute("property")));
			}

			if (null == handler && 0 == columnMap.size()) {
				throw new XmlParseException("<resultMap> rule and result can not be empty: " + id);
			}

			MappingVo mVo = new MappingVo(id, type, beanClass, handler, columnMap);
			mappingVoMap.put(id, mVo);
			log.info("add resultMap: " + id);
		}
	}

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
		throw new TangYuanException("Unsupported JdbcType: " + str);
	}

}
