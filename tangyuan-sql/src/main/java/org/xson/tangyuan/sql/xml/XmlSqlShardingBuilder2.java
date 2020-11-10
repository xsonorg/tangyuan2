package org.xson.tangyuan.sql.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.sharding.HashShardingHandler;
import org.xson.tangyuan.sharding.ModShardingHandler;
import org.xson.tangyuan.sharding.RandomShardingHandler;
import org.xson.tangyuan.sharding.RangeShardingHandler;
import org.xson.tangyuan.sharding.ShardingDefVo;
import org.xson.tangyuan.sharding.ShardingDefVo.ShardingMode;
import org.xson.tangyuan.sharding.ShardingHandler;
import org.xson.tangyuan.sql.SqlComponent;
import org.xson.tangyuan.sql.datasource.DataSourceGroupVo;
import org.xson.tangyuan.sql.datasource.DataSourceVo;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlSqlShardingBuilder2 extends DefaultXmlComponentBuilder {

	private XmlSqlContext                componentContext      = null;

	private Map<String, ShardingHandler> shardingClassMap      = new HashMap<String, ShardingHandler>();
	private Map<String, ShardingDefVo>   shardingDefMap        = new HashMap<String, ShardingDefVo>();

	private ShardingHandler              hashShardingHandler   = new HashShardingHandler();
	private ShardingHandler              modShardingHandler    = new ModShardingHandler();
	private ShardingHandler              randomShardingHandler = new RandomShardingHandler();
	private ShardingHandler              rangeShardingHandler  = new RangeShardingHandler();

	@Override
	protected void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing.type", "sharding", resource));
		this.componentContext = (XmlSqlContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "sharding", true);
		this.configurationElement();

		//		this.componentContext.setMappingVoMap(mappingVoMap);
		if (shardingDefMap.size() > 0) {//TODO
			SqlComponent.getInstance().getShardingDefManager().setShardingDefMap(shardingDefMap);
		}

		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();

		this.shardingClassMap = null;
		this.shardingDefMap = null;

		this.hashShardingHandler = null;
		this.modShardingHandler = null;
		this.randomShardingHandler = null;
		this.rangeShardingHandler = null;

		this.componentContext = null;
	}

	private void configurationElement() throws Throwable {
		buildShardingClassNode(this.root.evalNodes("shardingClass"));
		buildShardingTableNode(this.root.evalNodes("table"));
	}

	private void buildShardingClassNode(List<XmlNodeWrapper> contexts) throws Throwable {
		String tagName = "shardingClass";
		for (XmlNodeWrapper xNode : contexts) {
			String          id        = getStringFromAttr(xNode, "id", lang("xml.tag.attribute.empty", "id", tagName, this.resource));
			String          className = getStringFromAttr(xNode, "class", lang("xml.tag.attribute.empty", "class", tagName, this.resource));
			ShardingHandler handler   = getInstanceForName(className, ShardingHandler.class, lang("xml.class.impl.interface", className, ShardingHandler.class.getName()));

			if (shardingClassMap.containsKey(id)) {
				throw new XmlParseException(lang("xml.tag.id.repeated", id, tagName, this.resource));
			}
			shardingClassMap.put(id, handler);
			log.info(lang("add.tag.class"), tagName, className);
		}
	}

	private void buildShardingTableNode(List<XmlNodeWrapper> contexts) {
		String tagName = "table";

		int    size    = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode                   = contexts.get(i);

			String         name                    = getStringFromAttr(xNode, "name", lang("xml.tag.attribute.empty", "name", tagName, this.resource));
			String         dataSource              = getStringFromAttr(xNode, "dataSource", lang("xml.tag.attribute.empty", "dataSource", tagName, this.resource));
			String         _mode                   = getStringFromAttr(xNode, "mode");
			Integer        dbCount                 = getIntegerFromAttr(xNode, "dbCount");
			Integer        tableCount              = getIntegerFromAttr(xNode, "tableCount");
			Integer        tableCapacity           = getIntegerFromAttr(xNode, "tableCapacity");
			String         impl                    = getStringFromAttr(xNode, "impl");
			String         keys                    = getStringFromAttr(xNode, "keys");
			boolean        requireKeyword          = getBoolFromAttr(xNode, "requireKeyword", true);
			boolean        tableNameIndexIncrement = getBoolFromAttr(xNode, "increment", true);

			if (shardingDefMap.containsKey(name)) {
				throw new XmlParseException(lang("xml.tag.repeated", name, tagName, this.resource));
			}

			DataSourceVo dsVo = this.componentContext.getDataSourceVoMap().get(dataSource);
			if (null == dsVo) {
				throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", dataSource, "dataSource", tagName, this.resource));
			}

			boolean dataSourceGroup = dsVo.isGroup();
			int     dataSourceCount = 1;
			if (dataSourceGroup) {
				dataSourceCount = ((DataSourceGroupVo) dsVo).getCount();
			}

			ShardingMode mode = null;
			if (null != _mode) {
				mode = getShardingMode(_mode);
			}

			ShardingHandler handler = null;
			if (null != impl) {
				handler = shardingClassMap.get(impl);
				if (null == handler) {
					throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", impl, "impl", tagName, this.resource));
				}
			} else {
				handler = getShardingHandler(mode);
				if (null == handler) {
					throw new XmlParseException(lang("xml.tag.attribute.reference.invalid", _mode, "mode", tagName, this.resource));
				}
			}

			// random不需要关键字
			if (ShardingMode.RANDOM == mode) {
				requireKeyword = false;
			}

			Variable[] keywords = null;
			if (null != keys) {
				String[] array = keys.split(",");
				keywords = new Variable[array.length];
				for (int j = 0; j < array.length; j++) {
					keywords[j] = new NormalParser().parse(array[j].trim());
				}
			}

			// fix:修复在自定义模式下默认值的问题
			if (null != handler) {
				if (null == dbCount) {
					dbCount = 0;
				}
				if (null == tableCount) {
					tableCount = 0;
				}
				if (null == tableCapacity) {
					tableCapacity = 0;
				}
			}

			String        defaultDataSource = null;
			ShardingDefVo shardingDefVo     = new ShardingDefVo(name, dataSource, mode, dbCount, tableCount, tableCapacity, keywords, tableNameIndexIncrement, handler,
					dataSourceCount, dataSourceGroup, requireKeyword, defaultDataSource);

			shardingDefMap.put(name, shardingDefVo);
			//			log.info("add sharding table: " + name);
			log.info(lang("add.tag.class"), tagName, name);
		}
	}

	//	private ShardingMode getShardingMode(String type) {
	//		if ("RANGE".equalsIgnoreCase(type)) {
	//			return ShardingMode.RANGE;
	//		} else if ("HASH".equalsIgnoreCase(type)) {
	//			return ShardingMode.HASH;
	//		} else if ("MOD".equalsIgnoreCase(type)) {
	//			return ShardingMode.MOD;
	//		} else if ("RANDOM".equalsIgnoreCase(type)) {
	//			return ShardingMode.RANDOM;
	//		}
	//		return null;
	//	}

	//	private ShardingMode getShardingMode(String type) {
	//		if (ShardingMode.RANGE.toString().equalsIgnoreCase(type)) {
	//			return ShardingMode.RANGE;
	//		} else if (ShardingMode.HASH.toString().equalsIgnoreCase(type)) {
	//			return ShardingMode.HASH;
	//		} else if (ShardingMode.MOD.toString().equalsIgnoreCase(type)) {
	//			return ShardingMode.MOD;
	//		} else if (ShardingMode.RANDOM.toString().equalsIgnoreCase(type)) {
	//			return ShardingMode.RANDOM;
	//		}
	//		return null;
	//	}

	private ShardingHandler getShardingHandler(ShardingMode mode) {
		if (ShardingMode.RANGE == mode) {
			return rangeShardingHandler;
		} else if (ShardingMode.HASH == mode) {
			return hashShardingHandler;
		} else if (ShardingMode.MOD == mode) {
			return modShardingHandler;
		} else if (ShardingMode.RANDOM == mode) {
			return randomShardingHandler;
		}
		return null;
	}

	protected ShardingMode getShardingMode(String type) {
		if (ShardingMode.RANGE.toString().equalsIgnoreCase(type)) {
			return ShardingMode.RANGE;
		} else if (ShardingMode.HASH.toString().equalsIgnoreCase(type)) {
			return ShardingMode.HASH;
		} else if (ShardingMode.MOD.toString().equalsIgnoreCase(type)) {
			return ShardingMode.MOD;
		} else if (ShardingMode.RANDOM.toString().equalsIgnoreCase(type)) {
			return ShardingMode.RANDOM;
		}
		return null;
	}

	//////////////////////////// 
	//	private void buildShardingClassNode(List<XmlNodeWrapper> contexts) throws Exception {
	//
	//		for (XmlNodeWrapper xNode : contexts) {
	//			String id = StringUtils.trim(xNode.getStringAttribute("id")); // xml v
	//			if (null != shardingClassMap.get(id)) {
	//				throw new XmlParseException("Duplicate shardingClass:" + id);
	//			}
	//			String   className    = StringUtils.trim(xNode.getStringAttribute("class")); // xml v
	//			Class<?> handlerClass = ClassUtils.forName(className);
	//			if (!ShardingHandler.class.isAssignableFrom(handlerClass)) {
	//				throw new XmlParseException("mapping class not implement the ShardingHandler interface: " + className);
	//			}
	//			Object handler = handlerClass.newInstance();
	//			shardingClassMap.put(id, (ShardingHandler) handler);
	//			log.info("add sharding handler: " + className);
	//		}
	//	}

	//		info("*** Start parsing(sharding): {}", resource);
	//		init(resource, "mapper", true);
	//		this.componentContext = (XmlSqlContext) xmlContext;
	//		this.globalContext = this.componentContext.getXmlContext();
	//		parse0();
	//		if (shardingDefMap.size() > 0) {
	//			SqlComponent.getInstance().getShardingDefManager().setShardingDefMap(shardingDefMap);
	//		}
	//		clean();

	//	private void buildShardingTableNode(List<XmlNodeWrapper> contexts) {
	//		int size = contexts.size();
	//		for (int i = 0; i < size; i++) {
	//			XmlNodeWrapper xNode = contexts.get(i);
	//			String         name  = StringUtils.trim(xNode.getStringAttribute("name"));
	//			if (null != shardingDefMap.get(name)) {
	//				throw new XmlParseException("Already existing ShardingTable: " + name);
	//			}
	//			String dataSource = StringUtils.trim(xNode.getStringAttribute("dataSource"));
	//			if (null == dataSource || 0 == dataSource.length()) {
	//				throw new XmlParseException("Invalid ShardingTable dataSource: " + name);
	//			}
	//
	//			DataSourceVo dsVo = this.componentContext.getDataSourceVoMap().get(dataSource);
	//			if (null == dsVo) {
	//				throw new XmlParseException("不存在的dataSource:" + dataSource);
	//			}
	//			boolean dataSourceGroup = dsVo.isGroup();
	//			int     dataSourceCount = 1;
	//			if (dataSourceGroup) {
	//				dataSourceCount = ((DataSourceGroupVo) dsVo).getCount();
	//			}
	//
	//			ShardingMode mode  = null;
	//			String       _mode = StringUtils.trim(xNode.getStringAttribute("mode"));
	//			if (null != _mode) {
	//				mode = getShardingMode(_mode);
	//			}
	//
	//			Integer dbCount  = null;
	//			String  _dbCount = StringUtils.trim(xNode.getStringAttribute("dbCount"));
	//			if (null != _dbCount) {
	//				dbCount = Integer.parseInt(_dbCount);
	//			}
	//			Integer tableCount  = null;
	//			String  _tableCount = StringUtils.trim(xNode.getStringAttribute("tableCount"));
	//			if (null != _tableCount) {
	//				tableCount = Integer.parseInt(_tableCount);
	//			}
	//			Integer tableCapacity  = null;
	//			String  _tableCapacity = StringUtils.trim(xNode.getStringAttribute("tableCapacity"));
	//			if (null != _tableCapacity) {
	//				tableCapacity = Integer.parseInt(_tableCapacity);
	//			}
	//
	//			ShardingHandler handler = null;
	//			String          impl    = StringUtils.trim(xNode.getStringAttribute("impl"));
	//			if (null != impl) {
	//				handler = shardingClassMap.get(impl);
	//				if (null == handler) {
	//					throw new XmlParseException("Non-existent shardingHandler:" + impl);
	//				}
	//			} else {
	//				handler = getShardingHandler(mode);
	//			}
	//
	//			boolean requireKeyword  = true;
	//			String  _requireKeyword = StringUtils.trim(xNode.getStringAttribute("requireKeyword"));
	//			if (null != _requireKeyword) {
	//				requireKeyword = Boolean.parseBoolean(_requireKeyword);
	//			}
	//			// random不需要关键字
	//			if (ShardingMode.RANDOM == mode) {
	//				requireKeyword = false;
	//			}
	//
	//			Variable[] keywords = null;
	//			String     keys     = StringUtils.trim(xNode.getStringAttribute("keys"));
	//			if (null != keys) {
	//				String[] array = keys.split(",");
	//				keywords = new Variable[array.length];
	//				for (int j = 0; j < array.length; j++) {
	//					keywords[j] = new NormalParser().parse(array[j].trim());
	//				}
	//			}
	//
	//			if (null == handler && (null == dbCount || null == tableCount || null == tableCapacity)) {
	//				throw new XmlParseException("Sharding table parameter is incomplete: " + name);
	//			}
	//
	//			boolean tableNameIndexIncrement = true;
	//			String  _increment              = StringUtils.trim(xNode.getStringAttribute("increment"));
	//			if (null != _increment) {
	//				tableNameIndexIncrement = Boolean.parseBoolean(_increment);
	//			}
	//
	//			//  fix:修复在自定义模式下默认值的问题
	//			if (null != handler) {
	//				if (null == dbCount) {
	//					dbCount = 0;
	//				}
	//				if (null == tableCount) {
	//					tableCount = 0;
	//				}
	//				if (null == tableCapacity) {
	//					tableCapacity = 0;
	//				}
	//			}
	//
	//			String        defaultDataSource = null;
	//
	//			ShardingDefVo shardingDefVo     = new ShardingDefVo(name, dataSource, mode, dbCount, tableCount,
	//					tableCapacity, keywords, tableNameIndexIncrement, handler, dataSourceCount, dataSourceGroup,
	//					requireKeyword, defaultDataSource);
	//
	//			shardingDefMap.put(name, shardingDefVo);
	//			log.info("add sharding table: " + name);
	//		}
	//	}
}
