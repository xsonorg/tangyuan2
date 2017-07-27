package org.xson.tangyuan.ognl.vars.parser;

import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.vo.ShardingVariable;
import org.xson.tangyuan.sharding.ShardingArgVo;
import org.xson.tangyuan.sharding.ShardingArgVo.ShardingTemplate;
import org.xson.tangyuan.sharding.ShardingDefManager;
import org.xson.tangyuan.sharding.ShardingDefVo;
import org.xson.tangyuan.xml.XmlParseException;

public class ShardingParser extends AbstractParser {

	private ShardingTemplate	template		= null;

	private ShardingDefManager	shardingManager	= null;

	public boolean check(String text) {
		if (text.startsWith("DT:") || text.startsWith("DI:") || text.startsWith("T:") || text.startsWith("I:") || text.startsWith("D:")) {
			return true;
		}
		return false;
	}

	public ShardingParser(ShardingTemplate template, ShardingDefManager shardingManager) {
		this.template = template;
		this.shardingManager = shardingManager;
	}

	@Override
	public Variable parse(String text) {
		text = text.trim();

		String[] array = text.split(",");
		// ShardingDefVo shardingDef = TangYuanContainer.getInstance().getShardingDef(array[0].trim());
		ShardingDefVo shardingDef = shardingManager.getShardingDef(array[0].trim());
		if (null == shardingDef) {
			throw new XmlParseException("The sharding.table does not exist: " + text);
		}

		Variable[] keywords = shardingDef.getKeywords();
		if (shardingDef.isRequireKeyword()) {
			if (null == keywords && 1 == array.length) {
				throw new XmlParseException("The sharding.table.keywords does not exist: " + text);
			} else if (array.length > 1) {
				keywords = new Variable[array.length - 1];
				for (int i = 0; i < keywords.length; i++) {
					// keywords[i] = VariableParser.parse(array[i + 1].trim(), false);
					keywords[i] = new NormalParser().parse(array[i + 1].trim());
				}
			}
		}

		ShardingArgVo shardingArg = new ShardingArgVo(array[0].trim(), template, keywords, shardingDef);
		return new ShardingVariable(text, shardingArg);
	}

}
