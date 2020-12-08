package org.xson.tangyuan.hive.xml;

import java.util.List;

import org.xson.tangyuan.hive.HiveComponent;
import org.xson.tangyuan.hive.datasource.DataSourceGroupVo;
import org.xson.tangyuan.hive.datasource.DataSourceVo;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.parser.NormalParser;
import org.xson.tangyuan.sharding.ShardingDefVo;
import org.xson.tangyuan.sharding.ShardingDefVo.ShardingMode;
import org.xson.tangyuan.sharding.ShardingHandler;
import org.xson.tangyuan.xml.DefaultShardingBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;

public class XmlHiveShardingBuilder extends DefaultShardingBuilder {

	private XmlHiveContext componentContext = null;

	@Override
	protected void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing.type", "sharding", resource));
		this.componentContext = (XmlHiveContext) xmlContext;
		this.globalContext = this.componentContext.getXmlContext();
		this.init(resource, "sharding", true);
		this.configurationElement();

		if (shardingDefMap.size() > 0) {// TODO
			HiveComponent.getInstance().getShardingDefManager().setShardingDefMap(shardingDefMap);
		}

		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();

		this.componentContext = null;
	}

	private void configurationElement() throws Throwable {
		buildShardingClassNode(this.root.evalNodes("shardingClass"));
		buildShardingTableNode(this.root.evalNodes("table"));
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
			// log.info("add sharding table: " + name);
			log.info(lang("add.tag.class"), tagName, name);
		}
	}

}
