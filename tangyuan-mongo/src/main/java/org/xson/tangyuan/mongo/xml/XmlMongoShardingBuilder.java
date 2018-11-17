package org.xson.tangyuan.mongo.xml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mongo.MongoComponent;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceGroupVo;
import org.xson.tangyuan.mongo.datasource.MongoDataSourceVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.sharding.HashShardingHandler;
import org.xson.tangyuan.sharding.ModShardingHandler;
import org.xson.tangyuan.sharding.RandomShardingHandler;
import org.xson.tangyuan.sharding.RangeShardingHandler;
import org.xson.tangyuan.sharding.ShardingDefVo;
import org.xson.tangyuan.sharding.ShardingDefVo.ShardingMode;
import org.xson.tangyuan.sharding.ShardingHandler;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlMongoShardingBuilder {

	private Log								log						= LogFactory.getLog(getClass());

	private XPathParser						xPathParser				= null;
	private XmlMongoContext					context					= null;

	private Map<String, ShardingHandler>	shardingClassMap		= new HashMap<String, ShardingHandler>();
	private Map<String, ShardingDefVo>		shardingDefMap			= new HashMap<String, ShardingDefVo>();

	private ShardingHandler					hashShardingHandler		= new HashShardingHandler();
	private ShardingHandler					modShardingHandler		= new ModShardingHandler();
	private ShardingHandler					randomShardingHandler	= new RandomShardingHandler();
	private ShardingHandler					rangeShardingHandler	= new RangeShardingHandler();

	public XmlMongoShardingBuilder(InputStream inputStream) {
		this.xPathParser = new XPathParser(inputStream);
	}

	public void parse(XmlMongoContext context) throws Throwable {
		this.context = context;
		configurationElement(xPathParser.evalNode("/sharding"));
		shardingClassMap = null;
		shardingDefMap = null;
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {
		buildShardingClassNodes(context.evalNodes("shardingClass"));
		buildShardingTableNodes(context.evalNodes("table"));
		if (shardingDefMap.size() > 0) {
			MongoComponent.getInstance().getShardingDefManager().setShardingDefMap(shardingDefMap);
		}
	}

	private void buildShardingClassNodes(List<XmlNodeWrapper> contexts) throws Exception {
		// log.info("解析ShardingHandler:" + contexts.size());
		for (XmlNodeWrapper xNode : contexts) {
			String id = StringUtils.trim(xNode.getStringAttribute("id")); // xml v
			if (null != shardingClassMap.get(id)) {
				throw new XmlParseException("Duplicate shardingClass:" + id);
			}
			String className = StringUtils.trim(xNode.getStringAttribute("class")); // xml v
			Class<?> handlerClass = ClassUtils.forName(className);
			if (!ShardingHandler.class.isAssignableFrom(handlerClass)) {
				throw new XmlParseException("mapping class not implement the ShardingHandler interface: " + className);
			}
			Object handler = handlerClass.newInstance();
			shardingClassMap.put(id, (ShardingHandler) handler);
			log.info("add sharding handler: " + className);
		}
	}

	private void buildShardingTableNodes(List<XmlNodeWrapper> contexts) {
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String name = StringUtils.trim(xNode.getStringAttribute("name"));
			if (null != shardingDefMap.get(name)) {
				throw new XmlParseException("Already existing ShardingTable: " + name);
			}
			String dataSource = StringUtils.trim(xNode.getStringAttribute("dataSource"));
			if (null == dataSource || 0 == dataSource.length()) {
				throw new XmlParseException("Invalid ShardingTable dataSource: " + name);
			}

			MongoDataSourceVo dsVo = this.context.getDataSourceVoMap().get(dataSource);
			if (null == dsVo) {
				throw new XmlParseException("Non-existent mongo dataSource:" + dataSource);
			}
			boolean dataSourceGroup = dsVo.isGroup();
			int dataSourceCount = 1;
			if (dataSourceGroup) {
				dataSourceCount = ((MongoDataSourceGroupVo) dsVo).getCount();
			}

			ShardingMode mode = null;
			String _mode = StringUtils.trim(xNode.getStringAttribute("mode"));
			if (null != _mode) {
				mode = getShardingMode(_mode);
			}

			Integer dbCount = null;
			String _dbCount = StringUtils.trim(xNode.getStringAttribute("dbCount"));
			if (null != _dbCount) {
				dbCount = Integer.parseInt(_dbCount);
			}
			Integer tableCount = null;
			String _tableCount = StringUtils.trim(xNode.getStringAttribute("tableCount"));
			if (null != _tableCount) {
				tableCount = Integer.parseInt(_tableCount);
			}
			Integer tableCapacity = null;
			String _tableCapacity = StringUtils.trim(xNode.getStringAttribute("tableCapacity"));
			if (null != _tableCapacity) {
				tableCapacity = Integer.parseInt(_tableCapacity);
			}

			ShardingHandler handler = null;
			String impl = StringUtils.trim(xNode.getStringAttribute("impl"));
			if (null != impl) {
				handler = shardingClassMap.get(impl);
				if (null == handler) {
					throw new XmlParseException("Non-existent shardingHandler:" + impl);
				}
			} else {
				handler = getShardingHandler(mode);
			}

			boolean requireKeyword = true;
			String _requireKeyword = StringUtils.trim(xNode.getStringAttribute("requireKeyword"));
			if (null != _requireKeyword) {
				requireKeyword = Boolean.parseBoolean(_requireKeyword);
			}
			// random不需要关键字
			if (ShardingMode.RANDOM == mode) {
				requireKeyword = false;
			}

			Variable[] keywords = null;
			String keys = StringUtils.trim(xNode.getStringAttribute("keys"));
			if (null != keys) {
				String[] array = keys.split(",");
				keywords = new Variable[array.length];
				for (int j = 0; j < array.length; j++) {
					keywords[j] = new NormalParser().parse(array[j].trim());
				}
			}

			if (null == handler && (null == dbCount || null == tableCount || null == tableCapacity)) {
				throw new XmlParseException("Sharding table parameter is incomplete: " + name);
			}

			boolean tableNameIndexIncrement = true;
			String _increment = StringUtils.trim(xNode.getStringAttribute("increment"));
			if (null != _increment) {
				tableNameIndexIncrement = Boolean.parseBoolean(_increment);
			}

			// TODO fix:修复在自定义模式下默认值的问题
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

			String defaultDataSource = null;

			ShardingDefVo shardingDefVo = new ShardingDefVo(name, dataSource, mode, dbCount, tableCount, tableCapacity, keywords,
					tableNameIndexIncrement, handler, dataSourceCount, dataSourceGroup, requireKeyword, defaultDataSource);

			shardingDefMap.put(name, shardingDefVo);
			log.info("add sharding table: " + name);
		}
	}

	private ShardingMode getShardingMode(String type) {
		if ("RANGE".equalsIgnoreCase(type)) {
			return ShardingMode.RANGE;
		} else if ("HASH".equalsIgnoreCase(type)) {
			return ShardingMode.HASH;
		} else if ("MOD".equalsIgnoreCase(type)) {
			return ShardingMode.MOD;
		} else if ("RANDOM".equalsIgnoreCase(type)) {
			return ShardingMode.RANDOM;
		}
		return null;
	}

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
}
