package org.xson.tangyuan.sql.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.xson.tangyuan.xml.DefaultMapperBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlSqlMapperBuilder extends DefaultMapperBuilder {

	private XmlSqlContext componentContext = null;

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

		this.componentContext = null;
	}

	private void configurationElement() throws Throwable {
		buildDataTypeMappingNode(getMostOneNode(this.root, "dataTypeMapping", lang("xml.tag.mostone", "dataTypeMapping")));
		buildMappingClassNode(this.root.evalNodes("mappingClass"));
		buildColumnHandlerClassNode(this.root.evalNodes("columnHandlerClass"));
		buildResultMapNode(this.root.evalNodes("resultMap"));
	}

	private void buildDataTypeMappingNode(XmlNodeWrapper xNode) {
		if (null == xNode) {
			return;
		}
		String tagName = "dataTypeMapping";
		Map<String, TypeHandler<?>> jdbcTypeMap = new HashMap<String, TypeHandler<?>>();
		List<XmlNodeWrapper> relations = xNode.evalNodes("relation");
		for (XmlNodeWrapper propertyNode : relations) {
			String jdbcType = getStringFromAttr(propertyNode, "jdbcType",
					lang("xml.tag.attribute.empty", "jdbcType", tagName + ".property", this.resource));
			String javaType = getStringFromAttr(propertyNode, "javaType",
					lang("xml.tag.attribute.empty", "javaType", tagName + ".property", this.resource));
			jdbcTypeMap.put(jdbcType.toUpperCase(), getJdbcTypeHandler(javaType));
			// log.info("add database type mapping: " + jdbcType.toLowerCase() + " TO " + javaType.toLowerCase());
			log.info(lang("sql.mapper.datatype.mapping", jdbcType.toLowerCase(), javaType.toLowerCase()));
		}

		TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
		typeHandlerRegistry.init(jdbcTypeMap);
		SqlComponent.getInstance().setTypeHandlerRegistry(typeHandlerRegistry);
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
		// throw new TangYuanException("Unsupported JdbcType: " + str);
		throw new XmlParseException(lang("unsupported.type.n", "jdbc", str));
	}

}
