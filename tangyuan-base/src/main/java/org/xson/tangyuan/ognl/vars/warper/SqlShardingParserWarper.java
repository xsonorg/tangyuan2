package org.xson.tangyuan.ognl.vars.warper;

import org.xson.tangyuan.ognl.vars.ParserWarper;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.VariableConfig;
import org.xson.tangyuan.ognl.vars.parser.ShardingParser;
import org.xson.tangyuan.sharding.ShardingDefManager;
import org.xson.tangyuan.sharding.ShardingArgVo.ShardingTemplate;

/**
 * SQL分库分表变量解析包装
 */
public class SqlShardingParserWarper extends ParserWarper {

	private ShardingTemplate	template		= null;
	private ShardingDefManager	shardingManager	= null;

	public SqlShardingParserWarper(ShardingTemplate template, ShardingDefManager shardingManager) {
		this.template = template;
		this.shardingManager = shardingManager;
	}

	@Override
	public Variable parse(String text, VariableConfig config) {
		text = text.trim();
		return new ShardingParser(this.template, this.shardingManager).parse(text);
	}
}
