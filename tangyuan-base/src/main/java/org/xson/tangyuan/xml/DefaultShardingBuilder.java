package org.xson.tangyuan.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.sharding.HashShardingHandler;
import org.xson.tangyuan.sharding.ModShardingHandler;
import org.xson.tangyuan.sharding.RandomShardingHandler;
import org.xson.tangyuan.sharding.RangeShardingHandler;
import org.xson.tangyuan.sharding.ShardingDefVo;
import org.xson.tangyuan.sharding.ShardingDefVo.ShardingMode;
import org.xson.tangyuan.sharding.ShardingHandler;

/**
 * Sharding解析基类
 */
public abstract class DefaultShardingBuilder extends DefaultXmlComponentBuilder {

	protected Map<String, ShardingHandler> shardingClassMap      = new HashMap<String, ShardingHandler>();
	protected Map<String, ShardingDefVo>   shardingDefMap        = new HashMap<String, ShardingDefVo>();

	protected ShardingHandler              hashShardingHandler   = new HashShardingHandler();
	protected ShardingHandler              modShardingHandler    = new ModShardingHandler();
	protected ShardingHandler              randomShardingHandler = new RandomShardingHandler();
	protected ShardingHandler              rangeShardingHandler  = new RangeShardingHandler();

	@Override
	protected void clean() {
		super.clean();
		this.shardingClassMap = null;
		this.shardingDefMap = null;

		this.hashShardingHandler = null;
		this.modShardingHandler = null;
		this.randomShardingHandler = null;
		this.rangeShardingHandler = null;
	}

	protected void buildShardingClassNode(List<XmlNodeWrapper> contexts) throws Throwable {
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

	protected ShardingHandler getShardingHandler(ShardingMode mode) {
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
}
